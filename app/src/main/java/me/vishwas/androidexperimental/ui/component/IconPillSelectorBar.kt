package me.vishwas.androidexperimental.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme

private const val AnimationDurationMillis = 300
private val BarHeight = 88.dp
private val BarCornerRadius = 44.dp
private val BarShadowElevation = 16.dp
private val BarShadowColor = Color.Black.copy(alpha = 0.15f)
private val ItemSlotSize = 56.dp
private val ItemSlotSpacing = 28.dp
private val IconSize = 26.dp
private val IndicatorSize = 108.dp
private val IndicatorShadowElevation = 8.dp

/**
 * A pill-shaped icon selector bar where the selected [items] entry sits inside a large
 * circular indicator that overflows above and below the pill and slides horizontally to the
 * newly selected icon.
 *
 * Stateless: the caller owns [selectedItem] and is notified of changes via [onItemSelected].
 */
@Composable
fun IconPillSelectorBar(
    items: List<ControlItem>,
    selectedItem: ControlItem,
    onItemSelected: (ControlItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    var itemCenters by remember { mutableStateOf<Map<String, Float>>(emptyMap()) }
    val indicatorOffsetX = remember { Animatable(0f) }
    var indicatorInitialized by remember { mutableStateOf(false) }
    val indicatorRadiusPx = with(density) { (IndicatorSize / 2).toPx() }

    LaunchedEffect(selectedItem.id, itemCenters) {
        val center = itemCenters[selectedItem.id] ?: return@LaunchedEffect
        val target = center - indicatorRadiusPx
        if (!indicatorInitialized) {
            indicatorOffsetX.snapTo(target)
            indicatorInitialized = true
        } else {
            indicatorOffsetX.animateTo(target, tween(AnimationDurationMillis))
        }
    }

    Box(
        modifier = modifier.height(BarHeight),
        contentAlignment = Alignment.CenterStart,
    ) {
        Surface(
            modifier = Modifier
                .matchParentSize()
                .shadow(
                    elevation = BarShadowElevation,
                    shape = RoundedCornerShape(BarCornerRadius),
                    ambientColor = BarShadowColor,
                    spotColor = BarShadowColor,
                ),
            shape = RoundedCornerShape(BarCornerRadius),
            color = MaterialTheme.colorScheme.surface,
        ) {}

        if (indicatorInitialized) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset { IntOffset(indicatorOffsetX.value.roundToInt(), 0) }
                    .size(IndicatorSize)
                    .shadow(elevation = IndicatorShadowElevation, shape = CircleShape)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
            )
        }

        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(ItemSlotSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                IconPillSegment(
                    item = item,
                    isSelected = item.id == selectedItem.id,
                    onClick = { onItemSelected(item) },
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        val center = coordinates.positionInParent().x + coordinates.size.width / 2f
                        itemCenters = itemCenters + (item.id to center)
                    },
                )
            }
        }
    }
}

@Composable
private fun IconPillSegment(
    item: ControlItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
        animationSpec = tween(AnimationDurationMillis),
        label = "IconPillSegmentTint",
    )

    Box(
        modifier = modifier
            .size(ItemSlotSize)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .semantics {
                role = Role.Tab
                selected = isSelected
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.contentDescription,
            tint = tint,
            modifier = Modifier.size(IconSize),
        )
    }
}

private val PreviewItems = listOf(
    ControlItem(id = "alerts", icon = Icons.Outlined.Notifications, contentDescription = "Alerts"),
    ControlItem(id = "lock", icon = Icons.Outlined.Lock, contentDescription = "Lock"),
    ControlItem(id = "camera", icon = Icons.Outlined.Videocam, contentDescription = "Camera"),
)

@PreviewLightDark
@Composable
private fun IconPillSelectorBarPreview() {
    var selected by remember { mutableStateOf(PreviewItems[1]) }

    AndroidExperimentalTheme {
        Surface {
            IconPillSelectorBar(
                items = PreviewItems,
                selectedItem = selected,
                onItemSelected = { selected = it },
                modifier = Modifier.padding(24.dp),
            )
        }
    }
}
