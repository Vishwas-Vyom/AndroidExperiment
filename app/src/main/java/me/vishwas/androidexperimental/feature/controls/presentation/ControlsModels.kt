package me.vishwas.androidexperimental.feature.controls.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.SmartDisplay
import androidx.compose.material.icons.outlined.Thermostat
import me.vishwas.androidexperimental.ui.component.ControlItem

val sampleControlItems = listOf(
    ControlItem(id = "light", icon = Icons.Outlined.Lightbulb, contentDescription = "Light"),
    ControlItem(id = "thermostat", icon = Icons.Outlined.Thermostat, contentDescription = "Thermostat"),
    ControlItem(id = "media", icon = Icons.Outlined.SmartDisplay, contentDescription = "Media"),
)
