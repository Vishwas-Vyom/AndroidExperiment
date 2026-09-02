package me.vishwas.androidexperimental.feature.viewport.presentation

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme

// Keeps the viewport a sane size on tablets and unfolded foldables instead of letting it stretch.
private val StageMaxWidth = 840.dp

// The stage no longer fills the available height — this is what sets the viewport's height, and
// with it the scale of the image, since the image always fills the viewport vertically.
private const val StageAspectRatio = 1f

@Composable
fun ViewportImageContent(
    imageUri: Uri?,
    onSetViewportImage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            ViewportImageStage(
                imageUri = imageUri,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = StageMaxWidth)
                    .aspectRatio(StageAspectRatio),
            )
        }
        SetViewportImageButton(
            onClick = onSetViewportImage,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@PreviewLightDark
@Composable
private fun ViewportImageContentPreview() {
    AndroidExperimentalTheme {
        ViewportImageContent(
            imageUri = null,
            onSetViewportImage = {},
        )
    }
}
