package me.vishwas.androidexperimental.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme

/**
 * Floor instance of [PillIndicatorSelectorBar]: floors render as zero-padded two-digit numbers
 * ("01", "02", …) and the selected floor reveals [floorLabel] next to its number.
 *
 * Stateless: the caller owns [selectedFloor] and is notified of changes via [onFloorSelected].
 */
@Composable
fun FloorSelectorBar(
    floors: List<Int>,
    selectedFloor: Int,
    onFloorSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    floorLabel: String = "Floor",
) {
    PillIndicatorSelectorBar(
        items = floors,
        selectedItem = selectedFloor,
        onItemSelected = onFloorSelected,
        key = { it },
        primaryLabel = { it.toString().padStart(2, '0') },
        selectedLabel = { floorLabel },
        modifier = modifier,
    )
}

@PreviewLightDark
@Composable
private fun FloorSelectorBarPreview() {
    var selected by remember { mutableStateOf(1) }

    AndroidExperimentalTheme {
        Surface {
            FloorSelectorBar(
                floors = (1..5).toList(),
                selectedFloor = selected,
                onFloorSelected = { selected = it },
                modifier = Modifier.padding(24.dp),
            )
        }
    }
}
