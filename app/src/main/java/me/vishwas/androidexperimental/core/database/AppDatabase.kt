package me.vishwas.androidexperimental.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import me.vishwas.androidexperimental.core.database.dao.BookmarkDao
import me.vishwas.androidexperimental.core.database.dao.NoteDao
import me.vishwas.androidexperimental.core.database.entity.BookmarkEntity
import me.vishwas.androidexperimental.core.database.entity.NoteEntity

@Database(entities = [BookmarkEntity::class, NoteEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun noteDao(): NoteDao
}
