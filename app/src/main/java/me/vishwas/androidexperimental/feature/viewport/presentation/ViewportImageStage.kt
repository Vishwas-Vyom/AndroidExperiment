package me.vishwas.androidexperimental.feature.viewport.presentation

import android.R.attr.maxWidth
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstrainScope
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.first
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme

// The two viewport edges, pinned to the left and right sides. They mark where the viewport starts
// and ends, and they are what the image is constrained to.
private val ViewportEdgeWidth = 60.dp
private const val ViewportEdgeAlpha = 0.5f

/**
 * Shows [imageUri] inside a viewport it can be panned horizontally within.
 *
 * The layout is a [ConstraintLayout] holding three things: a viewport edge pinned to the left side,
 * a viewport edge pinned to the right side, and the image — constrained `start` to the left edge and
 * `end` to the right edge. The image is scaled to cover that span (see [viewportImagePlacement]), so
 * it is wider than the space between its own constraints and overflows past both, and the solver
 * positions it by `horizontalBias`:
 *
 *  - bias `0f` — the image's left edge sits on the left viewport edge; the scroll-right limit.
 *  - bias `1f` — the image's right edge sits on the right viewport edge; the scroll-left limit.
 *
 * Panning drives that bias, so the limits are enforced by the constraints themselves: there is no
 * position between 0 and 1 where either image edge can come inside the viewport. The overflow is
 * clipped at the stage's bounds, and excess height is cropped evenly top and bottom.
 */
@Composable
fun ViewportImageStage(
    imageUri: Uri?,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.4f)
            .clipToBounds(),
    ) {
        val viewportWidth = (maxWidth - ViewportEdgeWidth * 2).coerceAtLeast(0.dp)
        val scrollState = rememberScrollState()

        if (imageUri == null) {
            ViewportImagePlaceholder(modifier = Modifier.fillMaxSize())
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(scrollState)
                    .padding(horizontal = ViewportEdgeWidth),
            ) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Selected room image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxHeight()
                        // Landscape images size themselves to their aspect ratio and overflow, which
                        // is what we want. Portrait ones would be narrower than the viewport and
                        // leave a gap, so floor their width and let Crop trim the excess height.
                        .widthIn(min = viewportWidth),
                )
            }

            // Start centred instead of pinned to the left edge. maxValue is Int.MAX_VALUE until the
            // image has been measured, so a plain scrollTo() on the first frame would be a no-op.
            LaunchedEffect(imageUri) {
                val max = snapshotFlow { scrollState.maxValue }
                    .first { it != Int.MAX_VALUE && it > 0 }
                scrollState.scrollTo(max / 2)
            }
        }

        ViewportEdge(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(ViewportEdgeWidth),
        )
        ViewportEdge(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(ViewportEdgeWidth),
        )
    }
}

@Composable
private fun ViewportEdge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(
                Color.Black.copy(alpha = ViewportEdgeAlpha),
            ),
    )
}

@PreviewLightDark
@Composable
private fun ViewportImageStageEmptyPreview() {
    AndroidExperimentalTheme {
        ViewportImageStage(
            imageUri = null,
            modifier = Modifier
                .requiredWidth(360.dp)
                .requiredHeight(320.dp),
        )
    }
}
