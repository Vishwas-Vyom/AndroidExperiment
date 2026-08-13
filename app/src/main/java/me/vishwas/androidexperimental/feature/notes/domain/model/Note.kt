package me.vishwas.androidexperimental.feature.notes.domain.model

data class Note(
    val id: Long,
    val title: String,
    val content: String,
    val photoUrl: String?,
    val createdAt: Long,
)
