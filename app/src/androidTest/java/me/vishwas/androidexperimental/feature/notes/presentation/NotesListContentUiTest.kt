package me.vishwas.androidexperimental.feature.notes.presentation

import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit4.runners.AndroidJUnit4
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Covers Topics 2 (Text), 3 (Button), 4 (Image), 5 (Modifier), 6 (State),
 * 7 (remember/search filtering), 12 (Stability/ImmutableList), and 21
 * (Forms validation) through the real NotesListContent + NoteComposeForm
 * composables — both are ViewModel-free, so no Hilt test setup is needed
 * here (NotesViewModel itself is exercised separately, Topic 34).
 */
@RunWith(AndroidJUnit4::class)
class NotesListContentUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleNotes = persistentListOf(
        NotePreview(1, "Grocery list", "Milk, eggs, bread, spinach, coffee beans."),
        NotePreview(2, "Trip ideas", "Kyoto in autumn.", photoUrl = "https://picsum.photos/200"),
    )

    // ── Topic 2: Text ────────────────────────────────────────────────────────

    @Test
    fun list_showsNoteTitleAndContentText() {
        setContent(notes = sampleNotes)

        composeTestRule.onNodeWithText("Grocery list").assertIsDisplayed()
        composeTestRule.onNodeWithText("Milk, eggs, bread, spinach, coffee beans.").assertIsDisplayed()
    }

    // ── Topic 5: Modifier (clickableNoteRow covers the whole row) ───────────

    @Test
    fun noteRow_isClickableAcrossItsFullBounds() {
        var clicked: NotePreview? = null
        setContent(notes = sampleNotes, onNoteClick = { clicked = it })

        composeTestRule.onNodeWithText("Grocery list").onParent().assertHasClickAction()
        composeTestRule.onNodeWithText("Grocery list").performClick()

        assert(clicked?.id == 1)
    }

    // ── Topic 6 + 3: State + Button (compose form) ──────────────────────────
    // ── Topic 21: Forms (validate-on-submit, not disabled-until-valid) ──────

    @Test
    fun composeForm_savingWithBlankTitle_showsValidationErrorAndDoesNotSave() {
        var saveCalled = false
        setContent(notes = persistentListOf(), isComposingNote = true, onSaveNote = { _, _ -> saveCalled = true })

        composeTestRule.onNodeWithText("Save").performClick()

        composeTestRule.onNodeWithText("Title is required").assertIsDisplayed()
        assert(!saveCalled)
    }

    @Test
    fun composeForm_typingAfterValidationError_clearsTheError() {
        setContent(notes = persistentListOf(), isComposingNote = true)
        composeTestRule.onNodeWithText("Save").performClick()
        composeTestRule.onNodeWithText("Title is required").assertIsDisplayed()

        composeTestRule.onNodeWithText("Title").performTextInput("Reading list")

        composeTestRule.onNodeWithText("Title is required").assertDoesNotExist()
    }

    @Test
    fun composeForm_typingTitleAndSaving_firesOnSaveNoteWithTypedValues() {
        var savedTitle: String? = null
        var savedContent: String? = null
        setContent(
            notes = persistentListOf(),
            isComposingNote = true,
            onSaveNote = { title, content -> savedTitle = title; savedContent = content },
        )

        composeTestRule.onNodeWithText("Title").performTextInput("Reading list")
        composeTestRule.onNodeWithText("Note").performTextInput("Atomic Habits")
        composeTestRule.onNodeWithText("Save").performClick()

        assert(savedTitle == "Reading list")
        assert(savedContent == "Atomic Habits")
    }

    @Test
    fun composeForm_cancelButton_firesOnDismissCompose() {
        var dismissed = false
        setContent(notes = persistentListOf(), isComposingNote = true, onDismissCompose = { dismissed = true })

        composeTestRule.onNodeWithText("Cancel").performClick()

        assert(dismissed)
    }

    // ── Empty state fallback ─────────────────────────────────────────────────

    @Test
    fun emptyNotesAndNotComposing_showsEmptyState() {
        setContent(notes = persistentListOf(), isComposingNote = false)

        composeTestRule.onNodeWithText("No notes yet").assertIsDisplayed()
    }

    // ── Topic 7: remember (search filtering) ─────────────────────────────────

    @Test
    fun searchQuery_filtersListToMatchingTitlesOnly() {
        setContent(notes = sampleNotes, query = "grocery")

        composeTestRule.onNodeWithText("Grocery list").assertIsDisplayed()
        composeTestRule.onNodeWithText("Trip ideas").assertDoesNotExist()
    }

    private fun setContent(
        notes: ImmutableList<NotePreview>,
        query: String = "",
        onQueryChange: (String) -> Unit = {},
        isComposingNote: Boolean = false,
        onDismissCompose: () -> Unit = {},
        onSaveNote: (String, String) -> Unit = { _, _ -> },
        onNoteClick: (NotePreview) -> Unit = {},
    ) {
        composeTestRule.setContent {
            AndroidExperimentalTheme {
                NotesListContent(
                    notes = notes,
                    query = query,
                    onQueryChange = onQueryChange,
                    isComposingNote = isComposingNote,
                    onDismissCompose = onDismissCompose,
                    onSaveNote = onSaveNote,
                    onNoteClick = onNoteClick,
                )
            }
        }
    }
}
