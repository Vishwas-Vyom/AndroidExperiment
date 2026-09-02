package me.vishwas.androidexperimental.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.SmartDisplay
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme

// Matches the 300ms tween used across the other bottom-bar selector bars (HomeControlBar,
// IconPillSelectorBar, PillIndicatorSelectorBar) so all press/selection motion reads as one system.
private const val AnimationDurationMillis = 300

private val QuickActionButtonSize = 48.dp

/**
 * The controls screen's bottom bar: a menu shortcut, the [HomeControlBar] pill, and an
 * AI-assistant shortcut, laid out in a single row.
 *
 * Stateless: the caller owns [selectedItem] and is notified of changes via [onItemSelected].
 */
@Composable
fun HomeQuickActionsBar(
    items: List<ControlItem>,
    selectedItem: ControlItem?,
    onItemSelected: (ControlItem?) -> Unit,
    onMenuClick: () -> Unit,
    onAiClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QuickActionCircleButton(
            icon = Icons.Outlined.GridView,
            contentDescription = "Menu",
            onClick = onMenuClick,
        )
        HomeControlBar(
            items = items,
            selectedItem = selectedItem,
            onItemSelected = onItemSelected,
            modifier = Modifier.weight(1f, fill = false),
        )
        AiActionButton(onClick = onAiClick)
    }
}

@Composable
private fun QuickActionCircleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(AnimationDurationMillis),
        label = "QuickActionCircleButtonBackground",
    )

    Surface(
        modifier = modifier.size(QuickActionButtonSize),
        shape = CircleShape,
        color = backgroundColor,
        shadowElevation = 4.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                )
                .semantics { role = Role.Button },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun AiActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(AnimationDurationMillis),
        label = "AiActionButtonBackground",
    )

    Surface(
        modifier = modifier.size(QuickActionButtonSize),
        shape = CircleShape,
        color = backgroundColor,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                )
                .semantics {
                    contentDescription = "OK AI"
                    role = Role.Button
                }
                .padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = "OK AI",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private val PreviewItems = listOf(
    ControlItem(id = "light", icon = Icons.Outlined.Lightbulb, contentDescription = "Light"),
    ControlItem(id = "thermostat", icon = Icons.Outlined.Thermostat, contentDescription = "Thermostat"),
    ControlItem(id = "media", icon = Icons.Outlined.SmartDisplay, contentDescription = "Media"),
)

@PreviewLightDark
@Composable
private fun HomeQuickActionsBarPreview() {
    var selected by remember { mutableStateOf<ControlItem?>(PreviewItems.first()) }

    AndroidExperimentalTheme {
        Surface {
            HomeQuickActionsBar(
                items = PreviewItems,
                selectedItem = selected,
                onItemSelected = { selected = it },
                onMenuClick = {},
                onAiClick = {},
                modifier = Modifier.padding(24.dp),
            )
        }
    }
}
