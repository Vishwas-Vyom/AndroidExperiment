package me.vishwas.androidexperimental.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme

private const val AnimationDurationMillis = 300
private val BarCornerRadius = 20.dp
private val IndicatorCornerRadius = 16.dp
private val BarShadowElevation = 12.dp
private val BarShadowColor = Color.Black.copy(alpha = 0.15f)
private val ItemHeight = 48.dp

private data class SegmentBoundsPx(val offsetX: Float, val width: Float)

/**
 * A horizontally scrollable pill selector — supports any number of [items]. A single pill
 * indicator slides and resizes beneath the selected item (rather than each segment animating
 * its own background), and the selection is kept centered in the visible bar as it changes.
 *
 * Generic over [T] so the same bar can back any numbered/labeled selection (floors, rooms,
 * pages, …) — [primaryLabel] renders every segment, [selectedLabel] renders extra text shown
 * only on the selected segment (return `null` to omit it entirely).
 *
 * Stateless: the caller owns [selectedItem] and is notified of changes via [onItemSelected].
 */
@Composable
fun <T> PillIndicatorSelectorBar(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    key: (T) -> Any,
    primaryLabel: (T) -> String,
    modifier: Modifier = Modifier,
    selectedLabel: (T) -> String? = { null },
) {
    val density = LocalDensity.current
    val scrollState = rememberScrollState()
    val selectedKey = key(selectedItem)

    var segmentBounds by remember { mutableStateOf<Map<Any, SegmentBoundsPx>>(emptyMap()) }
    val indicatorOffsetX = remember { Animatable(0f) }
    val indicatorWidth = remember { Animatable(0f) }
    var indicatorInitialized by remember { mutableStateOf(false) }
    var viewportWidthPx by remember { mutableStateOf(0) }
    var scrollInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(selectedKey, segmentBounds) {
        val bounds = segmentBounds[selectedKey] ?: return@LaunchedEffect
        if (!indicatorInitialized) {
            indicatorOffsetX.snapTo(bounds.offsetX)
            indicatorWidth.snapTo(bounds.width)
            indicatorInitialized = true
        } else {
            coroutineScope {
                launch { indicatorOffsetX.animateTo(bounds.offsetX, tween(AnimationDurationMillis)) }
                launch { indicatorWidth.animateTo(bounds.width, tween(AnimationDurationMillis)) }
            }
        }
    }

    // Keeps the selected item centered in the visible bar, so it stays reachable as the
    // list grows — it slides into view together with the indicator instead of being left
    // off-screen after a scrolled selection.
    LaunchedEffect(selectedKey, segmentBounds, viewportWidthPx) {
        val bounds = segmentBounds[selectedKey] ?: return@LaunchedEffect
        if (viewportWidthPx <= 0) return@LaunchedEffect
        val target = (bounds.offsetX + bounds.width / 2f - viewportWidthPx / 2f)
            .roundToInt()
            .coerceIn(0, scrollState.maxValue)
        if (!scrollInitialized) {
            scrollState.scrollTo(target)
            scrollInitialized = true
        } else {
            scrollState.animateScrollTo(target)
        }
    }

    Surface(
        modifier = modifier.shadow(
            elevation = BarShadowElevation,
            shape = RoundedCornerShape(BarCornerRadius),
            ambientColor = BarShadowColor,
            spotColor = BarShadowColor,
        ),
        shape = RoundedCornerShape(BarCornerRadius),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box(
            modifier = Modifier
                .onSizeChanged { viewportWidthPx = it.width }
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 8.dp),
        ) {
            if (indicatorInitialized) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(indicatorOffsetX.value.roundToInt(), 0) }
                        .width(with(density) { indicatorWidth.value.toDp() })
                        .height(ItemHeight)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(IndicatorCornerRadius)),
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEach { item ->
                    val itemKey = key(item)
                    key(itemKey) {
                        PillIndicatorSegment(
                            primaryLabel = primaryLabel(item),
                            selectedLabel = selectedLabel(item),
                            isSelected = itemKey == selectedKey,
                            onClick = { onItemSelected(item) },
                            modifier = Modifier.onGloballyPositioned { coordinates ->
                                val position = coordinates.positionInParent()
                                segmentBounds = segmentBounds +
                                    (itemKey to SegmentBoundsPx(position.x, coordinates.size.width.toFloat()))
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PillIndicatorSegment(
    primaryLabel: String,
    selectedLabel: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
        animationSpec = tween(AnimationDurationMillis),
        label = "PillIndicatorSegmentContentColor",
    )

    Row(
        modifier = modifier
            .height(ItemHeight)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .semantics {
                role = Role.Tab
                selected = isSelected
            }
            .animateContentSize(animationSpec = tween(AnimationDurationMillis))
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = primaryLabel,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            color = contentColor,
        )
        AnimatedVisibility(
            visible = isSelected && selectedLabel != null,
            enter = fadeIn(tween(AnimationDurationMillis)) + expandHorizontally(tween(AnimationDurationMillis)),
            exit = fadeOut(tween(AnimationDurationMillis)) + shrinkHorizontally(tween(AnimationDurationMillis)),
        ) {
            Text(
                text = selectedLabel.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                color = contentColor,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PillIndicatorSelectorBarPreview() {
    var selected by remember { mutableStateOf(1) }

    AndroidExperimentalTheme {
        Surface {
            PillIndicatorSelectorBar(
                items = (1..8).toList(),
                selectedItem = selected,
                onItemSelected = { selected = it },
                key = { it },
                primaryLabel = { it.toString() },
                modifier = Modifier.padding(24.dp),
            )
        }
    }
}
