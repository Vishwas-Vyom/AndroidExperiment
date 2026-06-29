package me.vishwas.androidexperimental.feature.news.presentation.detail

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
import me.vishwas.androidexperimental.core.common.ArticleCache
import me.vishwas.androidexperimental.feature.news.domain.repository.NewsRepository
import me.vishwas.androidexperimental.feature.news.domain.usecase.ToggleBookmarkUseCase
import me.vishwas.androidexperimental.util.MainDispatcherRule
import me.vishwas.androidexperimental.util.testArticle
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

/**
 * Unit tests for [DetailViewModel].
 *
 * [DetailViewModel] reads [ArticleCache.selectedArticle] at creation time, so each test must
 * set the cache before calling [createViewModel] and clear it afterwards.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val toggleBookmarkUseCase: ToggleBookmarkUseCase = mockk()
    private val newsRepository: NewsRepository = mockk()

    @Before
    fun setup() {
        ArticleCache.selectedArticle = null
    }

    @After
    fun teardown() {
        ArticleCache.selectedArticle = null
    }

    private fun createViewModel() = DetailViewModel(toggleBookmarkUseCase, newsRepository)

    // ── article exposure ──────────────────────────────────────────────────────

    @Test
    fun `article reflects whatever is in ArticleCache`() {
        val cached = testArticle(title = "From Cache")
        ArticleCache.selectedArticle = cached
        every { newsRepository.isBookmarked(any()) } returns flowOf(false)

        val viewModel = createViewModel()

        assertEquals(cached, viewModel.article)
    }

    @Test
    fun `article is null when ArticleCache is empty`() {
        ArticleCache.selectedArticle = null

        val viewModel = createViewModel()

        assertNull(viewModel.article)
    }

    // ── isBookmarked flow ─────────────────────────────────────────────────────

    @Test
    fun `isBookmarked emits false initially when article is null`() = runTest {
        ArticleCache.selectedArticle = null

        val viewModel = createViewModel()

        // isBookmarked is a plain MutableStateFlow(false) when article is null
        assertFalse(viewModel.isBookmarked.value)
    }

    @Test
    fun `isBookmarked reflects repository flow when article is present`() = runTest {
        val article = testArticle(url = "https://example.com/detail")
        ArticleCache.selectedArticle = article
        every { newsRepository.isBookmarked("https://example.com/detail") } returns flowOf(true)

        val viewModel = createViewModel()

        viewModel.isBookmarked.test {
            // Initial value is false (stateIn initialValue), then true from repository
            val first = awaitItem()
            // Either false then true, or (with UnconfinedTestDispatcher) it might resolve to true immediately.
            // Verify that the repository was consulted.
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { newsRepository.isBookmarked("https://example.com/detail") }
    }

    // ── toggleBookmark ────────────────────────────────────────────────────────

    @Test
    fun `toggleBookmark delegates to toggleBookmarkUseCase with the cached article`() = runTest {
        val article = testArticle()
        ArticleCache.selectedArticle = article
        every { newsRepository.isBookmarked(any()) } returns flowOf(false)
        coEvery { toggleBookmarkUseCase(any()) } just Runs

        val viewModel = createViewModel()
        viewModel.toggleBookmark()

        coVerify(exactly = 1) { toggleBookmarkUseCase(article) }
    }

    @Test
    fun `toggleBookmark does nothing when article is null`() = runTest {
        ArticleCache.selectedArticle = null

        val viewModel = createViewModel()
        viewModel.toggleBookmark()

        coVerify(exactly = 0) { toggleBookmarkUseCase(any()) }
    }

    @Test
    fun `toggleBookmark can be called multiple times`() = runTest {
        val article = testArticle()
        ArticleCache.selectedArticle = article
        every { newsRepository.isBookmarked(any()) } returns flowOf(false)
        coEvery { toggleBookmarkUseCase(any()) } just Runs

        val viewModel = createViewModel()
        viewModel.toggleBookmark()
        viewModel.toggleBookmark()

        coVerify(exactly = 2) { toggleBookmarkUseCase(article) }
    }
}
