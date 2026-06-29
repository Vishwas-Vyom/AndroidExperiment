package me.vishwas.androidexperimental.snapshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit4.runners.AndroidJUnit4
import io.github.takahirom.roborazzi.captureRoboImage
import kotlinx.collections.immutable.toImmutableList
import me.vishwas.androidexperimental.feature.news.presentation.bookmarks.BookmarksContent
import me.vishwas.androidexperimental.feature.news.presentation.bookmarks.BookmarksUiState
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme
import me.vishwas.androidexperimental.util.testArticle
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Snapshot tests for [BookmarksContent] across all three UI states:
 * Loading, Empty, and Success.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33])
class BookmarksContentSnapshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bookmarksContent_loadingState() {
        composeTestRule.setContent {
            AndroidExperimentalTheme {
                BookmarksContent(
                    uiState = BookmarksUiState.Loading,
                    onArticleClick = {},
                    onRemoveBookmark = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun bookmarksContent_emptyState() {
        composeTestRule.setContent {
            AndroidExperimentalTheme {
                BookmarksContent(
                    uiState = BookmarksUiState.Empty,
                    onArticleClick = {},
                    onRemoveBookmark = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun bookmarksContent_successState_singleArticle() {
        val articles = listOf(
            testArticle(title = "Saved Article", imageUrl = null),
        ).toImmutableList()

        composeTestRule.setContent {
            AndroidExperimentalTheme {
                BookmarksContent(
                    uiState = BookmarksUiState.Success(articles),
                    onArticleClick = {},
                    onRemoveBookmark = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun bookmarksContent_successState_multipleArticles() {
        val articles = List(4) { i ->
            testArticle(url = "url$i", title = "Saved Article #${i + 1}", imageUrl = null)
        }.toImmutableList()

        composeTestRule.setContent {
            AndroidExperimentalTheme {
                BookmarksContent(
                    uiState = BookmarksUiState.Success(articles),
                    onArticleClick = {},
                    onRemoveBookmark = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
