package me.vishwas.androidexperimental.feature.notes.domain.repository

import kotlinx.coroutines.flow.Flow
import me.vishwas.androidexperimental.feature.notes.domain.model.Note

interface NotesRepository {
    fun getNotes(): Flow<List<Note>>
    suspend fun addNote(title: String, content: String)
}
