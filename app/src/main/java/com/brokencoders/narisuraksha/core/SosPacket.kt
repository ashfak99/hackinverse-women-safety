package com.brokencoders.narisuraksha.core

/**
 * Core SOS Packet contract as defined in specification.
 * Legacy BLE advertising payload is ~31 bytes max.
 * In wire format (AD Record 2 Service Data), the payload occupies 17 bytes:
 * - magic byte (0x53 'S'): 1 byte
 * - version (0x01): 1 byte
 * - senderId: 2 bytes
 * - timestamp: 4 bytes (or targetSenderId in ACK beacons)
 * - lat: 4 bytes
 * - lon: 4 bytes
 * - flags: 1 byte
 *
 * (Also backwards-compatible with legacy 15-byte un-versioned packets.)
 */
data class SosPacket(
    val senderId: Short,     // random anonymous ID generated at first launch
    val timestamp: Int,      // epoch seconds for distress; or targetSenderId for ACK
    val lat: Float,
    val lon: Float,
    val flags: Byte,
) {
    val isSos: Boolean get() = (flags.toInt() and FLAG_SOS) != 0
    val isLocationUnavailable: Boolean get() = (flags.toInt() and FLAG_LOCATION_UNAVAILABLE) != 0
    val isAck: Boolean get() = (flags.toInt() and FLAG_ACK) != 0
    val isTest: Boolean get() = (flags.toInt() and FLAG_TEST) != 0

    /**
     * In an ACK beacon, the [timestamp] field carries the targeted original sender's ID.
     * Returns 0 for broadcast ACK (target == 0) or for non-ACK packets.
     */
    val targetSenderId: Short
        get() = if (isAck) (timestamp and 0xFFFF).toShort() else 0

    companion object {
        const val FLAG_SOS: Int = 1 shl 0
        const val FLAG_LOCATION_UNAVAILABLE: Int = 1 shl 1
        const val FLAG_ACK: Int = 1 shl 2
        const val FLAG_TEST: Int = 1 shl 3

        fun create(
            senderId: Short,
            lat: Double? = null,
            lon: Double? = null,
            timestampSeconds: Int = (System.currentTimeMillis() / 1000L).toInt(),
            isAck: Boolean = false,
            targetSenderId: Short = 0,
            isTest: Boolean = false
        ): SosPacket {
            // ---- Flags ----
            // ACK wins over TEST when both are set (test ACK is still an ACK,
            // and must be routable via targetSenderId).
            var flagAccumulator = 0
            when {
                isAck -> {
                    flagAccumulator = flagAccumulator or FLAG_ACK
                    if (isTest) flagAccumulator = flagAccumulator or FLAG_TEST
                }
                isTest -> {
                    flagAccumulator = flagAccumulator or FLAG_TEST
                }
                else -> {
                    flagAccumulator = flagAccumulator or FLAG_SOS
                }
            }

            // ---- Coordinates ----
            val finalLat: Float
            val finalLon: Float
            if (lat != null && lon != null) {
                finalLat = lat.toFloat()
                finalLon = lon.toFloat()
            } else {
                finalLat = 0.0f
                finalLon = 0.0f
                flagAccumulator = flagAccumulator or FLAG_LOCATION_UNAVAILABLE
            }

            // ---- Timestamp field ----
            // For ACK packets the timestamp slot ALWAYS carries targetSenderId,
            // including 0 (= broadcast ACK). Otherwise decoding would round-trip
            // a real epoch value into a garbage Short and silently drop the ACK.
            val finalTimestamp = if (isAck) {
                targetSenderId.toInt() and 0xFFFF
            } else {
                timestampSeconds
            }

            return SosPacket(
                senderId = senderId,
                timestamp = finalTimestamp,
                lat = finalLat,
                lon = finalLon,
                flags = flagAccumulator.toByte()
            )
        }
    }
}