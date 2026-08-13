package me.vishwas.androidexperimental.feature.notes.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme

@Composable
fun NoteDetailContent(note: NotePreview, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(text = note.title, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = note.content,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
fun NoteDetailPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Select a note to view it here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@PreviewLightDark
@Composable
private fun NoteDetailContentPreview() {
    AndroidExperimentalTheme {
        NoteDetailContent(
            note = NotePreview(1, "Grocery list", "Milk, eggs, bread, spinach, coffee beans, and avocados."),
        )
    }
}

@PreviewLightDark
@Composable
private fun NoteDetailPlaceholderPreview() {
    AndroidExperimentalTheme {
        NoteDetailPlaceholder()
    }
}
