package me.vishwas.androidexperimental.feature.notes.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import me.vishwas.androidexperimental.core.common.NoteCache

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(onNavigateBack: () -> Unit) {
    val note = NoteCache.selectedNote

    // Mirrors DetailScreen's guard for a null cached article: if this
    // screen is somehow reached with nothing cached (e.g. process death
    // wiped the in-memory NoteCache), bail back rather than crash on a
    // null note — LaunchedEffect(Unit) is the correct place for this
    // one-shot navigation side effect (Topic 10).
    if (note == null) {
        LaunchedEffect(Unit) { onNavigateBack() }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(note.title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        NoteDetailContent(note = note, modifier = Modifier.padding(innerPadding))
    }
}
