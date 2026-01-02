package com.aevrontech.finevo.presentation.common

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual object LocationHelper {
    @SuppressLint("MissingPermission")
    actual suspend fun getCurrentLocation(context: Any): LocationData? {
        if (context !is Context) return null

        return suspendCoroutine { continuation ->
            try {
                val locationManager =
                    context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

                // Try GPS first, then Network
                var location: Location? = null

                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                }

                if (location == null &&
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                ) {
                    location =
                        locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }

                if (location != null) {
                    continuation.resume(
                        LocationData(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            address = null // Geocoding omitted for simplicity/permissions
                        )
                    )
                } else {
                    continuation.resume(null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                continuation.resume(null)
            }
        }
    }

    actual suspend fun getAddressFromCoordinates(context: Any, lat: Double, lng: Double): String? {
        if (context !is Context) return null
        return suspendCoroutine { continuation ->
            try {
                val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                @Suppress("DEPRECATION") val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    // Construct a decent address string
                    val sb = StringBuilder()
                    // Try to get feature name or thoroughfare, then locality
                    val feature = address.featureName
                    val thoroughfare = address.thoroughfare
                    val locality = address.locality

                    if (!feature.isNullOrEmpty() && feature != thoroughfare) {
                        sb.append(feature).append(", ")
                    }
                    if (!thoroughfare.isNullOrEmpty()) {
                        sb.append(thoroughfare).append(", ")
                    }
                    if (!locality.isNullOrEmpty()) {
                        sb.append(locality)
                    } else {
                        // fallback to admin area
                        address.adminArea?.let { sb.append(it) }
                    }

                    val result = sb.toString().trim().removeSuffix(",")
                    if (result.isEmpty()) {
                        continuation.resume("Unknown Location")
                    } else {
                        continuation.resume(result)
                    }
                } else {
                    continuation.resume(null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                continuation.resume(null)
            }
        }
    }
}
