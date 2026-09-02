package me.vishwas.androidexperimental.feature.blur.presentation

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import me.vishwas.androidexperimental.ui.blur.BackdropState
import me.vishwas.androidexperimental.ui.blur.backdropBlur
import me.vishwas.androidexperimental.ui.blur.backdropSource
import me.vishwas.androidexperimental.ui.blur.rememberBackdropState
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme
import kotlin.math.roundToInt

private val BackdropHeight = 220.dp
private val GlassShape = RoundedCornerShape(20.dp)
private const val GlassTintAlpha = 0.28f

/**
 * The one effect `Modifier.blur` cannot do, and the reason Haze exists: blurring the *background*
 * rather than the composable's own content. Drag the panel to see the blur track the artwork
 * underneath it.
 */
@Composable
fun BackdropBlurSection(radius: Dp, modifier: Modifier = Modifier) {
    val backdrop = rememberBackdropState()
    var panelOffset by remember { mutableStateOf(Offset.Zero) }

    BlurDemoSection(
        title = "7 · Backdrop blur (frosted glass)",
        description = "backdropSource + backdropBlur — the background records itself into a " +
            "graphics layer, and the panel replays that layer shifted by the gap between them, " +
            "then blurs it. Downscales instead of blurring below API 31.",
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(BackdropHeight)
                .clip(SampleArtShape),
        ) {
            BlurSampleArtwork(
                label = "background",
                modifier = Modifier
                    .fillMaxSize()
                    .backdropSource(backdrop),
            )
            GlassPanel(
                backdrop = backdrop,
                radius = radius,
                offset = panelOffset,
                onDrag = { panelOffset += it },
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@Composable
private fun GlassPanel(
    backdrop: BackdropState,
    radius: Dp,
    offset: Offset,
    onDrag: (Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(width = 200.dp, height = 92.dp)
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .backdropBlur(
                state = backdrop,
                radius = radius,
                shape = GlassShape,
                // A wash of surface colour over the blur is what keeps the label readable no
                // matter what happens to be behind the panel.
                tint = MaterialTheme.colorScheme.surface.copy(alpha = GlassTintAlpha),
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                shape = GlassShape,
            )
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount)
                }
            }
            .semantics { contentDescription = "Draggable frosted glass panel" },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Drag me",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@PreviewLightDark
@Composable
private fun BackdropBlurSectionPreview() {
    AndroidExperimentalTheme {
        BackdropBlurSection(radius = 24.dp, modifier = Modifier.fillMaxWidth())
    }
}
