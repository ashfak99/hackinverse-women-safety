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
) {
    init {
        require(!lat.isNaN() && !lon.isNaN()) { "Coordinates cannot contain NaN" }
        require(!lat.isInfinite() && !lon.isInfinite()) { "Coordinates cannot be infinite" }
        require(lat in -90.0..90.0) { "Latitude out of range: $lat" }
        require(lon in -180.0..180.0) { "Longitude out of range: $lon" }
    }

    /**
     * A coordinate is usable only if it isn't the Null Island (0,0) sentinel.
     * A real fix at exactly (0,0) is practically impossible in our operating region
     * and is overwhelmingly reported by GPS receivers that haven't locked yet.
     */
    val isUsable: Boolean
        get() = !(lat == 0.0 && lon == 0.0)
}

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

            buildCoordinates(currentLocation)?.let { coords ->
                Log.i(
                    TAG,
                    "Acquired fresh GPS location: lat=${coords.lat}, lon=${coords.lon}, acc=${coords.accuracy}m"
                )
                _currentCoordinates.value = coords
                return@withContext coords
            }

            // Strategy 2: Fast fallback to last known location (1.0s timeout)
            Log.d(TAG, "Attempting fast fallback to last known location...")
            val lastLoc: Location? = try {
                Tasks.await(fusedLocationClient.lastLocation, 1000, TimeUnit.MILLISECONDS)
            } catch (e: Exception) {
                Log.w(TAG, "Failed getting last known location: ${e.message}")
                null
            }

            buildCoordinates(lastLoc)?.let { coords ->
                Log.i(
                    TAG,
                    "Using last known GPS location: lat=${coords.lat}, lon=${coords.lon}, acc=${coords.accuracy}m"
                )
                _currentCoordinates.value = coords
                return@withContext coords
            }

            Log.w(TAG, "No usable GPS location available offline. Returning null (FLAG_LOCATION_UNAVAILABLE).")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error acquiring location", e)
            null
        }
    }

    /**
     * Converts a raw [Location] into validated [Coordinates], returning null if the
     * fix is implausible (null, NaN, out of range, or Null Island). Callers receiving
     * null should propagate FLAG_LOCATION_UNAVAILABLE via SosPacket.create().
     */
    private fun buildCoordinates(loc: Location?): Coordinates? {
        if (loc == null) return null

        val lat = loc.latitude
        val lon = loc.longitude

        if (lat.isNaN() || lon.isNaN()) {
            Log.w(TAG, "Rejecting NaN coordinates")
            return null
        }
        if (lat.isInfinite() || lon.isInfinite()) {
            Log.w(TAG, "Rejecting infinite coordinates")
            return null
        }
        if (lat !in -90.0..90.0 || lon !in -180.0..180.0) {
            Log.w(TAG, "Rejecting out-of-range coordinates: lat=$lat, lon=$lon")
            return null
        }
        if (lat == 0.0 && lon == 0.0) {
            Log.w(TAG, "Rejecting Null Island (0,0) fix")
            return null
        }

        return try {
            Coordinates(
                lat = lat,
                lon = lon,
                accuracy = loc.accuracy,
                timestamp = loc.time
            )
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Rejecting invalid coordinates: ${e.message}")
            null
        }
    }

    companion object {
        private const val TAG = "LocationProvider"
    }
}