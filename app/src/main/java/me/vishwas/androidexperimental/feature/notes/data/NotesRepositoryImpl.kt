package me.vishwas.androidexperimental.feature.notes.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.vishwas.androidexperimental.core.database.dao.NoteDao
import me.vishwas.androidexperimental.core.database.entity.NoteEntity
import me.vishwas.androidexperimental.feature.notes.domain.model.Note
import me.vishwas.androidexperimental.feature.notes.domain.repository.NotesRepository
import javax.inject.Inject

class NotesRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao,
) : NotesRepository {

    override fun getNotes(): Flow<List<Note>> =
        noteDao.getNotes().map { entities -> entities.map { it.toDomain() } }

    override suspend fun addNote(title: String, content: String) {
        noteDao.insert(
            NoteEntity(title = title, content = content, photoUrl = null, createdAt = System.currentTimeMillis()),
        )
    }

    private fun NoteEntity.toDomain() = Note(
        id = id,
        title = title,
        content = content,
        photoUrl = photoUrl,
        createdAt = createdAt,
    )
}
