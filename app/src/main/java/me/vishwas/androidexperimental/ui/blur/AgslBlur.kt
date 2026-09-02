package me.vishwas.androidexperimental.ui.blur

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.unit.Dp

/**
 * A blur whose radius is decided per pixel, which `BlurEffect` cannot express: it takes one radius
 * for the whole layer.
 *
 * Taps are laid out on a golden-angle spiral over the unit disc and scaled by the local radius, so
 * the sample pattern stays even at every radius instead of collapsing into a cross. Sixteen taps
 * is a deliberate trade — enough to read as a blur, few enough to stay cheap; at very large radii
 * it shows some sparkle a true Gaussian would not.
 *
 * Coordinates are clamped to the layer before sampling, which reproduces `TileMode.Clamp`. Without
 * it the input shader returns transparent outside the bounds and the edges darken.
 */
private const val ProgressiveBlurAgsl = """
uniform shader content;
uniform float2 uSize;
uniform float2 uRampStart;
uniform float2 uRampEnd;
uniform float uMaxRadius;

const int TAP_COUNT = 16;
const float GOLDEN_ANGLE = 2.399963;

float2 tapOffset(int i) {
    float unit = (float(i) + 0.5) / float(TAP_COUNT);
    float angle = float(i) * GOLDEN_ANGLE;
    return float2(cos(angle), sin(angle)) * sqrt(unit);
}

half4 main(float2 coord) {
    float2 axis = uRampEnd - uRampStart;
    float axisLength2 = max(dot(axis, axis), 1e-4);
    float t = clamp(dot(coord - uRampStart, axis) / axisLength2, 0.0, 1.0);
    float radius = uMaxRadius * t;

    if (radius < 0.5) {
        return content.eval(coord);
    }

    float4 sum = float4(0.0);
    for (int i = 0; i < TAP_COUNT; i++) {
        float2 p = clamp(coord + tapOffset(i) * radius, float2(0.0), uSize);
        sum += float4(content.eval(p));
    }
    return half4(sum / float(TAP_COUNT));
}
"""

/**
 * Continuous progressive blur: the radius ramps from zero to [radius] along [ramp], evaluated per
 * pixel in an AGSL shader.
 *
 * The layered [progressiveBlur] approximates the same ramp with a handful of fixed radii and
 * cross-fades between them. This has no bands at all, and rasterises the content once rather than
 * once per step — but it needs API 33, so it is the upgrade path rather than the baseline. Below
 * 33 the content draws unchanged; pair it with [progressiveBlur] if you need both.
 */
fun Modifier.agslProgressiveBlur(
    radius: Dp,
    ramp: BlurRamp = BlurRamp.TopToBottom,
): Modifier = drawWithCache {
    val radiusPx = radius.toPx()
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        radiusPx <= 0f ||
        size.minDimension <= 0f
    ) {
        return@drawWithCache onDrawWithContent { drawContent() }
    }

    val (rampStart, rampEnd) = ramp.endpoints(size, layoutDirection)
    val layer = obtainGraphicsLayer()
    layer.renderEffect = progressiveShaderEffect(size, rampStart, rampEnd, radiusPx)
    layer.clip = true

    onDrawWithContent {
        layer.record { this@onDrawWithContent.drawContent() }
        drawLayer(layer)
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun progressiveShaderEffect(
    size: Size,
    rampStart: Offset,
    rampEnd: Offset,
    maxRadiusPx: Float,
): RenderEffect {
    val shader = RuntimeShader(ProgressiveBlurAgsl).apply {
        setFloatUniform("uSize", size.width, size.height)
        setFloatUniform("uRampStart", rampStart.x, rampStart.y)
        setFloatUniform("uRampEnd", rampEnd.x, rampEnd.y)
        setFloatUniform("uMaxRadius", maxRadiusPx)
    }
    // "content" names the uniform the layer's own drawing is bound to.
    return android.graphics.RenderEffect
        .createRuntimeShaderEffect(shader, "content")
        .asComposeRenderEffect()
}
