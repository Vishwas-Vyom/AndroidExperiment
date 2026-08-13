package me.vishwas.androidexperimental.feature.notes.domain.usecase

import me.vishwas.androidexperimental.feature.notes.domain.repository.NotesRepository
import javax.inject.Inject

class AddNoteUseCase @Inject constructor(
    private val repository: NotesRepository,
) {
    suspend operator fun invoke(title: String, content: String) {
        require(title.isNotBlank()) { "Note title must not be blank" }
        repository.addNote(title, content)
    }
}
