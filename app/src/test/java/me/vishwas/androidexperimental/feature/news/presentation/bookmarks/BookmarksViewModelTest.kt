package me.vishwas.androidexperimental.feature.news.presentation.bookmarks

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.Runs
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.vishwas.androidexperimental.feature.news.domain.usecase.GetBookmarksUseCase
import me.vishwas.androidexperimental.feature.news.domain.usecase.ToggleBookmarkUseCase
import me.vishwas.androidexperimental.util.MainDispatcherRule
import me.vishwas.androidexperimental.util.testArticle
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Unit tests for [BookmarksViewModel].
 *
 * The ViewModel derives its [BookmarksUiState] entirely from the [GetBookmarksUseCase] Flow, so
 * these tests verify that the mapping and state transitions are correct.
 *
 * Turbine is used to assert Flow emissions in sequence.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BookmarksViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getBookmarksUseCase: GetBookmarksUseCase = mockk()
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase = mockk()

    private fun createViewModel() = BookmarksViewModel(getBookmarksUseCase, toggleBookmarkUseCase)

    // ── uiState emissions ─────────────────────────────────────────────────────

    @Test
    fun `uiState emits Loading then Empty when no bookmarks exist`() = runTest {
        every { getBookmarksUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertIs<BookmarksUiState.Loading>(awaitItem())  // stateIn initial value
            assertIs<BookmarksUiState.Empty>(awaitItem())    // empty list → Empty
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState emits Loading then Success when bookmarks exist`() = runTest {
        val articles = listOf(testArticle(url = "url1"), testArticle(url = "url2"))
        every { getBookmarksUseCase() } returns flowOf(articles)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertIs<BookmarksUiState.Loading>(awaitItem())
            val successState = awaitItem()
            assertIs<BookmarksUiState.Success>(successState)
            assertEquals(2, successState.articles.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState Success contains the correct articles from use case`() = runTest {
        val article = testArticle(title = "Breaking News")
        every { getBookmarksUseCase() } returns flowOf(listOf(article))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()  // Loading
            val successState = awaitItem() as BookmarksUiState.Success
            assertEquals("Breaking News", successState.articles[0].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState transitions to Empty when all bookmarks are removed`() = runTest {
        val articles = listOf(testArticle())
        // Simulate first emission = 1 article, second = empty
        every { getBookmarksUseCase() } returns kotlinx.coroutines.flow.flow {
            emit(articles)
            emit(emptyList())
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()  // Loading
            assertIs<BookmarksUiState.Success>(awaitItem())
            assertIs<BookmarksUiState.Empty>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── removeBookmark ────────────────────────────────────────────────────────

    @Test
    fun `removeBookmark delegates to toggleBookmarkUseCase`() = runTest {
        every { getBookmarksUseCase() } returns flowOf(emptyList())
        coEvery { toggleBookmarkUseCase(any()) } just Runs

        val viewModel = createViewModel()
        val article = testArticle()
        viewModel.removeBookmark(article)

        coVerify(exactly = 1) { toggleBookmarkUseCase(article) }
    }

    @Test
    fun `removeBookmark does not crash when called multiple times`() = runTest {
        every { getBookmarksUseCase() } returns flowOf(emptyList())
        coEvery { toggleBookmarkUseCase(any()) } just Runs

        val viewModel = createViewModel()
        val article = testArticle()
        viewModel.removeBookmark(article)
        viewModel.removeBookmark(article)

        coVerify(exactly = 2) { toggleBookmarkUseCase(article) }
    }
}
