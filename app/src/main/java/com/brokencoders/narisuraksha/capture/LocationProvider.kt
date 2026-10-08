package com.brokencoders.narisuraksha.capture

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import com.brokencoders.narisuraksha.core.PermissionHelper
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeUnit

data class Coordinates(
    val lat: Double,
    val lon: Double,
    val accuracy: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
)

interface LocationCoordinateProvider {
    suspend fun getCurrentLocation(): Coordinates?
    val currentCoordinates: StateFlow<Coordinates?>
}

/**
 * Offline-friendly location provider.
 * Retrieves GPS coordinates using FusedLocationProviderClient with high accuracy,
 * falling back quickly to last known location, and returning null without blocking SOS transmission.
 */
class LocationProvider(private val context: Context) : LocationCoordinateProvider {

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private val _currentCoordinates = MutableStateFlow<Coordinates?>(null)
    override val currentCoordinates: StateFlow<Coordinates?> = _currentCoordinates.asStateFlow()

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Coordinates? = withContext(Dispatchers.IO) {
        if (!PermissionHelper.hasLocationPermission(context)) {
            Log.w(TAG, "Location permission not granted. Returning null.")
            return@withContext null
        }

        if (!PermissionHelper.isLocationServiceEnabled(context)) {
            Log.w(TAG, "Location Services disabled on device. Trying last known location fallback...")
        }

        try {
            // Strategy 1: Attempt to get fresh current location with quick 2.5-second timeout
            val currentLocation = withTimeoutOrNull(2500L) {
                try {
                    val task = fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        null
                    )
                    Tasks.await(task, 2200, TimeUnit.MILLISECONDS)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed getting fresh location: ${e.message}")
                    null
                }
            }

            if (currentLocation != null) {
                Log.i(TAG, "Acquired fresh GPS location: lat=${currentLocation.latitude}, lon=${currentLocation.longitude}, acc=${currentLocation.accuracy}m")
                val coords = Coordinates(
                    lat = currentLocation.latitude,
                    lon = currentLocation.longitude,
                    accuracy = currentLocation.accuracy,
                    timestamp = currentLocation.time
                )
                _currentCoordinates.value = coords
                return@withContext coords
            }

            // Strategy 2: Fast fallback to last known location (1.0s timeout)
            Log.d(TAG, "Attempting fast fallback to last known location...")
            val lastLocationTask = fusedLocationClient.lastLocation
            val lastLoc: Location? = Tasks.await(lastLocationTask, 1000, TimeUnit.MILLISECONDS)
            if (lastLoc != null) {
                Log.i(TAG, "Using last known GPS location: lat=${lastLoc.latitude}, lon=${lastLoc.longitude}, acc=${lastLoc.accuracy}m")
                val coords = Coordinates(
                    lat = lastLoc.latitude,
                    lon = lastLoc.longitude,
                    accuracy = lastLoc.accuracy,
                    timestamp = lastLoc.time
                )
                _currentCoordinates.value = coords
                return@withContext coords
            }

            Log.w(TAG, "No GPS location available offline. Returning null (FLAG_LOCATION_UNAVAILABLE).")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error acquiring location", e)
            null
        }
    }

    companion object {
        private const val TAG = "LocationProvider"
    }
}
