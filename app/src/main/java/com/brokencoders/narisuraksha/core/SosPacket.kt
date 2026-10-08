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
 * (Also backwards-compatible with legacy 15-byte un-versioned packets).
 */
data class SosPacket(
    val senderId: Short,     // random anonymous ID generated at first launch
    val timestamp: Int,      // epoch seconds for distress; or targetSenderId for ACK
    val lat: Float,
    val lon: Float,
    val flags: Byte          // bit0 = SOS, bit1 = location unavailable, bit2 = ACK
) {
    val isSos: Boolean get() = (flags.toInt() and FLAG_SOS) != 0
    val isLocationUnavailable: Boolean get() = (flags.toInt() and FLAG_LOCATION_UNAVAILABLE) != 0
    val isAck: Boolean get() = (flags.toInt() and FLAG_ACK) != 0

    // In an ACK beacon, the timestamp field carries the targeted original sender's ID
    val targetSenderId: Short get() = if (isAck) (timestamp and 0xFFFF).toShort() else 0

    companion object {
        const val FLAG_SOS: Int = 1 shl 0
        const val FLAG_LOCATION_UNAVAILABLE: Int = 1 shl 1
        const val FLAG_ACK: Int = 1 shl 2

        fun create(
            senderId: Short,
            lat: Double?,
            lon: Double?,
            timestampSeconds: Int = (System.currentTimeMillis() / 1000L).toInt(),
            isAck: Boolean = false,
            targetSenderId: Short = 0
        ): SosPacket {
            var flagAccumulator = 0
            if (!isAck) {
                flagAccumulator = flagAccumulator or FLAG_SOS
            } else {
                flagAccumulator = flagAccumulator or FLAG_ACK
            }

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

            val finalTimestamp = if (isAck && targetSenderId != 0.toShort()) {
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
