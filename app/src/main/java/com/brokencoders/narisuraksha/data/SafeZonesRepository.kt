package com.brokencoders.narisuraksha.data

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class SafeZoneCategory(val label: String) {
    POLICE("Police Station"),
    WOMEN_HELP_DESK("24x7 Women Desk"),
    HOSPITAL("Hospital / Trauma"),
    METRO_SECURITY("Transit Security")
}

data class SafeZoneItem(
    val id: String,
    val name: String,
    val category: SafeZoneCategory,
    val address: String,
    val lat: Double,
    val lon: Double,
    val emergencyContact: String
)

object SafeZonesRepository {

    val preCachedSafeZones = listOf(
        SafeZoneItem(
            id = "1",
            name = "Central Women Police Station & 24x7 Help Desk",
            category = SafeZoneCategory.WOMEN_HELP_DESK,
            address = "Sector 12, Police Lines (24x7 Armed Guard)",
            lat = 28.6139,
            lon = 77.2090,
            emergencyContact = "1091"
        ),
        SafeZoneItem(
            id = "2",
            name = "City Police Headquarters",
            category = SafeZoneCategory.POLICE,
            address = "Ashoka Road, Civil Lines",
            lat = 28.6190,
            lon = 77.2150,
            emergencyContact = "112"
        ),
        SafeZoneItem(
            id = "3",
            name = "Civil Hospital - Trauma & Emergency Center",
            category = SafeZoneCategory.HOSPITAL,
            address = "Medical Enclave, Ring Road",
            lat = 28.6050,
            lon = 77.2200,
            emergencyContact = "108"
        ),
        SafeZoneItem(
            id = "4",
            name = "Central Metro CISF Security Booth",
            category = SafeZoneCategory.METRO_SECURITY,
            address = "Gate 2, CISF Security Booth",
            lat = 28.6250,
            lon = 77.2180,
            emergencyContact = "155370"
        ),
        SafeZoneItem(
            id = "5",
            name = "One Stop Centre (Sakhi) for Women",
            category = SafeZoneCategory.WOMEN_HELP_DESK,
            address = "District Social Welfare Complex",
            lat = 28.6300,
            lon = 77.2250,
            emergencyContact = "181"
        ),
        SafeZoneItem(
            id = "6",
            name = "District General Hospital",
            category = SafeZoneCategory.HOSPITAL,
            address = "Sector 4B, Emergency Block",
            lat = 28.6080,
            lon = 77.2010,
            emergencyContact = "102"
        ),
        SafeZoneItem(
            id = "7",
            name = "North District Police Post",
            category = SafeZoneCategory.POLICE,
            address = "Mall Road, North Campus",
            lat = 28.6220,
            lon = 77.2040,
            emergencyContact = "112"
        )
    )

    /**
     * Calculates distance between two GPS coordinates in kilometers using Haversine formula.
     */
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }

    /**
     * Formats distance into a human-readable string (e.g. "450 m" or "2.3 km").
     */
    fun formatDistance(distanceKm: Double): String {
        return if (distanceKm < 1.0) {
            "${(distanceKm * 1000).toInt()} m"
        } else {
            String.format(java.util.Locale.US, "%.1f km", distanceKm)
        }
    }

    /**
     * Calculates initial bearing (azimuth) from point 1 to point 2 in degrees (0..360).
     */
    fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
        val theta = atan2(y, x)
        val degrees = Math.toDegrees(theta)
        return ((degrees + 360) % 360).toFloat()
    }
}
