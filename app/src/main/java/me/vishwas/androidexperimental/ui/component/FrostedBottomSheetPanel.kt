package me.vishwas.androidexperimental.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.vishwas.androidexperimental.ui.blur.BackdropState
import me.vishwas.androidexperimental.ui.blur.backdropBlur
import me.vishwas.androidexperimental.ui.blur.rememberBackdropState
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme
import kotlin.math.roundToInt

private val SheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
private val SheetMaxWidth = 640.dp

// Short on purpose: a small downward flick should close the sheet, rather than making the user
// haul it most of the way off screen. The velocity threshold catches the quick flick that never
// travels far enough to cross the distance threshold.
private val DismissDragDistance = 56.dp
private const val DismissFlingVelocity = 900f

// How much surface colour washes over the blur. Text on glass is only legible because of this —
// the blur alone leaves whatever was behind the sheet competing with the sheet's own content.
private const val GlassTintAlpha = 0.55f
private const val SheetBorderAlpha = 0.18f
private const val DragHandleAlpha = 0.4f

/**
 * The glass panel itself: blurs whatever the [backdrop] recorded behind it, and can be dragged
 * downwards to ask to be dismissed.
 *
 * The whole panel is draggable rather than just the handle, matching how a Material sheet behaves.
 * The consequence is that a vertically scrollable [content] would fight the sheet for the gesture;
 * this panel does not arbitrate that, so keep its content short.
 *
 * Downward drag is clamped at zero and nothing is clipped — the panel is free to slide past the
 * bottom of the screen, since bounding the offset itself is what keeps it off the surrounding UI.
 */
@Composable
internal fun FrostedSheetPanel(
    backdrop: BackdropState,
    blurRadius: Dp,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scope = rememberCoroutineScope()
    val dragOffset = remember { Animatable(0f) }
    val dismissDistancePx = with(LocalDensity.current) { DismissDragDistance.toPx() }
    val dragState = rememberDraggableState { delta ->
        scope.launch { dragOffset.snapTo((dragOffset.value + delta).coerceAtLeast(0f)) }
    }

    Column(
        modifier = modifier
            .widthIn(max = SheetMaxWidth)
            .fillMaxWidth()
            .offset { IntOffset(x = 0, y = dragOffset.value.roundToInt()) }
            .backdropBlur(
                state = backdrop,
                radius = blurRadius,
                shape = SheetShape,
                tint = MaterialTheme.colorScheme.surface.copy(alpha = GlassTintAlpha),
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = SheetBorderAlpha),
                shape = SheetShape,
            )
            .draggable(
                state = dragState,
                orientation = Orientation.Vertical,
                onDragStopped = { velocity ->
                    if (dragOffset.value > dismissDistancePx || velocity > DismissFlingVelocity) {
                        onDismissRequest()
                    } else {
                        dragOffset.animateTo(0f)
                    }
                },
            )
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
            .semantics { paneTitle = "Bottom sheet" },
    ) {
        SheetDragHandle(modifier = Modifier.align(Alignment.CenterHorizontally))
        content()
    }
}

/** The grab bar. Decorative — the whole panel takes the drag, not just this. */
@Composable
private fun SheetDragHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(vertical = 12.dp)
            .size(width = 32.dp, height = 4.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DragHandleAlpha)),
    )
}

@PreviewLightDark
@Composable
private fun FrostedSheetPanelPreview() {
    AndroidExperimentalTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            FrostedSheetPanel(
                backdrop = rememberBackdropState(),
                blurRadius = 24.dp,
                onDismissRequest = {},
            ) {
                Text(text = "Sheet title", style = MaterialTheme.typography.titleLarge)
                Text(text = "Body text on glass.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
