package me.vishwas.androidexperimental.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme

data class PillFilterItem(
    val id: String,
    val label: String,
    val icon: ImageVector? = null,
)

private const val AnimationDurationMillis = 200
private val BarCornerRadius = 28.dp
private val SegmentCornerRadius = 20.dp
private val BarShadowElevation = 12.dp
private val BarShadowColor = Color.Black.copy(alpha = 0.15f)

/**
 * A pill-shaped, horizontally scrollable row of filter segments — supports any number of
 * [items]. The selected segment is highlighted with a filled capsule (optionally showing a
 * leading icon); unselected segments render as plain text.
 *
 * Stateless: the caller owns [selectedItem] and is notified of changes via [onItemSelected].
 */
@Composable
fun PillFilterBar(
    items: List<PillFilterItem>,
    selectedItem: PillFilterItem,
    onItemSelected: (PillFilterItem) -> Unit,
    modifier: Modifier = Modifier,
) {
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
        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(items, key = { it.id }) { item ->
                PillFilterSegment(
                    item = item,
                    isSelected = item.id == selectedItem.id,
                    onClick = { onItemSelected(item) },
                )
            }
        }
    }
}

@Composable
private fun PillFilterSegment(
    item: PillFilterItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(AnimationDurationMillis),
        label = "PillFilterSegmentBackground",
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.primary
        },
        animationSpec = tween(AnimationDurationMillis),
        label = "PillFilterSegmentContent",
    )

    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(SegmentCornerRadius))
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .semantics {
                role = Role.Tab
                selected = isSelected
            }
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (item.icon != null) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            text = item.label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = contentColor,
        )
    }
}

private val PreviewItems = listOf(
    PillFilterItem(id = "favorite", label = "Favorite", icon = Icons.Filled.Favorite),
    PillFilterItem(id = "gf", label = "GF"),
    PillFilterItem(id = "ff", label = "FF"),
    PillFilterItem(id = "sef", label = "SEF"),
    PillFilterItem(id = "thfd", label = "THFD"),
)

@PreviewLightDark
@Composable
private fun PillFilterBarPreview() {
    var selected by remember { mutableStateOf(PreviewItems.first()) }

    AndroidExperimentalTheme {
        Surface {
            PillFilterBar(
                items = PreviewItems,
                selectedItem = selected,
                onItemSelected = { selected = it },
                modifier = Modifier.padding(24.dp),
            )
        }
    }
}
