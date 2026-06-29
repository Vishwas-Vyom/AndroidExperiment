package me.vishwas.androidexperimental.feature.news.presentation.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit4.runners.AndroidJUnit4
import kotlinx.collections.immutable.toImmutableList
import me.vishwas.androidexperimental.feature.news.domain.model.Article
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme
import me.vishwas.androidexperimental.util.testArticle
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Compose UI tests for [SearchContent].
 *
 * Covers all four states (Idle, Loading, Success, Error) and verifies that user interactions
 * fire the correct callbacks.
 */
@RunWith(AndroidJUnit4::class)
class SearchContentUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        uiState: SearchUiState,
        query: String = "",
        onQueryChange: (String) -> Unit = {},
        onClear: () -> Unit = {},
        onArticleClick: (Article) -> Unit = {},
    ) {
        composeTestRule.setContent {
            AndroidExperimentalTheme {
                SearchContent(
                    uiState = uiState,
                    query = query,
                    onQueryChange = onQueryChange,
                    onClear = onClear,
                    onArticleClick = onArticleClick,
                )
            }
        }
    }

    // ── Idle state ────────────────────────────────────────────────────────────

    @Test
    fun idleState_shows_search_hint_text() {
        setContent(SearchUiState.Idle)

        composeTestRule.onNodeWithText("Search for news").assertIsDisplayed()
    }

    @Test
    fun idleState_shows_example_keywords_hint() {
        setContent(SearchUiState.Idle)

        composeTestRule.onNodeWithText("Try \"AI\", \"Climate\", \"Sports\"...").assertIsDisplayed()
    }

    @Test
    fun idleState_shows_search_placeholder_in_text_field() {
        setContent(SearchUiState.Idle)

        composeTestRule.onNodeWithText("Search news, topics, events...").assertIsDisplayed()
    }

    // ── TextField interaction ─────────────────────────────────────────────────

    @Test
    fun typing_in_search_field_invokes_onQueryChange() {
        var capturedQuery = ""
        composeTestRule.setContent {
            AndroidExperimentalTheme {
                var query by remember { mutableStateOf("") }
                SearchContent(
                    uiState = SearchUiState.Idle,
                    query = query,
                    onQueryChange = { query = it; capturedQuery = it },
                    onClear = {},
                    onArticleClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Search news, topics, events...").performTextInput("AI")

        assertEquals("AI", capturedQuery)
    }

    @Test
    fun clear_button_is_visible_when_query_is_not_empty() {
        setContent(SearchUiState.Idle, query = "AI")

        // The clear icon has contentDescription "Clear"
        composeTestRule.onNodeWithContentDescription("Clear").assertIsDisplayed()
    }

    @Test
    fun clear_button_invokes_onClear_when_tapped() {
        var clearCalled = false
        setContent(SearchUiState.Idle, query = "AI", onClear = { clearCalled = true })

        composeTestRule.onNodeWithContentDescription("Clear").performClick()

        assertTrue(clearCalled)
    }

    // ── Loading state ─────────────────────────────────────────────────────────

    @Test
    fun loadingState_shows_circular_progress_indicator() {
        setContent(SearchUiState.Loading("AI"), query = "AI")

        composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    // ── Success state ─────────────────────────────────────────────────────────

    @Test
    fun successState_shows_result_count_and_query() {
        val articles = listOf(testArticle(url = "url1"), testArticle(url = "url2"))
        setContent(
            SearchUiState.Success(articles.toImmutableList(), "AI"),
            query = "AI",
        )

        composeTestRule.onNodeWithText("2 results for \"AI\"").assertIsDisplayed()
    }

    @Test
    fun successState_shows_article_titles() {
        val articles = listOf(
            testArticle(url = "url1", title = "AI Research Breakthrough"),
            testArticle(url = "url2", title = "Machine Learning Update"),
        )
        setContent(SearchUiState.Success(articles.toImmutableList(), "AI"), query = "AI")

        composeTestRule.onNodeWithText("AI Research Breakthrough").assertIsDisplayed()
        composeTestRule.onNodeWithText("Machine Learning Update").assertIsDisplayed()
    }

    @Test
    fun successState_tapping_article_invokes_onArticleClick() {
        val article = testArticle(url = "url1", title = "Clickable Result")
        var clicked: Article? = null
        setContent(
            SearchUiState.Success(listOf(article).toImmutableList(), "AI"),
            query = "AI",
            onArticleClick = { clicked = it },
        )

        composeTestRule.onNodeWithText("Clickable Result").performClick()

        assertNotNull(clicked)
        assertEquals("url1", clicked?.url)
    }

    // ── Error state ───────────────────────────────────────────────────────────

    @Test
    fun errorState_shows_error_message() {
        setContent(SearchUiState.Error("No internet connection"), query = "AI")

        composeTestRule.onNodeWithText("No internet connection").assertIsDisplayed()
    }

    @Test
    fun errorState_shows_retry_button() {
        setContent(SearchUiState.Error("Server error"), query = "AI")

        composeTestRule.onNodeWithText("Try Again").assertIsDisplayed()
    }
}
