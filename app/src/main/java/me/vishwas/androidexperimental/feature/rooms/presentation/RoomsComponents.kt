package me.vishwas.androidexperimental.feature.rooms.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.vishwas.androidexperimental.ui.component.CarouselOrientation

@Composable
fun OrientationToggle(
    selected: CarouselOrientation,
    onSelectedChange: (CarouselOrientation) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFECECEC))
            .padding(4.dp),
    ) {
        CarouselOrientation.entries.forEach { option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .then(
                        if (isSelected) {
                            Modifier
                                .shadow(2.dp, RoundedCornerShape(50), clip = false)
                                .background(Color.White, RoundedCornerShape(50))
                        } else {
                            Modifier
                        },
                    )
                    .clickable { onSelectedChange(option) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = option.name,
                    color = if (isSelected) Color.Black else Color(0xFF8E8E93),
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
fun RoomCardView(
    room: RoomCard,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .shadow(16.dp, RoundedCornerShape(CornerRadius), clip = false)
            .clip(RoundedCornerShape(CornerRadius))
            .background(
                Brush.linearGradient(
                    colors = room.gradient,
                    start = Offset(0f, 0f),
                    end = Offset(400f, 900f),
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.25f))
                .clickable { onFavoriteClick() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "Favorite",
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, end = 16.dp, bottom = 20.dp),
        ) {
            Text(
                text = room.name,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = room.leftIcon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(room.leftText, color = Color.White.copy(alpha = 0.9f), fontSize = 15.sp)
                Spacer(Modifier.width(8.dp))
                Text("•", color = Color.White.copy(alpha = 0.6f), fontSize = 15.sp)
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = room.rightIcon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(room.rightText, color = Color.White.copy(alpha = 0.9f), fontSize = 15.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OrientationTogglePreview() {
    OrientationToggle(selected = CarouselOrientation.Vertical, onSelectedChange = {})
}

@Preview(showBackground = true, widthDp = 280, heightDp = 380)
@Composable
private fun RoomCardViewPreview() {
    RoomCardView(room = sampleRooms.first(), isFavorite = false, onFavoriteClick = {})
}
