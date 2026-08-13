package me.vishwas.androidexperimental.feature.rooms.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import me.vishwas.androidexperimental.ui.component.CardCarousel
import me.vishwas.androidexperimental.ui.component.CarouselOrientation

@Composable
fun RoomCardStack(
    rooms: List<RoomCard>,
    orientation: CarouselOrientation,
    modifier: Modifier = Modifier,
) {
    val favorited = remember { mutableStateMapOf<String, Boolean>() }

    CardCarousel(
        items = rooms,
        orientation = orientation,
        cardWidth = CardWidth,
        cardHeight = CardHeight,
        modifier = modifier,
        peekCount = PeekCount,
        peekMainStep = PeekMainStep,
        peekCrossInset = PeekCrossInset,
        dragThreshold = DragThreshold,
    ) { room, cardModifier ->
        RoomCardView(
            room = room,
            isFavorite = favorited[room.id] == true,
            onFavoriteClick = { favorited[room.id] = !(favorited[room.id] == true) },
            modifier = cardModifier,
        )
    }
}
