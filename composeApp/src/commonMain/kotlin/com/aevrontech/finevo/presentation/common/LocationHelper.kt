package com.aevrontech.finevo.presentation.common

data class LocationData(val latitude: Double, val longitude: Double, val address: String? = null)

expect object LocationHelper {
    suspend fun getCurrentLocation(context: Any): LocationData?
    suspend fun getAddressFromCoordinates(context: Any, lat: Double, lng: Double): String?
}
