package me.vishwas.androidexperimental.feature.notes.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme

@Composable
fun NotesListContent(
    notes: ImmutableList<NotePreview>,
    query: String,
    onQueryChange: (String) -> Unit,
    isComposingNote: Boolean,
    onDismissCompose: () -> Unit,
    onSaveNote: (title: String, content: String) -> Unit,
    onNoteClick: (NotePreview) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Keyed on notes.size (not `notes` itself) because `notes` is very likely
    // a SnapshotStateList passed by the same reference every recomposition —
    // remember(notes) would compare that reference to itself and never see a
    // "change", so filtering would silently go stale after the first add.
    // size (a plain Int) actually changes value when an item is added/removed.
    val filteredNotes = remember(notes.size, query) {
        if (query.isBlank()) {
            notes
        } else {
            notes.filter { it.title.contains(query, ignoreCase = true) }.toImmutableList()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (isComposingNote) {
            NoteComposeForm(onCancel = onDismissCompose, onSave = onSaveNote)
            HorizontalDivider()
        } else if (notes.isNotEmpty()) {
            NotesSearchField(query = query, onQueryChange = onQueryChange)
        }

        if (filteredNotes.isEmpty() && !isComposingNote) {
            NotesEmptyState(modifier = Modifier.weight(1f))
        } else {
            val listState = rememberLazyListState()

            // Keyed on notes.size (a real change, not a reference) so this
            // re-runs specifically when a note is added — auto-scrolling to
            // reveal it — without re-triggering on unrelated recompositions.
            LaunchedEffect(notes.size) {
                if (filteredNotes.isNotEmpty()) {
                    listState.animateScrollToItem(filteredNotes.lastIndex)
                }
            }

            LazyColumn(state = listState, modifier = Modifier.weight(1f)) {
                items(filteredNotes, key = { it.id }) { note ->
                    NoteListItem(note = note, onClick = { onNoteClick(note) })
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun NotesSearchField(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search notes") },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    )
}

@PreviewLightDark
@Composable
private fun NotesListContentEmptyPreview() {
    AndroidExperimentalTheme {
        NotesListContent(
            notes = persistentListOf(),
            query = "",
            onQueryChange = {},
            isComposingNote = false,
            onDismissCompose = {},
            onSaveNote = { _, _ -> },
            onNoteClick = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun NotesListContentWithNotesPreview() {
    AndroidExperimentalTheme {
        NotesListContent(
            notes = persistentListOf(
                NotePreview(1, "Grocery list", "Milk, eggs, bread, spinach, coffee beans."),
                NotePreview(2, "Standup notes", "Finished the notes list screen."),
            ),
            query = "",
            onQueryChange = {},
            isComposingNote = false,
            onDismissCompose = {},
            onSaveNote = { _, _ -> },
            onNoteClick = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun NotesListContentComposingPreview() {
    AndroidExperimentalTheme {
        NotesListContent(
            notes = persistentListOf(),
            query = "",
            onQueryChange = {},
            isComposingNote = true,
            onDismissCompose = {},
            onSaveNote = { _, _ -> },
            onNoteClick = {},
        )
    }
}
