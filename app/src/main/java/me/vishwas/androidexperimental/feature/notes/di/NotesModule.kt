package me.vishwas.androidexperimental.feature.notes.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.vishwas.androidexperimental.core.database.dao.NoteDao
import me.vishwas.androidexperimental.feature.notes.data.NotesRepositoryImpl
import me.vishwas.androidexperimental.feature.notes.domain.repository.NotesRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotesModule {

    @Provides
    @Singleton
    fun provideNotesRepository(noteDao: NoteDao): NotesRepository = NotesRepositoryImpl(noteDao)
}
