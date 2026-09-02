package me.vishwas.androidexperimental.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.SmartDisplay
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme

data class ControlItem(
    val id: String,
    val icon: ImageVector,
    val contentDescription: String,
)

private const val AnimationDurationMillis = 300

// Slower, and shared by everything that moves as part of the Home icon's reveal (the icon
// itself, the items track's background, and the items sliding to/from their spread positions)
// so that whole transition reads as one synchronized motion.
private const val RevealDurationMillis = 450
private val RevealEasing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

// Matches HomeQuickActionsBar's QuickActionButtonSize so the whole bottom bar reads as one height.
private val ControlIconSize = 48.dp
private val ItemSpacing = 4.dp
private val TrackPadding = 4.dp

// Space the Home icon (plus the gap after it) always reserves in the layout, whether visible or not.
private val HomeSlotWidth = ControlIconSize + ItemSpacing

/** Clips content to a circle that grows from the center as [progress] goes 0 -> 1 — an iris reveal. */
private class CircularRevealShape(private val progress: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val radius = (size.minDimension / 2f) * progress.coerceIn(0f, 1f)
        val center = Offset(size.width / 2f, size.height / 2f)
        val path = Path().apply { addOval(Rect(center = center, radius = radius)) }
        return Outline.Generic(path)
    }
}

/**
 * A pill-shaped quick-control bar. Selecting an [items] entry reveals a "Home" icon
 * that, when tapped, clears the selection and hides itself again. The bar's own width never
 * changes: with the Home icon hidden, [items] spread out to evenly fill the space it would
 * otherwise occupy; showing it slides them back into their compact positions beside it.
 *
 * Stateless: the caller owns [selectedItem] and is notified of changes via [onItemSelected].
 */
@Composable
fun HomeControlBar(
    items: List<ControlItem>,
    selectedItem: ControlItem?,
    onItemSelected: (ControlItem?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val itemsTrackColor by animateColorAsState(
        targetValue = if (selectedItem != null) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            Color.Transparent
        },
        animationSpec = tween(RevealDurationMillis, easing = RevealEasing),
        label = "ItemsTrackBackground",
    )
    val homeIconReveal by animateFloatAsState(
        targetValue = if (selectedItem != null) 1f else 0f,
        animationSpec = tween(RevealDurationMillis, easing = RevealEasing),
        label = "HomeIconReveal",
    )

    // The full content span from the Home icon's left edge to the items track's right edge —
    // fixed by item count alone, so it's identical whether or not Home is currently visible.
    val itemCount = items.size
    val trackContentWidth = ControlIconSize * itemCount + ItemSpacing * (itemCount - 1).coerceAtLeast(0)
    val fullWidth = HomeSlotWidth + TrackPadding * 2 + trackContentWidth
    val evenGap = if (itemCount > 0) {
        (fullWidth - ControlIconSize * itemCount) / (itemCount + 1)
    } else {
        0.dp
    }

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(ItemSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ControlIconButton(
                icon = Icons.Outlined.Home,
                contentDescription = "Home",
                isSelected = false,
                enabled = selectedItem != null,
                onClick = { onItemSelected(null) },
                modifier = Modifier.clip(CircularRevealShape(homeIconReveal)),
            )

            Box {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape)
                        .background(itemsTrackColor),
                )
                Row(
                    modifier = Modifier.padding(horizontal = TrackPadding),
                    horizontalArrangement = Arrangement.spacedBy(ItemSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    items.forEachIndexed { index, item ->
                        key(item.id) {
                            val compactX = HomeSlotWidth + TrackPadding +
                                (ControlIconSize + ItemSpacing) * index
                            val evenlyX = evenGap * (index + 1) + ControlIconSize * index
                            val itemOffsetX by animateDpAsState(
                                targetValue = if (selectedItem != null) 0.dp else evenlyX - compactX,
                                animationSpec = tween(RevealDurationMillis, easing = RevealEasing),
                                label = "ItemOffsetX",
                            )
                            ControlIconButton(
                                icon = item.icon,
                                contentDescription = item.contentDescription,
                                isSelected = selectedItem?.id == item.id,
                                onClick = { onItemSelected(item) },
                                modifier = Modifier.offset(x = itemOffsetX),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ControlIconButton(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(AnimationDurationMillis),
        label = "ControlIconBackground",
    )
    val tint by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(AnimationDurationMillis),
        label = "ControlIconTint",
    )

    Box(
        modifier = modifier
            .size(ControlIconSize)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .semantics { role = Role.Button },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(24.dp),
        )
    }
}

private val PreviewItems = listOf(
    ControlItem(id = "light", icon = Icons.Outlined.Lightbulb, contentDescription = "Light"),
    ControlItem(id = "thermostat", icon = Icons.Outlined.Thermostat, contentDescription = "Thermostat"),
    ControlItem(id = "media", icon = Icons.Outlined.SmartDisplay, contentDescription = "Media"),
)

@PreviewLightDark
@Composable
private fun HomeControlBarPreview() {
    var selected by remember { mutableStateOf<ControlItem?>(null) }

    AndroidExperimentalTheme {
        Surface {
            HomeControlBar(
                items = PreviewItems,
                selectedItem = selected,
                onItemSelected = { selected = it },
                modifier = Modifier.padding(24.dp),
            )
        }
    }
}
