package me.vishwas.androidexperimental.feature.rooms.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import me.vishwas.androidexperimental.ui.component.CarouselOrientation

@Composable
fun RoomsScreen(modifier: Modifier = Modifier) {
    var orientation by remember { mutableStateOf(CarouselOrientation.Vertical) }

    RoomsContent(
        orientation = orientation,
        onOrientationChange = { orientation = it },
        modifier = modifier,
    )
}
