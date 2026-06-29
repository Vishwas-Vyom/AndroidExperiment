package me.vishwas.androidexperimental.snapshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit4.runners.AndroidJUnit4
import io.github.takahirom.roborazzi.captureRoboImage
import kotlinx.collections.immutable.toImmutableList
import me.vishwas.androidexperimental.feature.news.presentation.search.SearchContent
import me.vishwas.androidexperimental.feature.news.presentation.search.SearchUiState
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme
import me.vishwas.androidexperimental.util.testArticle
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Snapshot tests for [SearchContent] across all four UI states.
 *
 * Run `./gradlew recordRoborazziDebug` to generate/update golden images.
 * Run `./gradlew verifyRoborazziDebug` to diff against existing goldens.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33])
class SearchContentSnapshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun searchContent_idleState_emptyQuery() {
        composeTestRule.setContent {
            AndroidExperimentalTheme {
                SearchContent(
                    uiState = SearchUiState.Idle,
                    query = "",
                    onQueryChange = {},
                    onClear = {},
                    onArticleClick = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun searchContent_loadingState() {
        composeTestRule.setContent {
            AndroidExperimentalTheme {
                SearchContent(
                    uiState = SearchUiState.Loading("AI"),
                    query = "AI",
                    onQueryChange = {},
                    onClear = {},
                    onArticleClick = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun searchContent_successState_withResults() {
        val articles = List(3) { i ->
            testArticle(url = "url$i", title = "Search Result #${i + 1}", imageUrl = null)
        }.toImmutableList()

        composeTestRule.setContent {
            AndroidExperimentalTheme {
                SearchContent(
                    uiState = SearchUiState.Success(articles, "AI"),
                    query = "AI",
                    onQueryChange = {},
                    onClear = {},
                    onArticleClick = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun searchContent_successState_noResults() {
        composeTestRule.setContent {
            AndroidExperimentalTheme {
                SearchContent(
                    uiState = SearchUiState.Success(emptyList<me.vishwas.androidexperimental.feature.news.domain.model.Article>().toImmutableList(), "xyz"),
                    query = "xyz",
                    onQueryChange = {},
                    onClear = {},
                    onArticleClick = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun searchContent_errorState() {
        composeTestRule.setContent {
            AndroidExperimentalTheme {
                SearchContent(
                    uiState = SearchUiState.Error("No internet connection"),
                    query = "AI",
                    onQueryChange = {},
                    onClear = {},
                    onArticleClick = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun searchContent_withActiveQuery_clearButtonVisible() {
        composeTestRule.setContent {
            AndroidExperimentalTheme {
                SearchContent(
                    uiState = SearchUiState.Idle,
                    query = "Climate",
                    onQueryChange = {},
                    onClear = {},
                    onArticleClick = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
