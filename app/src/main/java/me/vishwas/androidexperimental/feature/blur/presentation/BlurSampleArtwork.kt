package me.vishwas.androidexperimental.feature.blur.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme

private const val StripeCount = 14
private const val StripeAlpha = 0.30f

/**
 * The subject every demo blurs.
 *
 * Deliberately high-contrast and high-frequency: fine stripes and hard-edged circles are where a
 * blur is easiest to read, and text on top shows how far the radius has gone, since legibility
 * falls off long before the shapes stop being recognisable.
 */
@Composable
fun BlurSampleArtwork(
    label: String,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(colorScheme.primaryContainer, colorScheme.tertiaryContainer),
                ),
            )
            val stripeWidth = size.width / (StripeCount * 2f)
            repeat(StripeCount) { index ->
                drawRect(
                    color = colorScheme.onPrimaryContainer.copy(alpha = StripeAlpha),
                    topLeft = Offset(x = index * stripeWidth * 2f, y = 0f),
                    size = Size(width = stripeWidth, height = size.height),
                )
            }
            drawCircle(
                color = colorScheme.primary,
                radius = size.minDimension * 0.24f,
                center = Offset(size.width * 0.24f, size.height * 0.34f),
            )
            drawCircle(
                color = colorScheme.tertiary,
                radius = size.minDimension * 0.17f,
                center = Offset(size.width * 0.76f, size.height * 0.70f),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

@PreviewLightDark
@Composable
private fun BlurSampleArtworkPreview() {
    AndroidExperimentalTheme {
        BlurSampleArtwork(
            label = "Sample",
            modifier = Modifier
                .requiredWidth(320.dp)
                .requiredHeight(160.dp),
        )
    }
}
