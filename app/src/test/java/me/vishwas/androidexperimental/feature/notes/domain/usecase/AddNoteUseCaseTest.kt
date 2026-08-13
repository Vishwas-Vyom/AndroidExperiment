package me.vishwas.androidexperimental.feature.notes.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.vishwas.androidexperimental.feature.notes.domain.repository.NotesRepository
import org.junit.Test
import kotlin.test.assertFailsWith

/**
 * Unit tests for [AddNoteUseCase] — specifically the business rule (blank
 * title rejected) that lives here rather than only in the UI (Topic 21's
 * validation is UX on top of this invariant, not a replacement for it).
 */
class AddNoteUseCaseTest {

    private val repository: NotesRepository = mockk()
    private val useCase = AddNoteUseCase(repository)

    @Test
    fun `throws for a blank title and never calls the repository`() = runTest {
        coEvery { repository.addNote(any(), any()) } returns Unit

        assertFailsWith<IllegalArgumentException> {
            useCase("   ", "some content")
        }

        coVerify(exactly = 0) { repository.addNote(any(), any()) }
    }

    @Test
    fun `delegates to the repository for a non-blank title`() = runTest {
        coEvery { repository.addNote("Groceries", "Milk, eggs") } returns Unit

        useCase("Groceries", "Milk, eggs")

        coVerify(exactly = 1) { repository.addNote("Groceries", "Milk, eggs") }
    }
}
