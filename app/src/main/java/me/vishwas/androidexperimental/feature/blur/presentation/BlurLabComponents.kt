package me.vishwas.androidexperimental.feature.blur.presentation

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.vishwas.androidexperimental.ui.blur.BlurSupport
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme

val MinBlurRadius: Dp = 0.dp
val MaxBlurRadius: Dp = 40.dp
val DefaultBlurRadius: Dp = 20.dp

/** Card wrapper: heading, one line on what the technique is, then the live sample. */
@Composable
fun BlurDemoSection(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = description, style = MaterialTheme.typography.bodySmall)
            content()
        }
    }
}

/** Single radius shared by every section, so the techniques stay directly comparable. */
@Composable
fun BlurRadiusSlider(
    radius: Dp,
    onRadiusChange: (Dp) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Blur radius — ${radius.value.toInt()} dp",
            style = MaterialTheme.typography.labelLarge,
        )
        Slider(
            value = radius.value,
            onValueChange = { onRadiusChange(it.dp) },
            valueRange = MinBlurRadius.value..MaxBlurRadius.value,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Blur radius" },
        )
    }
}

/**
 * States plainly which techniques this device can actually run, because most of the difference
 * between them is which API level they need — on API 30 half this screen is a no-op.
 */
@Composable
fun BlurCapabilityBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lines = listOf(
        "API ${Build.VERSION.SDK_INT}",
        "RenderEffect (31+): ${BlurSupport.isRenderEffectSupported.asYesNo()}",
        "AGSL RuntimeShader (33+): ${BlurSupport.isRuntimeShaderSupported.asYesNo()}",
        "Cross-window blur: ${BlurSupport.isCrossWindowBlurEnabled(context).asYesNo()}",
    )
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = "This device", style = MaterialTheme.typography.titleMedium)
            lines.forEach { Text(text = it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

private fun Boolean.asYesNo(): String = if (this) "supported" else "unavailable"

@PreviewLightDark
@Composable
private fun BlurDemoSectionPreview() {
    AndroidExperimentalTheme {
        BlurDemoSection(
            title = "Content blur",
            description = "Blurs the composable's own pixels.",
            modifier = Modifier.fillMaxWidth(),
        ) {
            BlurRadiusSlider(radius = 16.dp, onRadiusChange = {})
        }
    }
}
