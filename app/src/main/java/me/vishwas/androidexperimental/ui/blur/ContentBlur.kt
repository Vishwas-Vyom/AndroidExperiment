package me.vishwas.androidexperimental.ui.blur

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

// Past roughly this factor the layer gets so small that the result reads as mosaic rather than
// blur, and shrinking further buys no extra smoothing.
internal const val MaxDownscale = 24f

/**
 * Blurs the composable's own content, falling back to [downscaleBlur] on API < 31.
 *
 * `Modifier.blur` is a no-op below Android 12 — it does not throw, it just draws nothing
 * different — so on a `minSdk 24` app a plain `Modifier.blur` silently stops working for a large
 * share of devices. This wrapper keeps something on screen everywhere.
 *
 * @param radius blur radius on both axes
 * @param edgeTreatment [BlurredEdgeTreatment.Rectangle] clamps edge pixels and clips the result to
 *   the content bounds (right for images); [BlurredEdgeTreatment.Unbounded] samples transparent
 *   black and lets the blur bleed outside the bounds (right for text and irregular shapes).
 */
@Stable
fun Modifier.blurCompat(
    radius: Dp,
    edgeTreatment: BlurredEdgeTreatment = BlurredEdgeTreatment.Rectangle,
): Modifier = if (BlurSupport.isRenderEffectSupported) {
    blur(radius, edgeTreatment)
} else {
    downscaleBlur(radius)
}

/**
 * Approximates a blur by rendering the content into a deliberately undersized layer and scaling it
 * back up, so the GPU's bilinear filtering does the smoothing.
 *
 * This is the only content blur available below Android 12: it needs no `RenderEffect`, only the
 * ability to redirect drawing into a [androidx.compose.ui.graphics.layer.GraphicsLayer], which
 * Compose supports back to API 21. It is a box filter rather than a Gaussian, so it is blockier
 * than [blurCompat] — but it is cheap, since the content is rasterised at a fraction of its area.
 *
 * @param radius the blur radius to imitate; drives how far the layer is shrunk
 */
fun Modifier.downscaleBlur(radius: Dp): Modifier = drawWithCache {
    val radiusPx = radius.toPx()
    if (radiusPx <= 0f || size.minDimension <= 0f) {
        return@drawWithCache onDrawWithContent { drawContent() }
    }

    // One texel of the shrunken layer ends up covering `downscale` pixels of the original, and
    // bilinear upscaling smears each texel across that span. Half the radius lines the result up
    // reasonably with what BlurEffect produces at the same radius.
    val downscale = (radiusPx / 2f).coerceIn(1f, MaxDownscale)
    val layerSize = IntSize(
        width = (size.width / downscale).roundToInt().coerceAtLeast(1),
        height = (size.height / downscale).roundToInt().coerceAtLeast(1),
    )
    val layer = obtainGraphicsLayer()

    onDrawWithContent {
        layer.record(size = layerSize) {
            scale(1f / downscale, pivot = Offset.Zero) {
                this@onDrawWithContent.drawContent()
            }
        }
        scale(downscale, pivot = Offset.Zero) {
            drawLayer(layer)
        }
    }
}

/** Radius that reads as "frosted" for panels and sheets without washing the backdrop out. */
val FrostedGlassBlurRadius: Dp = 24.dp
