package me.vishwas.androidexperimental.feature.blur.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.vishwas.androidexperimental.ui.blur.blurWithColorFilter
import me.vishwas.androidexperimental.ui.blur.maskedBlur
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme

@Composable
fun MaskedBlurSection(radius: Dp, modifier: Modifier = Modifier) {
    // Transparent through the middle, opaque at the rim: sharp core, blur washing in outwards.
    val lensMask = remember {
        Brush.radialGradient(
            0.0f to Color.Transparent,
            0.45f to Color.Transparent,
            1.0f to Color.Black,
        )
    }
    BlurDemoSection(
        title = "5 · Masked blur",
        description = "Modifier.maskedBlur — a blur cannot be applied to part of a layer, so the " +
            "blurred copy is punched down to a brush with DstIn and laid back over the sharp one. " +
            "Lens, vignette, redaction. API 31+.",
        modifier = modifier,
    ) {
        BlurSampleArtwork(
            label = "maskedBlur",
            modifier = Modifier.sampleArt().maskedBlur(radius, mask = lensMask),
        )
    }
}

@Composable
fun ChainedBlurSection(radius: Dp, modifier: Modifier = Modifier) {
    val desaturate = remember {
        ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
    }
    BlurDemoSection(
        title = "6 · Chained RenderEffect",
        description = "Modifier.blurWithColorFilter — blur and desaturate in one GPU pass by " +
            "feeding the blur effect into a colour-filter effect, instead of overdrawing a scrim. " +
            "API 31+.",
        modifier = modifier,
    ) {
        BlurSampleArtwork(
            label = "blur → colorFilter",
            modifier = Modifier.sampleArt().blurWithColorFilter(radius, colorFilter = desaturate),
        )
    }
}

@PreviewLightDark
@Composable
private fun MaskedBlurSectionPreview() {
    AndroidExperimentalTheme {
        MaskedBlurSection(radius = 24.dp, modifier = Modifier.fillMaxWidth())
    }
}

@PreviewLightDark
@Composable
private fun ChainedBlurSectionPreview() {
    AndroidExperimentalTheme {
        ChainedBlurSection(radius = 24.dp, modifier = Modifier.fillMaxWidth())
    }
}
