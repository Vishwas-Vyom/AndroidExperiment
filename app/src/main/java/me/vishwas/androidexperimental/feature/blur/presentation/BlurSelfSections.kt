package me.vishwas.androidexperimental.feature.blur.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.vishwas.androidexperimental.ui.blur.blurCompat
import me.vishwas.androidexperimental.ui.blur.downscaleBlur
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme

internal val SampleArtHeight = 150.dp
internal val SampleArtShape = RoundedCornerShape(16.dp)

internal fun Modifier.sampleArt(): Modifier = fillMaxWidth()
    .height(SampleArtHeight)
    .clip(SampleArtShape)

@Composable
fun ContentBlurSection(radius: Dp, modifier: Modifier = Modifier) {
    BlurDemoSection(
        title = "1 · Content blur",
        description = "Modifier.blurCompat — blurs this composable's own pixels on API 31+, and " +
            "falls back to the downscale trick below that, where Modifier.blur does nothing at all.",
        modifier = modifier,
    ) {
        BlurSampleArtwork(
            label = "blurCompat",
            modifier = Modifier.sampleArt().blurCompat(radius),
        )
    }
}

@Composable
fun DownscaleBlurSection(radius: Dp, modifier: Modifier = Modifier) {
    BlurDemoSection(
        title = "2 · Downscale blur",
        description = "Modifier.downscaleBlur — renders into an undersized layer and stretches it " +
            "back, letting bilinear filtering do the smoothing. No RenderEffect, so it works back " +
            "to API 21. Blockier than a real Gaussian, and cheaper.",
        modifier = modifier,
    ) {
        BlurSampleArtwork(
            label = "downscaleBlur",
            modifier = Modifier.sampleArt().downscaleBlur(radius),
        )
    }
}

@PreviewLightDark
@Composable
private fun ContentBlurSectionPreview() {
    AndroidExperimentalTheme {
        ContentBlurSection(radius = 16.dp, modifier = Modifier.fillMaxWidth())
    }
}

@PreviewLightDark
@Composable
private fun DownscaleBlurSectionPreview() {
    AndroidExperimentalTheme {
        DownscaleBlurSection(radius = 16.dp, modifier = Modifier.fillMaxWidth())
    }
}
