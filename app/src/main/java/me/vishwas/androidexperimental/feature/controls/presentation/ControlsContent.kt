package me.vishwas.androidexperimental.feature.controls.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import me.vishwas.androidexperimental.ui.component.ControlItem
import me.vishwas.androidexperimental.ui.component.HomeQuickActionsBar
import me.vishwas.androidexperimental.ui.component.PillFilterBar
import me.vishwas.androidexperimental.ui.component.PillFilterItem
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme

private val sampleFloorFilters = listOf(
    PillFilterItem(id = "favorite", label = "Favorite", icon = Icons.Filled.Favorite),
    PillFilterItem(id = "gf", label = "GF"),
    PillFilterItem(id = "ff", label = "FF"),
    PillFilterItem(id = "sef", label = "SEF"),
    PillFilterItem(id = "thfd", label = "THFD"),
    PillFilterItem(id = "ff2", label = "FF2"),
    PillFilterItem(id = "tf", label = "TF"),
)

@Composable
fun ControlsContent(modifier: Modifier = Modifier) {
    var selectedItem by remember { mutableStateOf<ControlItem?>(null) }
    var selectedFloorFilter by remember { mutableStateOf(sampleFloorFilters.first()) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
    ) {
        PillFilterBar(
            items = sampleFloorFilters,
            selectedItem = selectedFloorFilter,
            onItemSelected = { selectedFloorFilter = it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 16.dp),
        )
        HomeQuickActionsBar(
            items = sampleControlItems,
            selectedItem = selectedItem,
            onItemSelected = { selectedItem = it },
            onMenuClick = {},
            onAiClick = {},
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 12.dp, end = 12.dp, bottom = 16.dp),
        )
    }
}

@PreviewLightDark
@Composable
private fun ControlsContentPreview() {
    AndroidExperimentalTheme {
        ControlsContent()
    }
}
