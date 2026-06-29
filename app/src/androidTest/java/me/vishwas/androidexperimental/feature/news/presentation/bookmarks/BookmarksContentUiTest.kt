package me.vishwas.androidexperimental.feature.news.presentation.bookmarks

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit4.runners.AndroidJUnit4
import kotlinx.collections.immutable.toImmutableList
import me.vishwas.androidexperimental.feature.news.domain.model.Article
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme
import me.vishwas.androidexperimental.util.testArticle
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertNotNull

/**
 * Compose UI tests for [BookmarksContent] across Loading, Empty, and Success states.
 */
@RunWith(AndroidJUnit4::class)
class BookmarksContentUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        uiState: BookmarksUiState,
        onArticleClick: (Article) -> Unit = {},
        onRemoveBookmark: (Article) -> Unit = {},
    ) {
        composeTestRule.setContent {
            AndroidExperimentalTheme {
                BookmarksContent(
                    uiState = uiState,
                    onArticleClick = onArticleClick,
                    onRemoveBookmark = onRemoveBookmark,
                )
            }
        }
    }

    // ── TopBar ────────────────────────────────────────────────────────────────

    @Test
    fun topBar_shows_Bookmarks_title() {
        setContent(BookmarksUiState.Loading)
        composeTestRule.onNodeWithText("Bookmarks").assertIsDisplayed()
    }

    // ── Loading state ─────────────────────────────────────────────────────────

    @Test
    fun loadingState_shows_circular_progress_indicator() {
        setContent(BookmarksUiState.Loading)

        composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    // ── Empty state ───────────────────────────────────────────────────────────

    @Test
    fun emptyState_shows_no_bookmarks_message() {
        setContent(BookmarksUiState.Empty)

        composeTestRule.onNodeWithText("No bookmarks yet").assertIsDisplayed()
    }

    @Test
    fun emptyState_shows_call_to_action_text() {
        setContent(BookmarksUiState.Empty)

        composeTestRule.onNodeWithText("Save articles to read them later").assertIsDisplayed()
    }

    // ── Success state ─────────────────────────────────────────────────────────

    @Test
    fun successState_shows_all_bookmarked_article_titles() {
        val articles = listOf(
            testArticle(url = "url1", title = "Bookmarked Article One"),
            testArticle(url = "url2", title = "Bookmarked Article Two"),
        )
        setContent(BookmarksUiState.Success(articles.toImmutableList()))

        composeTestRule.onNodeWithText("Bookmarked Article One").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bookmarked Article Two").assertIsDisplayed()
    }

    @Test
    fun successState_tapping_an_article_fires_onArticleClick_with_correct_url() {
        val article = testArticle(url = "https://example.com/saved", title = "My Saved Article")
        var clicked: Article? = null
        setContent(
            BookmarksUiState.Success(listOf(article).toImmutableList()),
            onArticleClick = { clicked = it },
        )

        composeTestRule.onNodeWithText("My Saved Article").performClick()

        assertNotNull(clicked)
        assert(clicked?.url == "https://example.com/saved")
    }

    @Test
    fun successState_shows_source_text_for_each_article() {
        val article = testArticle(url = "url1", source = "TechCrunch", title = "Tech Article")
        setContent(BookmarksUiState.Success(listOf(article).toImmutableList()))

        // NewsArticleCard renders source.uppercase()
        composeTestRule.onNodeWithText("TECHCRUNCH").assertIsDisplayed()
    }
}
