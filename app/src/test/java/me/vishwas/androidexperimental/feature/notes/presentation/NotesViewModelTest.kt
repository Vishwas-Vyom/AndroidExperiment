package me.vishwas.androidexperimental.feature.notes.presentation

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.vishwas.androidexperimental.feature.notes.domain.model.Note
import me.vishwas.androidexperimental.feature.notes.domain.usecase.AddNoteUseCase
import me.vishwas.androidexperimental.feature.notes.domain.usecase.GetNotesUseCase
import me.vishwas.androidexperimental.util.MainDispatcherRule
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Unit tests for [NotesViewModel] — covers Topic 22 (ViewModel wiring),
 * Topic 23 (StateFlow / combine of notes + search query), and Topic 24
 * (SharedFlow one-off save events), entirely without Compose, Android, or
 * a real database — only fake use cases (backed by mockk).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getNotesUseCase: GetNotesUseCase = mockk()
    private val addNoteUseCase: AddNoteUseCase = mockk()

    private fun createViewModel() = NotesViewModel(getNotesUseCase, addNoteUseCase)

    private fun note(id: Long, title: String) = Note(id, title, "content", null, createdAt = id)

    // ── uiState (Topic 23: StateFlow + combine) ──────────────────────────────

    @Test
    fun `uiState emits Loading then Success mapped from the notes flow`() = runTest {
        every { getNotesUseCase() } returns flowOf(listOf(note(1, "Groceries")))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertIs<NotesUiState.Loading>(awaitItem())
            val success = awaitItem()
            assertIs<NotesUiState.Success>(success)
            assertEquals(1, success.notes.size)
            assertEquals("Groceries", success.notes[0].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onQueryChange filters uiState notes by title, case-insensitively`() = runTest {
        every { getNotesUseCase() } returns flowOf(
            listOf(note(1, "Groceries"), note(2, "Standup notes")),
        )

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Success, unfiltered

            viewModel.onQueryChange("group")

            val filtered = awaitItem()
            assertIs<NotesUiState.Success>(filtered)
            assertEquals(1, filtered.notes.size)
            assertEquals("Groceries", filtered.notes[0].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── saveNote / events (Topic 24: SharedFlow) ─────────────────────────────

    @Test
    fun `saveNote emits NoteSaved on success`() = runTest {
        every { getNotesUseCase() } returns flowOf(emptyList())
        coEvery { addNoteUseCase("Title", "Content") } returns Unit

        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.saveNote("Title", "Content")
            assertIs<NotesUiEvent.NoteSaved>(awaitItem())
        }
    }

    @Test
    fun `saveNote emits SaveFailed when the use case rejects a blank title`() = runTest {
        every { getNotesUseCase() } returns flowOf(emptyList())
        coEvery { addNoteUseCase("", any()) } throws IllegalArgumentException("Note title must not be blank")

        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.saveNote("", "Content")
            val event = awaitItem()
            assertIs<NotesUiEvent.SaveFailed>(event)
            assertEquals("Note title must not be blank", event.message)
        }
    }
}
