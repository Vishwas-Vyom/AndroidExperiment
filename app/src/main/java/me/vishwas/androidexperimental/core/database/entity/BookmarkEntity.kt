package me.vishwas.androidexperimental.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val url: String,
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val source: String,
    val author: String?,
    val publishedAt: String,
    val content: String?,
    val category: String,
)
