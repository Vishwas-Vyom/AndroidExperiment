package me.vishwas.androidexperimental.feature.blur.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import me.vishwas.androidexperimental.ui.blur.BlurBehindWindow
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme

// Window blur reads far weaker than a content blur at the same number, because it is applied to
// the composited window rather than to a tight layer.
private const val WindowBlurMultiplier = 2f
private const val DialogSurfaceAlpha = 0.9f

@Composable
fun WindowBlurSection(radius: Dp, modifier: Modifier = Modifier) {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    BlurDemoSection(
        title = "8 · Window blur",
        description = "BlurBehindWindow — asks the system compositor to blur the windows behind " +
            "this one. The only blur that reaches pixels the app does not own, and the only one " +
            "the system can refuse: it is off under battery saver and on low-end devices.",
        modifier = modifier,
    ) {
        Button(onClick = { showDialog = true }, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Open a dialog that blurs the app behind it")
        }
    }

    if (showDialog) {
        BlurredDialog(radius = radius, onDismiss = { showDialog = false })
    }
}

@Composable
private fun BlurredDialog(radius: Dp, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        BlurBehindWindow(radius = radius * WindowBlurMultiplier)
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface.copy(alpha = DialogSurfaceAlpha),
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End,
            ) {
                Text(text = "Window blur", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "The screen behind this dialog is blurred by the compositor, not by " +
                        "the app. Move the radius slider and reopen to compare.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                TextButton(onClick = onDismiss) { Text(text = "Close") }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun WindowBlurSectionPreview() {
    AndroidExperimentalTheme {
        WindowBlurSection(radius = 24.dp, modifier = Modifier.fillMaxWidth())
    }
}
