package me.vishwas.androidexperimental.feature.blur.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.vishwas.androidexperimental.ui.blur.BlurRamp
import me.vishwas.androidexperimental.ui.blur.agslProgressiveBlur
import me.vishwas.androidexperimental.ui.blur.progressiveBlur
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme

@Composable
fun ProgressiveBlurSection(radius: Dp, modifier: Modifier = Modifier) {
    BlurDemoSection(
        title = "3 · Progressive blur, layered",
        description = "Modifier.progressiveBlur — BlurEffect has one radius per layer, so the ramp " +
            "is faked with four masked copies at increasing radii. API 31+.",
        modifier = modifier,
    ) {
        BlurSampleArtwork(
            label = "progressiveBlur",
            modifier = Modifier.sampleArt().progressiveBlur(radius, ramp = BlurRamp.TopToBottom),
        )
    }
}

@Composable
fun AgslProgressiveBlurSection(radius: Dp, modifier: Modifier = Modifier) {
    BlurDemoSection(
        title = "4 · Progressive blur, AGSL",
        description = "Modifier.agslProgressiveBlur — the same ramp computed per pixel in a " +
            "RuntimeShader, so there are no bands and the content is rasterised once. API 33+.",
        modifier = modifier,
    ) {
        BlurSampleArtwork(
            label = "agslProgressiveBlur",
            modifier = Modifier.sampleArt().agslProgressiveBlur(radius, ramp = BlurRamp.TopToBottom),
        )
    }
}

@PreviewLightDark
@Composable
private fun ProgressiveBlurSectionPreview() {
    AndroidExperimentalTheme {
        ProgressiveBlurSection(radius = 24.dp, modifier = Modifier.fillMaxWidth())
    }
}

@PreviewLightDark
@Composable
private fun AgslProgressiveBlurSectionPreview() {
    AndroidExperimentalTheme {
        AgslProgressiveBlurSection(radius = 24.dp, modifier = Modifier.fillMaxWidth())
    }
}
