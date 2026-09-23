package me.vishwas.androidexperimental.feature.blur.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme

/**
 * Opens the frosted sheet that the whole lab screen is wrapped in. The sheet has to live at the
 * root of the screen rather than in this card, because it blurs the screen behind it and can only
 * sample a background it is a sibling of.
 */
@Composable
fun SheetBlurSection(
    onOpenSheet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BlurDemoSection(
        title = "9 · Frosted bottom sheet",
        description = "FrostedBottomSheetHost — backdrop blur applied to a real sheet. Drawn " +
            "inside the app window rather than in ModalBottomSheet's own dialog window, because " +
            "a blur cannot read pixels belonging to another window. Scroll the list behind it " +
            "while it is open to watch the glass keep up.",
        modifier = modifier,
    ) {
        Button(onClick = onOpenSheet, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Open a sheet you can see the screen through")
        }
    }
}

/** What the sheet shows. Short, because the sheet takes vertical drags itself. */
@Composable
fun ColumnScope.FrostedSheetDemoContent(onClose: () -> Unit) {
    Text(text = "Frosted sheet", style = MaterialTheme.typography.headlineSmall)
    Text(
        text = "Everything below the sheet edge is this screen, blurred and washed with surface " +
            "colour. Drag the sheet down or tap outside to close it.",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 8.dp),
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(
            text = "An opaque card, for comparison with the glass around it.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(12.dp),
        )
    }
    TextButton(onClick = onClose, modifier = Modifier.align(Alignment.End)) {
        Text(text = "Close")
    }
}

@PreviewLightDark
@Composable
private fun SheetBlurSectionPreview() {
    AndroidExperimentalTheme {
        SheetBlurSection(onOpenSheet = {}, modifier = Modifier.fillMaxWidth())
    }
}

@PreviewLightDark
@Composable
private fun FrostedSheetDemoContentPreview() {
    AndroidExperimentalTheme {
        Surface {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                FrostedSheetDemoContent(onClose = {})
            }
        }
    }
}
