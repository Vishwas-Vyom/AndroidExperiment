package me.vishwas.androidexperimental.snapshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit4.runners.AndroidJUnit4
import io.github.takahirom.roborazzi.captureRoboImage
import me.vishwas.androidexperimental.feature.news.presentation.detail.DetailContent
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme
import me.vishwas.androidexperimental.util.testArticle
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Snapshot tests for [DetailContent] showing bookmarked and un-bookmarked states.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33])
class DetailContentSnapshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun detailContent_notBookmarked_withContent() {
        composeTestRule.setContent {
            AndroidExperimentalTheme {
                DetailContent(
                    article = testArticle(
                        title = "Scientists Discover New Exoplanet",
                        source = "Space.com",
                        author = "Dr. Jane Smith",
                        publishedAt = "2024-06-15T14:30:00Z",
                        content = "Astronomers have identified a new exoplanet in the habitable zone " +
                            "of a nearby star system, raising hopes for potential life-supporting conditions.",
                        imageUrl = null,
                    ),
                    isBookmarked = false,
                    onNavigateBack = {},
                    onToggleBookmark = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun detailContent_bookmarked() {
        composeTestRule.setContent {
            AndroidExperimentalTheme {
                DetailContent(
                    article = testArticle(imageUrl = null),
                    isBookmarked = true,
                    onNavigateBack = {},
                    onToggleBookmark = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun detailContent_noAuthor_noContent() {
        composeTestRule.setContent {
            AndroidExperimentalTheme {
                DetailContent(
                    article = testArticle(
                        author = null,
                        content = null,
                        description = "A short description is all we have.",
                        imageUrl = null,
                    ),
                    isBookmarked = false,
                    onNavigateBack = {},
                    onToggleBookmark = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
