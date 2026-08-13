package me.vishwas.androidexperimental.feature.rooms.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import me.vishwas.androidexperimental.ui.component.CarouselOrientation

@Composable
fun RoomsContent(
    orientation: CarouselOrientation,
    onOrientationChange: (CarouselOrientation) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 24.dp)
            .safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OrientationToggle(
            selected = orientation,
            onSelectedChange = onOrientationChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
        )

        Spacer(Modifier.height(48.dp))

        RoomCardStack(
            rooms = sampleRooms,
            orientation = orientation,
        )
    }
}

@PreviewLightDark
@Composable
private fun RoomsContentPreview() {
    RoomsContent(orientation = CarouselOrientation.Vertical, onOrientationChange = {})
}
