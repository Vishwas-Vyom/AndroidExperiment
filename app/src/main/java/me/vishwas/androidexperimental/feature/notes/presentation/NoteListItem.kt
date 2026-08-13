package me.vishwas.androidexperimental.feature.notes.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme

/**
 * clickable() before padding() so the ripple fills the entire row bounds
 * (including the padding area) instead of only the inner content box.
 * Reversing the order would visibly shrink the ripple to the text/thumbnail
 * bounds only, leaving a "dead" ripple-less margin around it.
 */
fun Modifier.clickableNoteRow(onClick: () -> Unit): Modifier = this
    .clickable(onClick = onClick)
    .padding(horizontal = 16.dp, vertical = 12.dp)
    .semantics(mergeDescendants = true) {}

@Composable
fun NoteListItem(
    note: NotePreview,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickableNoteRow(onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NoteThumbnail(photoUrl = note.photoUrl)
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun NoteThumbnail(photoUrl: String?, modifier: Modifier = Modifier) {
    if (photoUrl != null) {
        // Box, not Row/Column: the badge must overlap the thumbnail's corner,
        // not sit sequentially beside or below it — Modifier.align() inside
        // Box positions it relative to the Box's own bounds, adapting
        // automatically if the 48.dp size constant ever changes.
        Box(modifier = modifier.size(48.dp)) {
            AsyncImage(
                model = photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
            )
            Icon(
                imageVector = Icons.Rounded.PhotoCamera,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(16.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .padding(2.dp),
            )
        }
    } else {
        Box(
            modifier = modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Notes,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun NoteListItemPreview() {
    AndroidExperimentalTheme {
        NoteListItem(
            note = NotePreview(1, "Grocery list", "Milk, eggs, bread, spinach, coffee beans, and avocados."),
            onClick = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun NoteListItemWithPhotoPreview() {
    AndroidExperimentalTheme {
        NoteListItem(
            note = NotePreview(2, "Trip ideas", "Kyoto in autumn.", photoUrl = "https://picsum.photos/200"),
            onClick = {},
        )
    }
}
