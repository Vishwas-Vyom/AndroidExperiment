package me.vishwas.androidexperimental.ui.blur

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

/**
 * Shared handle between a [backdropSource] and the [backdropBlur] panels floating over it.
 *
 * Holds the recorded picture of the background plus where that picture sits on screen, which is
 * what lets a panel work out which slice of the background is behind it.
 */
@Stable
class BackdropState internal constructor(internal val layer: GraphicsLayer) {
    /** Top-left of the recorded source in root coordinates; unspecified until it is placed. */
    internal var sourceOrigin: Offset by mutableStateOf(Offset.Unspecified)
}

/** Creates a [BackdropState] whose backing layer is tied to the composition that remembers it. */
@Composable
fun rememberBackdropState(): BackdropState {
    val layer = rememberGraphicsLayer()
    return remember(layer) { BackdropState(layer) }
}

/**
 * Marks this composable as the background that [backdropBlur] panels sample from.
 *
 * The content still draws normally; it is just routed through a
 * [androidx.compose.ui.graphics.layer.GraphicsLayer] on the way, so the same drawing commands can
 * be replayed a second time inside a panel.
 *
 * The blurred panel must **not** be inside this subtree, or recording the source would try to
 * record the panel drawing the source. Make them siblings — background first, panels after — so
 * the source is recorded earlier in the same frame than the panels that read it.
 */
fun Modifier.backdropSource(state: BackdropState): Modifier = this
    .onGloballyPositioned { state.sourceOrigin = it.positionInRoot() }
    .drawWithContent {
        state.layer.record { this@drawWithContent.drawContent() }
        drawLayer(state.layer)
    }

/**
 * Frosted glass: blurs whatever a [backdropSource] drew behind this composable, rather than
 * blurring this composable's own content.
 *
 * This is the effect `Modifier.blur` cannot give you. A blur is a read of neighbouring pixels, and
 * a composable has no access to the pixels its parent already painted — so the background has to
 * be captured deliberately ([backdropSource]) and replayed here, shifted by the distance between
 * the two, then blurred and clipped to [shape].
 *
 * Below API 31 there is no `RenderEffect`, so the replayed background is downscaled and stretched
 * back instead — blockier, but it still reads as glass rather than as a plain scrim.
 *
 * Two things to know about how it stays in sync. Position changes invalidate the draw, because the
 * origins are snapshot state read inside the draw block. Background *content* changes do not need
 * an invalidation: the panel replays a live render node, so the compositor picks the new content
 * up on its own. The cost is that the panel can be one frame behind on the very first frame,
 * before the source has recorded anything.
 *
 * @param state the same handle passed to [backdropSource]
 * @param radius blur radius applied to the sampled background
 * @param shape clips the glass; use the same shape as the panel's own background
 * @param tint optional wash drawn over the blur — a low-alpha surface colour here is what keeps
 *   foreground text legible over an arbitrary background
 */
@Composable
fun Modifier.backdropBlur(
    state: BackdropState,
    radius: Dp = FrostedGlassBlurRadius,
    shape: Shape = RectangleShape,
    tint: Color = Color.Unspecified,
): Modifier {
    var overlayOrigin by remember { mutableStateOf(Offset.Unspecified) }
    return this
        .onGloballyPositioned { overlayOrigin = it.positionInRoot() }
        .drawWithCache {
            val radiusPx = radius.toPx()
            val useRenderEffect = BlurSupport.isRenderEffectSupported && radiusPx > 0f
            val downscale = if (useRenderEffect) 1f else (radiusPx / 2f).coerceIn(1f, MaxDownscale)

            val effectLayer = obtainGraphicsLayer()
            effectLayer.renderEffect =
                if (useRenderEffect) BlurEffect(radiusPx, radiusPx, TileMode.Clamp) else null

            val layerSize = IntSize(
                width = (size.width / downscale).roundToInt().coerceAtLeast(1),
                height = (size.height / downscale).roundToInt().coerceAtLeast(1),
            )
            val outlinePath = Path().apply {
                addOutline(shape.createOutline(size, layoutDirection, this@drawWithCache))
            }

            onDrawBehind {
                val origin = overlayOrigin
                val source = state.sourceOrigin
                if (origin.isUnspecified || source.isUnspecified) return@onDrawBehind

                // The source layer draws from its own top-left, so shift it by however far that
                // sits from ours to line the background back up under this panel.
                val shift = source - origin
                effectLayer.record(size = layerSize) {
                    scale(1f / downscale, pivot = Offset.Zero) {
                        translate(shift.x, shift.y) { drawLayer(state.layer) }
                    }
                }

                clipPath(outlinePath) {
                    scale(downscale, pivot = Offset.Zero) { drawLayer(effectLayer) }
                    if (tint.isSpecified) drawRect(tint)
                }
            }
        }
}
