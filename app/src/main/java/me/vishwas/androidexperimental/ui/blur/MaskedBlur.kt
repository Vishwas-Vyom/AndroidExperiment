package me.vishwas.androidexperimental.ui.blur

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.unit.Dp

/**
 * Blurs the content only where [mask] is opaque, leaving it sharp everywhere else — a lens or
 * spotlight blur, a vignette, a redaction patch.
 *
 * The same three-layer sandwich as [progressiveBlur], with one band instead of a ramp: draw the
 * sharp content, blur a copy of it, punch the copy down to the mask with `DstIn`, and lay the
 * survivor on top. A blur cannot be applied to part of a layer, so isolating the region has to
 * happen after the blur, not during it.
 *
 * Requires API 31; below that the content draws unchanged.
 *
 * @param radius blur radius inside the masked region
 * @param mask any brush — a `Brush.radialGradient` for a soft lens, a `Brush.verticalGradient` for
 *   a fade, a solid `SolidColor(Color.Black)` clipped by the caller for a hard-edged patch. Only
 *   the alpha of the brush matters.
 */
fun Modifier.maskedBlur(
    radius: Dp,
    mask: Brush,
): Modifier = drawWithCache {
    val radiusPx = radius.toPx()
    if (!BlurSupport.isRenderEffectSupported || radiusPx <= 0f) {
        return@drawWithCache onDrawWithContent { drawContent() }
    }

    val contentLayer = obtainGraphicsLayer()
    val blurLayer = obtainGraphicsLayer()
    val maskLayer = obtainGraphicsLayer()
    blurLayer.renderEffect = BlurEffect(radiusPx, radiusPx, TileMode.Clamp)

    onDrawWithContent {
        contentLayer.record { this@onDrawWithContent.drawContent() }
        drawLayer(contentLayer)

        blurLayer.record { drawLayer(contentLayer) }
        maskLayer.record {
            drawLayer(blurLayer)
            drawRect(brush = mask, blendMode = BlendMode.DstIn)
        }
        drawLayer(maskLayer)
    }
}
