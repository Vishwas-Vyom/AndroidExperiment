package me.vishwas.androidexperimental.feature.notes.presentation

data class NotePreview(
    val id: Int,
    val title: String,
    val content: String,
    val photoUrl: String? = null,
)
