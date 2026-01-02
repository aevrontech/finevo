package com.aevrontech.finevo.presentation.common

import android.preference.PreferenceManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
actual fun LocationMapPreview(lat: Double, lng: Double, modifier: Modifier) {
    val context = LocalContext.current

    // Initialize osmdroid configuration
    DisposableEffect(Unit) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        Configuration.getInstance().load(context, prefs)
        onDispose {}
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(16.0)
            isTilesScaledToDpi = true
        }
    }

    // Update map position when lat/lng changes
    LaunchedEffect(lat, lng) {
        val point = GeoPoint(lat, lng)
        mapView.controller.setCenter(point)

        mapView.overlays.clear()
        val marker = Marker(mapView)
        marker.position = point
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = "Selected Location"
        mapView.overlays.add(marker)
        mapView.invalidate()
    }

    AndroidView(factory = { mapView }, modifier = modifier)
}

@Composable
actual fun LocationPickerMap(
    initialLat: Double?,
    initialLng: Double?,
    onLocationSelected: (Double, Double) -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Initialize osmdroid
    DisposableEffect(Unit) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        Configuration.getInstance().load(context, prefs)
        onDispose {}
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            isTilesScaledToDpi = true
            controller.setZoom(16.0)
        }
    }

    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(initialLat, initialLng) {
        if (!initialized && initialLat != null && initialLng != null) {
            mapView.controller.setCenter(GeoPoint(initialLat, initialLng))
            initialized = true
        } else if (!initialized) {
            // Default to something if null (e.g. Kuala Lumpur or user location if permission
            // handled outside)
            // Ideally we should get current location here if initial is null, but for now we wait
            // for user or default
            // If location is null, we might want to center on a default or just wait.
            // Let's rely on the "My Location" button or external init.
            // But to avoid a blank ocean, let's default to KL if nothing.
            mapView.controller.setCenter(GeoPoint(3.140853, 101.693207))
            initialized = true
        }
    }

    // Listener for map movement
    DisposableEffect(mapView) {
        val listener =
            object : MapListener {
                override fun onScroll(event: ScrollEvent?): Boolean {
                    val center = mapView.mapCenter
                    onLocationSelected(center.latitude, center.longitude)
                    return true
                }

                override fun onZoom(event: ZoomEvent?): Boolean {
                    val center = mapView.mapCenter
                    onLocationSelected(center.latitude, center.longitude)
                    return true
                }
            }
        mapView.addMapListener(listener)
        onDispose { mapView.removeMapListener(listener) }
    }

    Box(modifier = modifier) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())

        // Center Marker (Pin)
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Center",
            modifier =
                Modifier
                    .size(48.dp)
                    .align(Alignment.Center)
                    .padding(bottom = 24.dp), // Check padding to align tip to center
            tint = Color(0xFFD32F2F) // Red pin
        )

        // My Location FAB
        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    val location = LocationHelper.getCurrentLocation(context)
                    location?.let {
                        mapView.controller.animateTo(GeoPoint(it.latitude, it.longitude))
                        onLocationSelected(it.latitude, it.longitude)
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color.White,
            contentColor = Color.Black
        ) { Icon(Icons.Default.LocationOn, "My Location") }
    }
}
