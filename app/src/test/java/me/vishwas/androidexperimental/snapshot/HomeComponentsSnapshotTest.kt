package me.vishwas.androidexperimental.snapshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.runner.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import me.vishwas.androidexperimental.feature.news.domain.model.NewsCategory
import me.vishwas.androidexperimental.feature.news.presentation.home.CategoryChipsRow
import me.vishwas.androidexperimental.feature.news.presentation.home.ErrorState
import me.vishwas.androidexperimental.feature.news.presentation.home.FeaturedArticleCard
import me.vishwas.androidexperimental.feature.news.presentation.home.NewsArticleCard
import me.vishwas.androidexperimental.feature.news.presentation.home.NewsLoadingIndicator
import me.vishwas.androidexperimental.feature.news.presentation.home.SectionHeader
import me.vishwas.androidexperimental.feature.news.presentation.home.SourceBadge
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme
import me.vishwas.androidexperimental.util.testArticle
import me.vishwas.androidexperimental.util.testArticleWithoutImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Snapshot tests for individual composables in [HomeComponents.kt].
 *
 * Roborazzi renders each composable via Robolectric (JVM, no device needed) and saves a PNG
 * golden image the first time the test runs. On subsequent runs the PNG is compared pixel-by-pixel.
 *
 * To update goldens after an intentional UI change:
 *   ./gradlew recordRoborazziDebug
 *
 * To verify against existing goldens:
 *   ./gradlew verifyRoborazziDebug
 *
 * Goldens are stored in: app/src/test/snapshots/images/
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33])
class HomeComponentsSnapshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun newsArticleCard_withImage() {
        composeTestRule.setContent {
            AndroidExperimentalTheme {
                NewsArticleCard(
                    article = testArticle(
                        title = "Jetpack Compose Reaches 1.0",
                        source = "Android Developers",
                        imageUrl = null, // Coil won't load in Robolectric
                    ),
                    onClick = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun newsArticleCard_withoutImage() {
        composeTestRule.setContent {
            AndroidExperimentalTheme {
                NewsArticleCard(
                    article = testArticleWithoutImage(),
                    onClick = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun featuredArticleCard_withoutNetworkImage() {
        composeTestRule.setContent {
            AndroidExperimentalTheme {
                FeaturedArticleCard(
                    article = testArticle(
                        title = "Major Climate Agreement Reached at COP Summit",
                        source = "Reuters",
                        imageUrl = null,
                    ),
                    onClick = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun categoryChipsRow_generalSelected() {
        val categories = NewsCategory.entries.toList()
        composeTestRule.setContent {
            AndroidExperimentalTheme {
                CategoryChipsRow(
                    categories = categories,
                    selectedCategory = NewsCategory.GENERAL,
                    onCategorySelected = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun categoryChipsRow_techSelected() {
        val categories = NewsCategory.entries.toList()
        composeTestRule.setContent {
            AndroidExperimentalTheme {
                CategoryChipsRow(
                    categories = categories,
                    selectedCategory = NewsCategory.TECHNOLOGY,
                    onCategorySelected = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun sectionHeader() {
        composeTestRule.setContent {
            AndroidExperimentalTheme {
                SectionHeader(title = "Latest News")
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun sourceBadge() {
        composeTestRule.setContent {
            AndroidExperimentalTheme {
                SourceBadge(source = "BBC News")
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun newsLoadingIndicator() {
        composeTestRule.setContent {
            AndroidExperimentalTheme {
                NewsLoadingIndicator()
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun errorState() {
        composeTestRule.setContent {
            AndroidExperimentalTheme {
                ErrorState(
                    message = "Unable to load news. Check your connection.",
                    onRetry = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
