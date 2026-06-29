package me.vishwas.androidexperimental.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import me.vishwas.androidexperimental.core.database.dao.BookmarkDao
import me.vishwas.androidexperimental.core.database.entity.BookmarkEntity

@Database(entities = [BookmarkEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
}
