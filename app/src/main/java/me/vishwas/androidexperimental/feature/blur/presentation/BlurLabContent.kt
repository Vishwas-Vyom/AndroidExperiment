package me.vishwas.androidexperimental.feature.blur.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme

private val ContentMaxWidth = 840.dp

/**
 * Every blur technique in `ui/blur`, driven by one shared radius so they stay comparable.
 *
 * The slider sits outside the list rather than in it, because comparing techniques means changing
 * the radius while looking at a section further down.
 */
@Composable
fun BlurLabContent(
    radius: Dp,
    onRadiusChange: (Dp) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(modifier = Modifier.widthIn(max = ContentMaxWidth)) {
            BlurRadiusSlider(
                radius = radius,
                onRadiusChange = onRadiusChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item(key = "capabilities") { BlurCapabilityBanner(Modifier.fillMaxWidth()) }
                item(key = "content") { ContentBlurSection(radius, Modifier.fillMaxWidth()) }
                item(key = "downscale") { DownscaleBlurSection(radius, Modifier.fillMaxWidth()) }
                item(key = "progressive") { ProgressiveBlurSection(radius, Modifier.fillMaxWidth()) }
                item(key = "agsl") { AgslProgressiveBlurSection(radius, Modifier.fillMaxWidth()) }
                item(key = "masked") { MaskedBlurSection(radius, Modifier.fillMaxWidth()) }
                item(key = "chained") { ChainedBlurSection(radius, Modifier.fillMaxWidth()) }
                item(key = "backdrop") { BackdropBlurSection(radius, Modifier.fillMaxWidth()) }
                item(key = "window") { WindowBlurSection(radius, Modifier.fillMaxWidth()) }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun BlurLabContentPreview() {
    AndroidExperimentalTheme {
        BlurLabContent(radius = DefaultBlurRadius, onRadiusChange = {})
    }
}
