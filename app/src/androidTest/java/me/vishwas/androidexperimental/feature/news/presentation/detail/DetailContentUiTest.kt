package me.vishwas.androidexperimental.feature.news.presentation.detail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit4.runners.AndroidJUnit4
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme
import me.vishwas.androidexperimental.util.testArticle
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue

/**
 * Compose UI tests for [DetailContent].
 *
 * Tests verify that article data is rendered correctly and that toolbar actions
 * (back, bookmark toggle) fire the expected callbacks.
 */
@RunWith(AndroidJUnit4::class)
class DetailContentUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleArticle = testArticle(
        title = "James Webb Telescope Finds Water on New Exoplanet",
        source = "Space.com",
        author = "Dr. Alice Kim",
        publishedAt = "2024-06-15T14:30:00Z",
        content = "Astronomers using the James Webb Space Telescope have detected the spectral signature of water.",
        imageUrl = null,
    )

    private fun setContent(
        isBookmarked: Boolean = false,
        onNavigateBack: () -> Unit = {},
        onToggleBookmark: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            AndroidExperimentalTheme {
                DetailContent(
                    article = sampleArticle,
                    isBookmarked = isBookmarked,
                    onNavigateBack = onNavigateBack,
                    onToggleBookmark = onToggleBookmark,
                )
            }
        }
    }

    // ── article data rendered ─────────────────────────────────────────────────

    @Test
    fun article_title_is_displayed() {
        setContent()
        composeTestRule
            .onNodeWithText("James Webb Telescope Finds Water on New Exoplanet")
            .assertIsDisplayed()
    }

    @Test
    fun article_source_badge_shows_uppercase_source() {
        setContent()
        composeTestRule.onNodeWithText("SPACE.COM").assertIsDisplayed()
    }

    @Test
    fun article_author_is_displayed_when_present() {
        setContent()
        composeTestRule.onNodeWithText("Dr. Alice Kim").assertIsDisplayed()
    }

    @Test
    fun article_body_content_is_displayed() {
        setContent()
        composeTestRule
            .onNodeWithText(
                "Astronomers using the James Webb Space Telescope have detected the spectral signature of water.",
                substring = true,
            )
            .assertIsDisplayed()
    }

    @Test
    fun read_full_article_button_is_displayed() {
        setContent()
        composeTestRule.onNodeWithText("Read Full Article").assertIsDisplayed()
    }

    // ── bookmark toggle ───────────────────────────────────────────────────────

    @Test
    fun bookmark_button_shows_add_bookmark_description_when_not_bookmarked() {
        setContent(isBookmarked = false)
        composeTestRule.onNodeWithContentDescription("Add bookmark").assertIsDisplayed()
    }

    @Test
    fun bookmark_button_shows_remove_bookmark_description_when_bookmarked() {
        setContent(isBookmarked = true)
        composeTestRule.onNodeWithContentDescription("Remove bookmark").assertIsDisplayed()
    }

    @Test
    fun tapping_bookmark_button_invokes_onToggleBookmark() {
        var toggled = false
        setContent(onToggleBookmark = { toggled = true })

        composeTestRule.onNodeWithContentDescription("Add bookmark").performClick()

        assertTrue(toggled)
    }

    // ── navigation ────────────────────────────────────────────────────────────

    @Test
    fun tapping_back_button_invokes_onNavigateBack() {
        var backCalled = false
        setContent(onNavigateBack = { backCalled = true })

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assertTrue(backCalled)
    }
}
