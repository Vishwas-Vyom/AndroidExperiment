package me.vishwas.androidexperimental.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlin.math.abs
import kotlinx.coroutines.launch

enum class CarouselOrientation { Horizontal, Vertical }

/**
 * A stacked, drag-to-cycle card carousel: a front card with [peekCount] cards peeking out
 * behind it. Dragging the front card past [dragThreshold] cycles it to the back of the deck
 * while the peek cards promote forward to take its place.
 */
@Composable
fun <T> CardCarousel(
    items: List<T>,
    orientation: CarouselOrientation,
    cardWidth: Dp,
    cardHeight: Dp,
    modifier: Modifier = Modifier,
    peekCount: Int = 2,
    peekMainStep: Dp = 22.dp,
    peekCrossInset: Dp = 16.dp,
    dragThreshold: Dp = 24.dp,
    onFrontItemChanged: (T) -> Unit = {},
    content: @Composable (item: T, modifier: Modifier) -> Unit,
) {
    var deck by remember(items) { mutableStateOf(items) }
    val offsetMain = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val currentOnFrontItemChanged by rememberUpdatedState(onFrontItemChanged)

    val density = LocalDensity.current
    val isVertical = orientation == CarouselOrientation.Vertical
    val thresholdPx = with(density) { dragThreshold.toPx() }
    val stepPx = with(density) { peekMainStep.toPx() }

    // How far the front card travels while exiting — bounded well short of a full card
    // length so it can never drift over surrounding UI outside the carousel.
    val exitDistancePx = with(density) { (dragThreshold * 1.5f).toPx() }

    val stackWidth = if (isVertical) cardWidth else cardWidth + peekMainStep * peekCount
    val stackHeight = if (isVertical) cardHeight + peekMainStep * peekCount else cardHeight

    // Cross-axis scale for a resting peek layer (0 = front, full size).
    fun crossScaleAt(layer: Int): Float {
        if (layer <= 0) return 1f
        val fullCross = if (isVertical) cardWidth else cardHeight
        val reducedCross = fullCross - peekCrossInset * 2 * layer
        return reducedCross / fullCross
    }

    // Main-axis translation for a resting peek layer (0 = front, no offset).
    fun mainTranslationAt(layer: Int): Float = if (layer <= 0) 0f else -(stepPx * layer)

    fun advanceDeck() {
        if (deck.size > 1) {
            deck = deck.drop(1) + deck.take(1)
            deck.firstOrNull()?.let(currentOnFrontItemChanged)
        }
    }

    Box(
        modifier = modifier.width(stackWidth).height(stackHeight),
        contentAlignment = if (isVertical) Alignment.BottomCenter else Alignment.CenterEnd,
    ) {
        // Peek layers move toward the front slot as the front card is dragged away,
        // so the next card is already in place by the time the front card exits.
        for (layer in peekCount downTo 1) {
            val item = deck.getOrNull(layer) ?: continue

            content(
                item,
                Modifier
                    .width(cardWidth)
                    .height(cardHeight)
                    .graphicsLayer {
                        val progress = (abs(offsetMain.value) / thresholdPx).coerceIn(0f, 1f)
                        val scale = lerp(crossScaleAt(layer), crossScaleAt(layer - 1), progress)
                        val translation = lerp(mainTranslationAt(layer), mainTranslationAt(layer - 1), progress)
                        if (isVertical) {
                            scaleX = scale
                            translationY = translation
                        } else {
                            scaleY = scale
                            translationX = translation
                        }
                    },
            )
        }

        val front = deck.firstOrNull()
        if (front != null) {
            content(
                front,
                Modifier
                    .width(cardWidth)
                    .height(cardHeight)
                    .graphicsLayer {
                        if (isVertical) translationY = offsetMain.value else translationX = offsetMain.value
                    }
                    .pointerInput(orientation, front) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val delta = if (isVertical) dragAmount.y else dragAmount.x
                                val newValue = (offsetMain.value + delta).coerceIn(-exitDistancePx, exitDistancePx)
                                scope.launch { offsetMain.snapTo(newValue) }
                            },
                            onDragEnd = {
                                scope.launch {
                                    if (abs(offsetMain.value) > thresholdPx) {
                                        val direction = if (offsetMain.value > 0) 1f else -1f
                                        val target = direction * exitDistancePx
                                        offsetMain.animateTo(target, animationSpec = tween(220))
                                        advanceDeck()
                                        offsetMain.snapTo(0f)
                                    } else {
                                        offsetMain.animateTo(0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                    }
                                }
                            },
                            onDragCancel = {
                                scope.launch { offsetMain.animateTo(0f, animationSpec = spring()) }
                            },
                        )
                    },
            )
        }
    }
}
