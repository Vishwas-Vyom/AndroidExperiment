package me.vishwas.androidexperimental.feature.notes.presentation

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit4.runners.AndroidJUnit4
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI tests for [NotesEmptyState] — verifies both visible text and
 * the accessibility semantics (heading) added in the Accessibility section.
 */
@RunWith(AndroidJUnit4::class)
class NotesEmptyStateUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent() {
        composeTestRule.setContent {
            AndroidExperimentalTheme {
                NotesEmptyState()
            }
        }
    }

    @Test
    fun showsTitle() {
        setContent()
        composeTestRule.onNodeWithText("No notes yet").assertIsDisplayed()
    }

    @Test
    fun showsSubtitle() {
        setContent()
        composeTestRule.onNodeWithText("Notes you create will show up here.").assertIsDisplayed()
    }

    @Test
    fun title_isMarkedAsAccessibilityHeading() {
        setContent()

        val isHeading = SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)
        composeTestRule.onNodeWithText("No notes yet").assert(isHeading)
    }
}
