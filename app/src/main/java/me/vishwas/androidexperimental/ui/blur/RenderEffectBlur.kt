package me.vishwas.androidexperimental.ui.blur

import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asAndroidColorFilter
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.unit.Dp

/**
 * Blurs the content and recolours the result in the same GPU pass, by chaining a colour-filter
 * effect on top of a blur effect.
 *
 * `RenderEffect`s compose: each one takes an optional input effect and transforms its output. That
 * is what makes a tinted or desaturated frost a single pass instead of a blur layer plus an
 * overdrawn scrim — worth it here because the second layer would otherwise be full-screen.
 *
 * Order matters and is fixed here: blur first, then filter. Filtering first would tint the sharp
 * pixels and then smear the tint, which for a saturating filter is a visibly different result.
 *
 * Requires API 31; below that the content draws unchanged.
 *
 * @param radius blur radius on both axes
 * @param colorFilter applied to the blurred output — e.g. `ColorFilter.tint(color, BlendMode.SrcAtop)`
 *   for a coloured frost, or `ColorFilter.colorMatrix(...)` to desaturate it
 */
fun Modifier.blurWithColorFilter(
    radius: Dp,
    colorFilter: ColorFilter,
): Modifier = drawWithCache {
    val radiusPx = radius.toPx()
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || radiusPx <= 0f) {
        return@drawWithCache onDrawWithContent { drawContent() }
    }

    val layer = obtainGraphicsLayer()
    layer.renderEffect = blurThenFilter(radiusPx, colorFilter)
    // The blur samples past the content bounds, so clip the layer back to them.
    layer.clip = true

    onDrawWithContent {
        layer.record { this@onDrawWithContent.drawContent() }
        drawLayer(layer)
    }
}

@RequiresApi(Build.VERSION_CODES.S)
private fun blurThenFilter(radiusPx: Float, colorFilter: ColorFilter): RenderEffect =
    android.graphics.RenderEffect.createColorFilterEffect(
        colorFilter.asAndroidColorFilter(),
        android.graphics.RenderEffect.createBlurEffect(radiusPx, radiusPx, Shader.TileMode.CLAMP),
    ).asComposeRenderEffect()
