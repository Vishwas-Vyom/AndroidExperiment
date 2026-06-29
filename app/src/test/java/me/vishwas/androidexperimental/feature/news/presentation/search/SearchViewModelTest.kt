package me.vishwas.androidexperimental.feature.news.presentation.search

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import me.vishwas.androidexperimental.core.network.NetworkResult
import me.vishwas.androidexperimental.feature.news.domain.usecase.SearchNewsUseCase
import me.vishwas.androidexperimental.util.MainDispatcherRule
import me.vishwas.androidexperimental.util.testArticle
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Unit tests for [SearchViewModel].
 *
 * Key behaviours tested:
 * - Initial state is [SearchUiState.Idle]
 * - Blank query immediately clears to Idle
 * - Successful search emits Success with the correct query and articles
 * - Failed search emits Error
 * - clearSearch resets state to Idle and cancels the in-flight job
 * - Rapid successive calls cancel the previous job (debounce)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val searchNewsUseCase: SearchNewsUseCase = mockk()

    private fun createViewModel() = SearchViewModel(searchNewsUseCase)

    // ── initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial state is Idle`() {
        val viewModel = createViewModel()
        assertEquals(SearchUiState.Idle, viewModel.uiState.value)
    }

    // ── blank query ───────────────────────────────────────────────────────────

    @Test
    fun `search with blank query resets state to Idle`() = runTest {
        val viewModel = createViewModel()
        viewModel.search("   ")
        assertEquals(SearchUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `search with empty string resets state to Idle`() = runTest {
        val viewModel = createViewModel()
        viewModel.search("")
        assertEquals(SearchUiState.Idle, viewModel.uiState.value)
    }

    // ── successful search ─────────────────────────────────────────────────────

    @Test
    fun `search emits Success state with matching articles`() = runTest {
        val articles = listOf(testArticle(title = "AI breakthrough"))
        coEvery { searchNewsUseCase("AI") } returns NetworkResult.Success(articles)

        val viewModel = createViewModel()
        viewModel.search("AI")

        val state = viewModel.uiState.value
        assertIs<SearchUiState.Success>(state)
        assertEquals("AI", state.query)
        assertEquals(1, state.articles.size)
        assertEquals("AI breakthrough", state.articles[0].title)
    }

    @Test
    fun `search emits Success with empty list when API returns no articles`() = runTest {
        coEvery { searchNewsUseCase("nothing") } returns NetworkResult.Success(emptyList())

        val viewModel = createViewModel()
        viewModel.search("nothing")

        val state = viewModel.uiState.value
        assertIs<SearchUiState.Success>(state)
        assertEquals(0, state.articles.size)
    }

    // ── error state ───────────────────────────────────────────────────────────

    @Test
    fun `search emits Error state when API fails`() = runTest {
        coEvery { searchNewsUseCase("AI") } returns NetworkResult.Error("No internet")

        val viewModel = createViewModel()
        viewModel.search("AI")

        val state = viewModel.uiState.value
        assertIs<SearchUiState.Error>(state)
        assertEquals("No internet", state.message)
    }

    // ── clearSearch ───────────────────────────────────────────────────────────

    @Test
    fun `clearSearch resets state to Idle`() = runTest {
        coEvery { searchNewsUseCase("AI") } returns NetworkResult.Success(emptyList())

        val viewModel = createViewModel()
        viewModel.search("AI")
        viewModel.clearSearch()

        assertEquals(SearchUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `clearSearch cancels an in-flight search so useCase is never called`() = runTest {
        // Use coEvery setup so that if the useCase IS called it won't throw
        coEvery { searchNewsUseCase(any()) } returns NetworkResult.Success(emptyList())

        val viewModel = createViewModel()
        // The debounce delay is 400ms. Advance 200ms (half-way), then clear.
        viewModel.search("AI")
        advanceTimeBy(200)
        viewModel.clearSearch()
        advanceUntilIdle()

        // With clearSearch cancelling the job, the useCase should not have been invoked.
        coVerify(exactly = 0) { searchNewsUseCase(any()) }
        assertEquals(SearchUiState.Idle, viewModel.uiState.value)
    }

    // ── debounce / rapid typing ───────────────────────────────────────────────

    @Test
    fun `rapid successive searches cancel previous and only invoke useCase for last query`() = runTest {
        coEvery { searchNewsUseCase(any()) } returns NetworkResult.Success(emptyList())

        val viewModel = createViewModel()

        // Type "A", then immediately "AI" — "A"'s coroutine should be cancelled
        viewModel.search("A")
        viewModel.search("AI")
        advanceUntilIdle()

        // "A" should never have reached the API
        coVerify(exactly = 0) { searchNewsUseCase("A") }
        // "AI" should have been called exactly once
        coVerify(exactly = 1) { searchNewsUseCase("AI") }
    }

    @Test
    fun `search after debounce delay reaches the useCase`() = runTest {
        coEvery { searchNewsUseCase("AI") } returns NetworkResult.Success(emptyList())

        val viewModel = createViewModel()
        viewModel.search("AI")
        advanceTimeBy(401)   // past the 400 ms debounce
        advanceUntilIdle()

        coVerify(exactly = 1) { searchNewsUseCase("AI") }
    }
}
