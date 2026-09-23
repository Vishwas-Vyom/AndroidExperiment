package me.vishwas.androidexperimental.ui.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.vishwas.androidexperimental.ui.blur.FrostedGlassBlurRadius
import me.vishwas.androidexperimental.ui.blur.backdropSource
import me.vishwas.androidexperimental.ui.blur.rememberBackdropState
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme

// Light enough that the glass still reads brighter than its surroundings, which is what sells the
// panel as translucent rather than as a solid card on a dark background.
private const val ScrimAlpha = 0.32f

/**
 * Hosts [content] with a frosted-glass bottom sheet over it: the sheet is translucent and you see
 * [content] blurred *through* it, rather than the sheet sitting on an opaque surface.
 *
 * It deliberately does not use Material3's [androidx.compose.material3.ModalBottomSheet], which
 * renders into its own dialog window. A blur is a read of neighbouring pixels, and pixels in
 * another window are not readable from this one — so a sheet that wants to blur the app behind it
 * has to be drawn *inside* the app's own window, as a sibling of the content it samples. That is
 * what this host arranges: [content] records itself via
 * [me.vishwas.androidexperimental.ui.blur.backdropSource], and the sheet replays and blurs that
 * recording.
 *
 * The trade for that is manual sheet behaviour — drag to dismiss, back press, scrim tap — instead
 * of what `ModalBottomSheet` gives for free. The blur is what is being bought.
 *
 * Recording the full-size [content] into a graphics layer every frame is the running cost of the
 * technique, so it is only switched on while the sheet is on screen or animating off it.
 *
 * @param visible whether the sheet is open; the caller owns this
 * @param onDismissRequest fired by back press, a scrim tap, or a downward fling on the sheet — the
 *   host never closes itself, it only asks
 * @param blurRadius blur applied to the content seen through the sheet
 * @param sheetContent laid out in a [ColumnScope] below the drag handle
 * @param content the screen behind the sheet, and the thing the sheet blurs
 */
@Composable
fun FrostedBottomSheetHost(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    blurRadius: Dp = FrostedGlassBlurRadius,
    sheetContent: @Composable ColumnScope.() -> Unit,
    content: @Composable () -> Unit,
) {
    val backdrop = rememberBackdropState()
    val sheetVisibility = remember { MutableTransitionState(false) }
    sheetVisibility.targetState = visible

    // True while the sheet is open *or* still sliding away, which is exactly how long the sheet
    // has something to sample.
    val isBackdropActive = sheetVisibility.currentState || sheetVisibility.targetState

    BackHandler(enabled = visible, onBack = onDismissRequest)

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (isBackdropActive) Modifier.backdropSource(backdrop) else Modifier),
        ) {
            content()
        }

        // No enter/exit on the container itself: the scrim fades and the sheet slides, and giving
        // the container its own transition too would apply both to the sheet.
        AnimatedVisibility(
            visibleState = sheetVisibility,
            enter = EnterTransition.None,
            exit = ExitTransition.None,
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                SheetScrim(
                    onDismissRequest = onDismissRequest,
                    modifier = Modifier.animateEnterExit(enter = fadeIn(), exit = fadeOut()),
                )
                FrostedSheetPanel(
                    backdrop = backdrop,
                    blurRadius = blurRadius,
                    onDismissRequest = onDismissRequest,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .animateEnterExit(
                            enter = slideInVertically { height -> height },
                            exit = slideOutVertically { height -> height },
                        ),
                    content = sheetContent,
                )
            }
        }
    }
}

/** Dims everything outside the sheet and closes it when tapped. */
@Composable
private fun SheetScrim(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = ScrimAlpha))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClickLabel = "Close sheet",
                onClick = onDismissRequest,
            ),
    )
}

@PreviewLightDark
@Composable
private fun FrostedBottomSheetHostPreview() {
    AndroidExperimentalTheme {
        FrostedBottomSheetHost(
            visible = true,
            onDismissRequest = {},
            blurRadius = 24.dp,
            sheetContent = {
                Text(text = "Sheet", style = MaterialTheme.typography.titleLarge)
                Text(text = "Frosted over the screen behind it.")
            },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "Screen content")
            }
        }
    }
}
