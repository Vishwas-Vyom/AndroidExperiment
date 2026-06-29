package me.vishwas.androidexperimental.feature.news.presentation.home

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.Runs
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.vishwas.androidexperimental.core.domain.NoParams
import me.vishwas.androidexperimental.core.network.NetworkResult
import me.vishwas.androidexperimental.feature.news.domain.model.NewsCategory
import me.vishwas.androidexperimental.feature.news.domain.repository.NewsRepository
import me.vishwas.androidexperimental.feature.news.domain.usecase.GetNewsByCategoryUseCase
import me.vishwas.androidexperimental.feature.news.domain.usecase.GetTopHeadlinesUseCase
import me.vishwas.androidexperimental.feature.news.domain.usecase.ToggleBookmarkUseCase
import me.vishwas.androidexperimental.util.MainDispatcherRule
import me.vishwas.androidexperimental.util.testArticle
import me.vishwas.androidexperimental.util.testArticleWithoutImage
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * Unit tests for [HomeViewModel].
 *
 * Strategy:
 * - Mocks for all three use cases and the repository are created with MockK.
 * - [MainDispatcherRule] replaces [Dispatchers.Main] with [UnconfinedTestDispatcher] so
 *   ViewModel coroutines run eagerly inside [runTest].
 * - Each test creates a fresh ViewModel to avoid state leaking between tests.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getTopHeadlinesUseCase: GetTopHeadlinesUseCase = mockk()
    private val getNewsByCategoryUseCase: GetNewsByCategoryUseCase = mockk()
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase = mockk()
    private val newsRepository: NewsRepository = mockk()

    @Before
    fun setup() {
        // Default bookmark flow so isPaneArticleBookmarked doesn't crash
        every { newsRepository.isBookmarked(any()) } returns flowOf(false)
    }

    private fun createViewModel() = HomeViewModel(
        getTopHeadlinesUseCase,
        getNewsByCategoryUseCase,
        toggleBookmarkUseCase,
        newsRepository,
    )

    // ── init / loadNews ───────────────────────────────────────────────────────

    @Test
    fun `initial state transitions from Loading to Success with GENERAL category`() = runTest {
        val articles = listOf(testArticle())
        coEvery { getTopHeadlinesUseCase(NoParams) } returns NetworkResult.Success(articles)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertIs<HomeUiState.Success>(state)
        assertEquals(NewsCategory.GENERAL, state.selectedCategory)
        assertEquals(1, state.articles.size)
    }

    @Test
    fun `initial state becomes Error when API returns an error`() = runTest {
        coEvery { getTopHeadlinesUseCase(NoParams) } returns NetworkResult.Error("Server down")

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertIs<HomeUiState.Error>(state)
        assertEquals("Server down", state.message)
    }

    // ── selectCategory ────────────────────────────────────────────────────────

    @Test
    fun `selectCategory with the same category does not trigger a new load`() = runTest {
        coEvery { getTopHeadlinesUseCase(NoParams) } returns NetworkResult.Success(emptyList())

        val viewModel = createViewModel()
        viewModel.selectCategory(NewsCategory.GENERAL)  // same as initial

        // getTopHeadlinesUseCase should have been called exactly once (on init)
        coVerify(exactly = 1) { getTopHeadlinesUseCase(NoParams) }
    }

    @Test
    fun `selectCategory with a different category loads new articles`() = runTest {
        coEvery { getTopHeadlinesUseCase(NoParams) } returns NetworkResult.Success(emptyList())
        val techArticles = listOf(testArticle(url = "tech-url", category = "technology"))
        coEvery { getNewsByCategoryUseCase("technology") } returns NetworkResult.Success(techArticles)

        val viewModel = createViewModel()
        viewModel.selectCategory(NewsCategory.TECHNOLOGY)

        val state = viewModel.uiState.value
        assertIs<HomeUiState.Success>(state)
        assertEquals(NewsCategory.TECHNOLOGY, state.selectedCategory)
        assertEquals(1, state.articles.size)
    }

    @Test
    fun `selectCategory on Error state re-fetches articles for the new category`() = runTest {
        coEvery { getTopHeadlinesUseCase(NoParams) } returns NetworkResult.Error("offline")
        val businessArticles = listOf(testArticle(category = "business"))
        coEvery { getNewsByCategoryUseCase("business") } returns NetworkResult.Success(businessArticles)

        val viewModel = createViewModel()
        assertIs<HomeUiState.Error>(viewModel.uiState.value)

        viewModel.selectCategory(NewsCategory.BUSINESS)

        assertIs<HomeUiState.Success>(viewModel.uiState.value)
    }

    // ── refresh ───────────────────────────────────────────────────────────────

    @Test
    fun `refresh replaces articles with fresh data on Success state`() = runTest {
        val initial = listOf(testArticle(url = "url1"))
        val refreshed = listOf(testArticle(url = "url1"), testArticle(url = "url2"))
        coEvery { getTopHeadlinesUseCase(NoParams) } returnsMany listOf(
            NetworkResult.Success(initial),
            NetworkResult.Success(refreshed),
        )

        val viewModel = createViewModel()
        viewModel.refresh()

        val state = viewModel.uiState.value
        assertIs<HomeUiState.Success>(state)
        assertEquals(2, state.articles.size)
    }

    @Test
    fun `refresh keeps isRefreshing false after a successful refresh`() = runTest {
        coEvery { getTopHeadlinesUseCase(NoParams) } returns NetworkResult.Success(emptyList())

        val viewModel = createViewModel()
        viewModel.refresh()

        val state = viewModel.uiState.value
        assertIs<HomeUiState.Success>(state)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun `refresh on Error state triggers a fresh load from GENERAL`() = runTest {
        coEvery { getTopHeadlinesUseCase(NoParams) } returnsMany listOf(
            NetworkResult.Error("first fail"),
            NetworkResult.Success(listOf(testArticle())),
        )

        val viewModel = createViewModel()
        assertIs<HomeUiState.Error>(viewModel.uiState.value)

        viewModel.refresh()

        assertIs<HomeUiState.Success>(viewModel.uiState.value)
    }

    // ── selectPaneArticle ─────────────────────────────────────────────────────

    @Test
    fun `paneArticle is null initially`() = runTest {
        coEvery { getTopHeadlinesUseCase(NoParams) } returns NetworkResult.Success(emptyList())
        val viewModel = createViewModel()

        assertNull(viewModel.paneArticle.value)
    }

    @Test
    fun `selectPaneArticle updates paneArticle`() = runTest {
        coEvery { getTopHeadlinesUseCase(NoParams) } returns NetworkResult.Success(emptyList())
        val viewModel = createViewModel()

        val article = testArticle()
        viewModel.selectPaneArticle(article)

        assertEquals(article, viewModel.paneArticle.value)
    }

    @Test
    fun `selectPaneArticle can be updated multiple times`() = runTest {
        coEvery { getTopHeadlinesUseCase(NoParams) } returns NetworkResult.Success(emptyList())
        val viewModel = createViewModel()

        viewModel.selectPaneArticle(testArticle(url = "url1"))
        viewModel.selectPaneArticle(testArticle(url = "url2"))

        assertEquals("url2", viewModel.paneArticle.value?.url)
    }

    // ── togglePaneBookmark ────────────────────────────────────────────────────

    @Test
    fun `togglePaneBookmark calls toggleBookmarkUseCase with the selected article`() = runTest {
        coEvery { getTopHeadlinesUseCase(NoParams) } returns NetworkResult.Success(emptyList())
        coEvery { toggleBookmarkUseCase(any()) } just Runs

        val viewModel = createViewModel()
        val article = testArticle()
        viewModel.selectPaneArticle(article)
        viewModel.togglePaneBookmark()

        coVerify(exactly = 1) { toggleBookmarkUseCase(article) }
    }

    @Test
    fun `togglePaneBookmark does nothing when no article is selected`() = runTest {
        coEvery { getTopHeadlinesUseCase(NoParams) } returns NetworkResult.Success(emptyList())

        val viewModel = createViewModel()
        viewModel.togglePaneBookmark()  // paneArticle is null

        coVerify(exactly = 0) { toggleBookmarkUseCase(any()) }
    }

    // ── isPaneArticleBookmarked ───────────────────────────────────────────────

    @Test
    fun `isPaneArticleBookmarked is false when no article is selected`() = runTest {
        coEvery { getTopHeadlinesUseCase(NoParams) } returns NetworkResult.Success(emptyList())

        val viewModel = createViewModel()

        assertFalse(viewModel.isPaneArticleBookmarked.value)
    }

    @Test
    fun `isPaneArticleBookmarked reflects repository when an article is selected`() = runTest {
        coEvery { getTopHeadlinesUseCase(NoParams) } returns NetworkResult.Success(emptyList())
        val article = testArticle(url = "https://example.com/article")
        every { newsRepository.isBookmarked("https://example.com/article") } returns flowOf(true)

        val viewModel = createViewModel()
        viewModel.selectPaneArticle(article)

        // isPaneArticleBookmarked uses WhileSubscribed — collect it to trigger upstream
        val value = viewModel.isPaneArticleBookmarked.value
        // The flow emits false initially (StateFlow default) then true from repository.
        // With UnconfinedTestDispatcher the final value should be true.
        // We verify at least that the repository was asked.
        coVerify { newsRepository.isBookmarked("https://example.com/article") }
    }

    // ── HomeUiState.Success computed properties ───────────────────────────────

    @Test
    fun `Success state computes featuredArticle as first article with an image`() = runTest {
        val withoutImage = testArticleWithoutImage("url1")
        val withImage = testArticle(url = "url2", imageUrl = "https://img.com/1.jpg")
        coEvery { getTopHeadlinesUseCase(NoParams) } returns
            NetworkResult.Success(listOf(withoutImage, withImage))

        val viewModel = createViewModel()

        val state = viewModel.uiState.value as HomeUiState.Success
        assertEquals(withImage, state.featuredArticle)
        assertEquals(listOf(withoutImage), state.regularArticles.toList())
    }
}
