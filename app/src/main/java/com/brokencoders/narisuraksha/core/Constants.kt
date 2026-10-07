package com.brokencoders.narisuraksha.core

import java.util.UUID

object Constants {
    // 128-bit custom service UUID for BLE advertising and scanning filters
    val SOS_SERVICE_UUID: UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")

    // Legacy fallback manufacturer ID
    const val MANUFACTURER_ID = 0xFFFF

    // Timeout for BLE advertising in milliseconds (60 seconds)
    const val SOS_ADVERTISE_TIMEOUT_MS = 60_000L

    // Countdown duration in seconds
    const val COUNTDOWN_DURATION_SECONDS = 5

    // Shake detection thresholds
    const val SHAKE_THRESHOLD_G = 2.7f // ~2.7g
    const val SHAKE_SPIKE_WINDOW_MS = 2000L // 2 seconds window
    const val SHAKE_REQUIRED_SPIKES = 3
    const val SHAKE_COOLDOWN_MS = 3000L // 3 seconds cooldown between alerts

    // Notification Channel IDs
    const val CHANNEL_ALERT_ID = "nari_emergency_alert_channel"
    const val CHANNEL_ALERT_NAME = "Emergency SOS Alerts"
    const val CHANNEL_SERVICE_ID = "nari_guardian_service_channel"
    const val CHANNEL_SERVICE_NAME = "Guardian Background Service"

    const val NOTIFICATION_ID_SERVICE = 1001
    const val NOTIFICATION_ID_ALERT_BASE = 2000
    const val NOTIFICATION_ID_SOS_ACTIVE = 1002

    // Distance RSSI buckets
    // Near: < 3m (~ RSSI > -65 dBm)
    // Medium: 3m - 10m (~ RSSI between -65 and -80 dBm)
    // Far: > 10m (~ RSSI < -80 dBm)
    enum class DistanceBucket(val label: String, val approxDistanceText: String) {
        VERY_CLOSE("Immediate Proximity", "< 3 meters away"),
        NEAR("Near", "3 - 8 meters away"),
        MEDIUM("Medium Distance", "8 - 15 meters away"),
        FAR("Far", "> 15 meters away")
    }

    fun getDistanceBucket(rssi: Int): DistanceBucket {
        return when {
            rssi >= -60 -> DistanceBucket.VERY_CLOSE
            rssi >= -72 -> DistanceBucket.NEAR
            rssi >= -84 -> DistanceBucket.MEDIUM
            else -> DistanceBucket.FAR
        }
    }
}
