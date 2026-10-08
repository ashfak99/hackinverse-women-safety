package com.brokencoders.narisuraksha.core

import android.os.ParcelUuid
import java.util.UUID

object Constants {
    // 16-bit BLE Service UUID (0xFDE1) mapped into standard Bluetooth Base UUID.
    // Encodes in 16-bit form (4 bytes in AD structure), leaving ample room in 31-byte legacy adverts.
    // Note: 0xFDE1 falls in the Bluetooth SIG-allocated 16-bit member range (suitable for hackathon prototyping).
    // A commercial production release would register an official UUID with the Bluetooth SIG or adopt a custom 128-bit UUID.
    val SOS_SERVICE_UUID: UUID = UUID.fromString("0000FDE1-0000-1000-8000-00805F9B34FB")
    val SOS_PARCEL_UUID: ParcelUuid = ParcelUuid(SOS_SERVICE_UUID)

    // Fallback 128-bit UUID for backwards compatibility
    val SOS_LEGACY_128_UUID: UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")

    // Legacy fallback manufacturer ID
    const val MANUFACTURER_ID = 0xFFFF

    // Timeout for BLE advertising in milliseconds (60 seconds)
    const val SOS_ADVERTISE_TIMEOUT_MS = 60_000L

    // Countdown duration in seconds
    const val COUNTDOWN_DURATION_SECONDS = 3

    // Shake detection thresholds
    const val SHAKE_THRESHOLD_G = 2.7f // ~2.7g
    const val SHAKE_SPIKE_WINDOW_MS = 2000L // 2 seconds window
    const val SHAKE_REQUIRED_SPIKES = 3
    const val SHAKE_COOLDOWN_MS = 3000L // 3 seconds cooldown between alerts
    const val SHAKE_MIN_SPIKE_INTERVAL_MS = 220L // Minimum separation between distinct shake strokes

    // Rate limiting for incoming packets per sender ID
    const val PACKET_RATE_LIMIT_MS = 10_000L

    // Global alert rate limiting across all senders (mitigates spoofed ID-rotation flood attacks)
    const val GLOBAL_ALERT_RATE_LIMIT_MAX_PER_MINUTE = 10
    const val GLOBAL_ALERT_RATE_LIMIT_WINDOW_MS = 60_000L

    // Notification Channel IDs
    const val CHANNEL_ALERT_ID = "nari_emergency_alert_channel"
    const val CHANNEL_ALERT_NAME = "Emergency Alerts"
    const val CHANNEL_SERVICE_ID = "calc_service_channel"
    const val CHANNEL_SERVICE_NAME = "Background Service"

    const val NOTIFICATION_ID_SERVICE = 1001
    const val NOTIFICATION_ID_ALERT_BASE = 2000
    const val NOTIFICATION_ID_SOS_ACTIVE = 1002

    // Approximate Distance RSSI buckets (calibrated for noisy BLE propagation)
    // Near: < 3m (~ RSSI > -65 dBm)
    // Medium: 3m - 10m (~ RSSI between -65 and -80 dBm)
    // Far: > 10m (~ RSSI < -80 dBm)
    enum class DistanceBucket(val label: String, val approxDistanceText: String) {
        VERY_CLOSE("Immediate Proximity", "Approx. < 3 meters away"),
        NEAR("Near", "Approx. 3 - 10 meters away"),
        FAR("Far", "Approx. > 10 meters away")
    }

    fun getDistanceBucket(rssi: Int): DistanceBucket {
        return when {
            rssi >= -65 -> DistanceBucket.VERY_CLOSE
            rssi >= -80 -> DistanceBucket.NEAR
            else -> DistanceBucket.FAR
        }
    }
}
