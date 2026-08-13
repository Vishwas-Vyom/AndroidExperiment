package me.vishwas.androidexperimental.feature.notes.presentation

import android.util.Log

/**
 * Stand-in for a real analytics/crash-reporting SDK — the kind of
 * non-Compose, non-Kotlin-idiomatic external system DisposableEffect and
 * SideEffect exist to talk to safely from within composition.
 */
object NotesAnalytics {
    fun logScreenView() = Log.d("NotesAnalytics", "Notes list screen viewed")
    fun logScreenLeft() = Log.d("NotesAnalytics", "Notes list screen left")
    fun setNoteCountBreadcrumb(count: Int) = Log.d("NotesAnalytics", "noteCount=$count")
}
