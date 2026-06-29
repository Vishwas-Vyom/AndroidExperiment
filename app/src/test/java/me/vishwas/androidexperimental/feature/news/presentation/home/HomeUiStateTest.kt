package me.vishwas.androidexperimental.feature.news.presentation.home

import kotlinx.collections.immutable.toImmutableList
import me.vishwas.androidexperimental.feature.news.domain.model.NewsCategory
import me.vishwas.androidexperimental.util.testArticle
import me.vishwas.androidexperimental.util.testArticleWithoutImage
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Tests for the computed properties inside [HomeUiState.Success]:
 * - [HomeUiState.Success.featuredArticle]: first article that has an image
 * - [HomeUiState.Success.regularArticles]: all articles except the featured one
 */
class HomeUiStateTest {

    private fun successState(
        articles: List<me.vishwas.androidexperimental.feature.news.domain.model.Article>,
        category: NewsCategory = NewsCategory.GENERAL,
        isRefreshing: Boolean = false,
    ) = HomeUiState.Success(articles.toImmutableList(), category, isRefreshing)

    // ── featuredArticle ───────────────────────────────────────────────────────

    @Test
    fun `featuredArticle is the first article that has an imageUrl`() {
        val noImage = testArticleWithoutImage("url1")
        val withImage = testArticle(url = "url2", imageUrl = "https://img.example.com/a.jpg")
        val state = successState(listOf(noImage, withImage))

        assertEquals(withImage, state.featuredArticle)
    }

    @Test
    fun `featuredArticle is null when no articles have an imageUrl`() {
        val articles = listOf(
            testArticleWithoutImage("url1"),
            testArticleWithoutImage("url2"),
        )
        val state = successState(articles)

        assertNull(state.featuredArticle)
    }

    @Test
    fun `featuredArticle is null for an empty article list`() {
        val state = successState(emptyList())
        assertNull(state.featuredArticle)
    }

    @Test
    fun `featuredArticle picks the first image article even if later articles also have images`() {
        val first = testArticle(url = "url1", imageUrl = "https://img.example.com/1.jpg")
        val second = testArticle(url = "url2", imageUrl = "https://img.example.com/2.jpg")
        val state = successState(listOf(first, second))

        assertEquals(first, state.featuredArticle)
    }

    // ── regularArticles ───────────────────────────────────────────────────────

    @Test
    fun `regularArticles excludes the featuredArticle`() {
        val featured = testArticle(url = "url1", imageUrl = "https://img.example.com/1.jpg")
        val regular1 = testArticleWithoutImage("url2")
        val regular2 = testArticleWithoutImage("url3")
        val state = successState(listOf(featured, regular1, regular2))

        assertEquals(listOf(regular1, regular2), state.regularArticles.toList())
    }

    @Test
    fun `regularArticles is empty when there is only one article and it is featured`() {
        val featured = testArticle(url = "url1", imageUrl = "https://img.example.com/1.jpg")
        val state = successState(listOf(featured))

        assertTrue(state.regularArticles.isEmpty())
    }

    @Test
    fun `regularArticles contains all articles when none have an image`() {
        val a1 = testArticleWithoutImage("url1")
        val a2 = testArticleWithoutImage("url2")
        val state = successState(listOf(a1, a2))

        assertEquals(listOf(a1, a2), state.regularArticles.toList())
    }

    // ── isRefreshing ──────────────────────────────────────────────────────────

    @Test
    fun `isRefreshing defaults to false`() {
        val state = successState(emptyList())
        assertFalse(state.isRefreshing)
    }

    @Test
    fun `copy preserves isRefreshing flag`() {
        val state = successState(emptyList()).copy(isRefreshing = true)
        assertTrue(state.isRefreshing)
    }
}
