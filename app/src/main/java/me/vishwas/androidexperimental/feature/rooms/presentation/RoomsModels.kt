package me.vishwas.androidexperimental.feature.rooms.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class RoomCard(
    val id: String,
    val name: String,
    val leftIcon: ImageVector,
    val leftText: String,
    val rightIcon: ImageVector,
    val rightText: String,
    val gradient: List<Color>,
)

val sampleRooms = listOf(
    RoomCard(
        id = "home_office",
        name = "Home Office",
        leftIcon = Icons.Filled.Lightbulb, leftText = "1 Light",
        rightIcon = Icons.Filled.Wifi, rightText = "Online",
        gradient = listOf(Color(0xFF7E9A72), Color(0xFF23301E)),
    ),
    RoomCard(
        id = "kids_room",
        name = "Kids Room",
        leftIcon = Icons.Filled.Lightbulb, leftText = "4 Lights",
        rightIcon = Icons.AutoMirrored.Filled.VolumeUp, rightText = "Playing",
        gradient = listOf(Color(0xFFD693A8), Color(0xFF35182A)),
    ),
    RoomCard(
        id = "garden_deck",
        name = "Garden Deck",
        leftIcon = Icons.Filled.WbSunny, leftText = "Sunny",
        rightIcon = Icons.Filled.WaterDrop, rightText = "Off",
        gradient = listOf(Color(0xFF8FC08A), Color(0xFF122117)),
    ),
    RoomCard(
        id = "primary_suite",
        name = "Primary Suite",
        leftIcon = Icons.Filled.Lightbulb, leftText = "3 Lights",
        rightIcon = Icons.Filled.Thermostat, rightText = "68°",
        gradient = listOf(Color(0xFFBE9573), Color(0xFF2A170F)),
    ),
    RoomCard(
        id = "family_lounge",
        name = "Family Lounge",
        leftIcon = Icons.Filled.Lightbulb, leftText = "2 Lights",
        rightIcon = Icons.Filled.Air, rightText = "1 AC",
        gradient = listOf(Color(0xFF6FA0C4), Color(0xFF11212F)),
    ),
)

val CardWidth = 260.dp
val CardHeight = 360.dp
val CornerRadius = 28.dp
const val PeekCount = 2
val PeekMainStep = 22.dp
val PeekCrossInset = 16.dp
val DragThreshold = 56.dp
