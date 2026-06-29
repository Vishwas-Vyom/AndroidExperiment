package me.vishwas.androidexperimental.feature.news.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.vishwas.androidexperimental.core.domain.NoParams
import me.vishwas.androidexperimental.core.network.NetworkResult
import me.vishwas.androidexperimental.feature.news.domain.repository.NewsRepository
import me.vishwas.androidexperimental.util.testArticle
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Unit tests for all five use cases in the news feature.
 *
 * Each use case is a thin wrapper over [NewsRepository]; these tests verify:
 * - The correct repository method is called with the correct parameters
 * - The result is passed back to the caller unchanged
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UseCaseTests {

    private val repository: NewsRepository = mockk()

    // ── GetTopHeadlinesUseCase ────────────────────────────────────────────────

    @Test
    fun `GetTopHeadlinesUseCase calls repository getTopHeadlines`() = runTest {
        val articles = listOf(testArticle())
        coEvery { repository.getTopHeadlines(any()) } returns NetworkResult.Success(articles)

        val result = GetTopHeadlinesUseCase(repository)(NoParams)

        assertIs<NetworkResult.Success<*>>(result)
        coVerify(exactly = 1) { repository.getTopHeadlines(any()) }
    }

    @Test
    fun `GetTopHeadlinesUseCase forwards Error from repository`() = runTest {
        coEvery { repository.getTopHeadlines(any()) } returns NetworkResult.Error("timeout")

        val result = GetTopHeadlinesUseCase(repository)(NoParams)

        assertIs<NetworkResult.Error>(result)
        assertEquals("timeout", result.message)
    }

    // ── GetNewsByCategoryUseCase ──────────────────────────────────────────────

    @Test
    fun `GetNewsByCategoryUseCase passes category string to repository`() = runTest {
        coEvery { repository.getNewsByCategory("technology", any()) } returns
            NetworkResult.Success(listOf(testArticle()))

        GetNewsByCategoryUseCase(repository)("technology")

        coVerify(exactly = 1) { repository.getNewsByCategory("technology", any()) }
    }

    @Test
    fun `GetNewsByCategoryUseCase forwards Success from repository`() = runTest {
        val techArticles = listOf(testArticle(category = "technology"))
        coEvery { repository.getNewsByCategory(any(), any()) } returns
            NetworkResult.Success(techArticles)

        val result = GetNewsByCategoryUseCase(repository)("technology")

        assertIs<NetworkResult.Success<*>>(result)
        assertEquals(1, (result as NetworkResult.Success).data.size)
    }

    // ── SearchNewsUseCase ─────────────────────────────────────────────────────

    @Test
    fun `SearchNewsUseCase passes query to repository searchNews`() = runTest {
        coEvery { repository.searchNews("AI") } returns NetworkResult.Success(emptyList())

        SearchNewsUseCase(repository)("AI")

        coVerify(exactly = 1) { repository.searchNews("AI") }
    }

    @Test
    fun `SearchNewsUseCase returns articles from repository`() = runTest {
        val articles = listOf(testArticle(title = "AI article"))
        coEvery { repository.searchNews(any()) } returns NetworkResult.Success(articles)

        val result = SearchNewsUseCase(repository)("AI")

        assertIs<NetworkResult.Success<*>>(result)
    }

    // ── GetBookmarksUseCase ───────────────────────────────────────────────────

    @Test
    fun `GetBookmarksUseCase returns flow from repository getBookmarks`() = runTest {
        val articles = listOf(testArticle())
        every { repository.getBookmarks() } returns flowOf(articles)

        val result = GetBookmarksUseCase(repository)().first()

        assertEquals(articles, result)
        coVerify { repository.getBookmarks() }
    }

    @Test
    fun `GetBookmarksUseCase returns empty flow when no bookmarks exist`() = runTest {
        every { repository.getBookmarks() } returns flowOf(emptyList())

        val result = GetBookmarksUseCase(repository)().first()

        assertTrue(result.isEmpty())
    }

    // ── ToggleBookmarkUseCase ─────────────────────────────────────────────────

    @Test
    fun `ToggleBookmarkUseCase delegates to repository toggleBookmark`() = runTest {
        val article = testArticle()
        coEvery { repository.toggleBookmark(article) } returns Unit

        ToggleBookmarkUseCase(repository)(article)

        coVerify(exactly = 1) { repository.toggleBookmark(article) }
    }

    @Test
    fun `ToggleBookmarkUseCase can be called for different articles`() = runTest {
        val a1 = testArticle(url = "url1")
        val a2 = testArticle(url = "url2")
        coEvery { repository.toggleBookmark(any()) } returns Unit

        val useCase = ToggleBookmarkUseCase(repository)
        useCase(a1)
        useCase(a2)

        coVerify(exactly = 1) { repository.toggleBookmark(a1) }
        coVerify(exactly = 1) { repository.toggleBookmark(a2) }
    }
}
