package me.vishwas.androidexperimental.core.common

import me.vishwas.androidexperimental.feature.notes.presentation.NotePreview

/**
 * Mirrors ArticleCache's role: NavController.navigate() only carries a
 * String route, not arbitrary objects, so the note being navigated to is
 * stashed here immediately before navigating and read back by the
 * destination composable — the same hand-off pattern the news feature
 * already uses for Screen.Detail.
 */
object NoteCache {
    var selectedNote: NotePreview? = null
}
