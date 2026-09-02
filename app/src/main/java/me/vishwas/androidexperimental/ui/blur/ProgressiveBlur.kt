package me.vishwas.androidexperimental.ui.blur

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

/** The axis a [progressiveBlur] ramps along, from sharp at the start to fully blurred at the end. */
enum class BlurRamp { TopToBottom, BottomToTop, StartToEnd, EndToStart }

private const val DefaultRampSteps = 4

/**
 * Blurs the content with a radius that ramps from zero to [radius] along [ramp] — the effect used
 * to fade a list out under a translucent top bar, or to soften the bottom of a hero image.
 *
 * `BlurEffect` has a single radius for the whole layer, so a true variable-radius blur is not
 * expressible with it. This builds the ramp out of [steps] discrete layers instead: the content is
 * rasterised **once**, then re-blurred at increasing radii and each result is masked to the band
 * where it dominates. Because every band's mask stays opaque past its own band, each band shows a
 * cross-fade between two neighbouring radii, which reads as continuous.
 *
 * Costs `2 * steps + 1` graphics layers, so keep [steps] low; 3–5 is plenty. Requires API 31 —
 * below that the content is drawn unchanged, since a stack of no-op blurs would just be waste.
 *
 * For a genuinely continuous ramp on API 33+, see [agslProgressiveBlur], which varies the radius
 * per pixel in a shader instead.
 */
fun Modifier.progressiveBlur(
    radius: Dp,
    ramp: BlurRamp = BlurRamp.TopToBottom,
    steps: Int = DefaultRampSteps,
): Modifier = drawWithCache {
    val radiusPx = radius.toPx()
    val stepCount = steps.coerceAtLeast(1)
    if (!BlurSupport.isRenderEffectSupported || radiusPx <= 0f || size.minDimension <= 0f) {
        return@drawWithCache onDrawWithContent { drawContent() }
    }

    val contentLayer = obtainGraphicsLayer()
    val blurLayers = List(stepCount) { obtainGraphicsLayer() }
    val maskLayers = List(stepCount) { obtainGraphicsLayer() }

    val (rampStart, rampEnd) = ramp.endpoints(size, layoutDirection)
    val masks = List(stepCount) { index ->
        Brush.linearGradient(
            0f to Color.Transparent,
            1f to Color.Black,
            start = lerp(rampStart, rampEnd, index.toFloat() / stepCount),
            end = lerp(rampStart, rampEnd, (index + 1).toFloat() / stepCount),
        )
    }
    blurLayers.forEachIndexed { index, layer ->
        val stepRadius = radiusPx * (index + 1) / stepCount
        layer.renderEffect = BlurEffect(stepRadius, stepRadius, TileMode.Clamp)
    }

    onDrawWithContent {
        contentLayer.record { this@onDrawWithContent.drawContent() }
        drawLayer(contentLayer)

        repeat(stepCount) { index ->
            blurLayers[index].record { drawLayer(contentLayer) }
            maskLayers[index].record {
                drawLayer(blurLayers[index])
                // DstIn keeps the blurred pixels only where the gradient is opaque. It has to
                // happen in its own layer — on the shared canvas it would erase the base content
                // that was already drawn underneath.
                drawRect(brush = masks[index], blendMode = BlendMode.DstIn)
            }
            drawLayer(maskLayers[index])
        }
    }
}

/** The two points the ramp runs between, in local pixels. */
internal fun BlurRamp.endpoints(size: Size, layoutDirection: LayoutDirection): Pair<Offset, Offset> {
    val leftToRight = Offset.Zero to Offset(size.width, 0f)
    val rightToLeft = Offset(size.width, 0f) to Offset.Zero
    val isRtl = layoutDirection == LayoutDirection.Rtl
    return when (this) {
        BlurRamp.TopToBottom -> Offset.Zero to Offset(0f, size.height)
        BlurRamp.BottomToTop -> Offset(0f, size.height) to Offset.Zero
        BlurRamp.StartToEnd -> if (isRtl) rightToLeft else leftToRight
        BlurRamp.EndToStart -> if (isRtl) leftToRight else rightToLeft
    }
}
