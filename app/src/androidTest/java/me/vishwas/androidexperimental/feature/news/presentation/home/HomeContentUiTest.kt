package me.vishwas.androidexperimental.feature.news.presentation.home

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit4.runners.AndroidJUnit4
import kotlinx.collections.immutable.toImmutableList
import me.vishwas.androidexperimental.feature.news.domain.model.Article
import me.vishwas.androidexperimental.feature.news.domain.model.NewsCategory
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme
import me.vishwas.androidexperimental.util.testArticle
import me.vishwas.androidexperimental.util.testArticleWithoutImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Compose UI tests for [HomeContent].
 *
 * These tests run on a real device or emulator. They interact with the composable by finding
 * nodes in the semantic tree and asserting on their presence, text, and selection state.
 */
@RunWith(AndroidJUnit4::class)
class HomeContentUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val categories = NewsCategory.entries.toList()

    private fun setContent(
        uiState: HomeUiState,
        onCategorySelected: (NewsCategory) -> Unit = {},
        onRefresh: () -> Unit = {},
        onArticleClick: (Article) -> Unit = {},
    ) {
        composeTestRule.setContent {
            AndroidExperimentalTheme {
                HomeContent(
                    uiState = uiState,
                    categories = categories,
                    onCategorySelected = onCategorySelected,
                    onRefresh = onRefresh,
                    onArticleClick = onArticleClick,
                )
            }
        }
    }

    // ── TopBar ────────────────────────────────────────────────────────────────

    @Test
    fun topBar_shows_BREAKING_and_NewsFeed_labels() {
        setContent(HomeUiState.Loading)

        composeTestRule.onNodeWithText("BREAKING").assertIsDisplayed()
        composeTestRule.onNodeWithText("News Feed").assertIsDisplayed()
    }

    // ── Loading state ─────────────────────────────────────────────────────────

    @Test
    fun loadingState_shows_circular_progress_indicator() {
        setContent(HomeUiState.Loading)

        composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    // ── Error state ───────────────────────────────────────────────────────────

    @Test
    fun errorState_shows_error_message_and_retry_button() {
        setContent(HomeUiState.Error("Could not load news"))

        composeTestRule.onNodeWithText("Could not load news").assertIsDisplayed()
        composeTestRule.onNodeWithText("Try Again").assertIsDisplayed()
    }

    @Test
    fun errorState_retry_button_invokes_onRefresh_callback() {
        var refreshCalled = false
        setContent(
            uiState = HomeUiState.Error("Error"),
            onRefresh = { refreshCalled = true },
        )

        composeTestRule.onNodeWithText("Try Again").performClick()

        assert(refreshCalled) { "Expected onRefresh to be called when 'Try Again' is tapped" }
    }

    // ── Success state — category chips ────────────────────────────────────────

    @Test
    fun successState_shows_all_category_chips() {
        val state = successState()
        setContent(state)

        NewsCategory.entries.forEach { category ->
            composeTestRule.onNodeWithText(category.displayName).assertIsDisplayed()
        }
    }

    @Test
    fun successState_selected_category_chip_is_marked_selected() {
        val state = successState(selectedCategory = NewsCategory.TECHNOLOGY)
        setContent(state)

        composeTestRule.onNodeWithText(NewsCategory.TECHNOLOGY.displayName).assertIsSelected()
    }

    @Test
    fun successState_tapping_a_chip_invokes_onCategorySelected_with_correct_category() {
        var selected: NewsCategory? = null
        val state = successState()
        setContent(state, onCategorySelected = { selected = it })

        composeTestRule.onNodeWithText(NewsCategory.BUSINESS.displayName).performClick()

        assertEquals(NewsCategory.BUSINESS, selected)
    }

    // ── Success state — article list ──────────────────────────────────────────

    @Test
    fun successState_shows_article_titles() {
        val articles = listOf(
            testArticleWithoutImage("url1").copy(title = "Article Alpha"),
            testArticleWithoutImage("url2").copy(title = "Article Beta"),
        )
        val state = successState(articles = articles)
        setContent(state)

        composeTestRule.onNodeWithText("Article Alpha").assertIsDisplayed()
        composeTestRule.onNodeWithText("Article Beta").assertIsDisplayed()
    }

    @Test
    fun successState_tapping_an_article_invokes_onArticleClick_with_correct_article() {
        val article = testArticleWithoutImage("url1").copy(title = "Clickable Article")
        val state = successState(articles = listOf(article))
        var clicked: Article? = null
        setContent(state, onArticleClick = { clicked = it })

        composeTestRule.onNodeWithText("Clickable Article").performClick()

        assertNotNull(clicked)
        assertEquals("url1", clicked?.url)
    }

    @Test
    fun successState_shows_Latest_News_section_header_when_regular_articles_present() {
        // Featured article has image; regular articles don't
        val featured = testArticle(url = "url0", imageUrl = "https://img.example.com/1.jpg", title = "Featured")
        val regular = testArticleWithoutImage("url1").copy(title = "Regular Article")
        val state = successState(articles = listOf(featured, regular))
        setContent(state)

        composeTestRule.onNodeWithText("Latest News").assertIsDisplayed()
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun successState(
        articles: List<Article> = listOf(testArticleWithoutImage()),
        selectedCategory: NewsCategory = NewsCategory.GENERAL,
    ) = HomeUiState.Success(articles.toImmutableList(), selectedCategory)
}
