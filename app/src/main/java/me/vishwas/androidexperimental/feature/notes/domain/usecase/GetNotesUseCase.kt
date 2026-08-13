package me.vishwas.androidexperimental.feature.notes.domain.usecase

import kotlinx.coroutines.flow.Flow
import me.vishwas.androidexperimental.feature.notes.domain.model.Note
import me.vishwas.androidexperimental.feature.notes.domain.repository.NotesRepository
import javax.inject.Inject

class GetNotesUseCase @Inject constructor(
    private val repository: NotesRepository,
) {
    operator fun invoke(): Flow<List<Note>> = repository.getNotes()
}
