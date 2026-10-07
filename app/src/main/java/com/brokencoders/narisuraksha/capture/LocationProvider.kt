package com.brokencoders.narisuraksha.capture

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.CancellationSignal
import android.util.Log
import com.brokencoders.narisuraksha.core.PermissionHelper
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeUnit

data class Coordinates(val lat: Double, val lon: Double)

/**
 * Offline-friendly location provider.
 * Retrieves GPS coordinates using FusedLocationProviderClient with high accuracy,
 * falling back to last known location, and ultimately returning null if GPS is unavailable offline.
 */
class LocationProvider(private val context: Context) {

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Coordinates? = withContext(Dispatchers.IO) {
        if (!PermissionHelper.hasLocationPermission(context)) {
            Log.w(TAG, "Location permission not granted. Returning null.")
            return@withContext null
        }

        try {
            // Strategy 1: Attempt to get current location with 4-second timeout
            val currentLocation = withTimeoutOrNull(4000L) {
                try {
                    val cancelSignal = CancellationSignal()
                    val task = fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        null
                    )
                    Tasks.await(task, 3500, TimeUnit.MILLISECONDS)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed getting current fresh location: ${e.message}")
                    null
                }
            }

            if (currentLocation != null) {
                Log.i(TAG, "Acquired fresh GPS location: lat=${currentLocation.latitude}, lon=${currentLocation.longitude}")
                return@withContext Coordinates(currentLocation.latitude, currentLocation.longitude)
            }

            // Strategy 2: Fallback to last known location
            Log.d(TAG, "Attempting fallback to last known location...")
            val lastLocationTask = fusedLocationClient.lastLocation
            val lastLoc: Location? = Tasks.await(lastLocationTask, 2000, TimeUnit.MILLISECONDS)
            if (lastLoc != null) {
                Log.i(TAG, "Using last known GPS location: lat=${lastLoc.latitude}, lon=${lastLoc.longitude}")
                return@withContext Coordinates(lastLoc.latitude, lastLoc.longitude)
            }

            Log.w(TAG, "No GPS location available offline.")
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
