package com.aevrontech.finevo.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun LocationMapPreview(lat: Double, lng: Double, modifier: Modifier = Modifier)

@Composable
expect fun LocationPickerMap(
    initialLat: Double?,
    initialLng: Double?,
    onLocationSelected: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
)
