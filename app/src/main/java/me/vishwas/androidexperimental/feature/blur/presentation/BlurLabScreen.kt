package me.vishwas.androidexperimental.feature.blur.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BlurLabScreen(modifier: Modifier = Modifier) {
    // Dp is not Saveable, so the radius survives configuration changes as its raw value.
    var radiusValue by rememberSaveable { mutableFloatStateOf(DefaultBlurRadius.value) }
    var sheetVisible by rememberSaveable { mutableStateOf(false) }

    BlurLabContent(
        radius = radiusValue.dp,
        onRadiusChange = { radiusValue = it.value },
        sheetVisible = sheetVisible,
        onSheetVisibleChange = { sheetVisible = it },
        modifier = modifier,
    )
}
