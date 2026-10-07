package com.brokencoders.narisuraksha.core

/**
 * Core SOS Packet contract as defined in specification.
 * Legacy BLE advertising payload is ~31 bytes max.
 * This packet occupies 15 bytes in binary form:
 * - senderId: 2 bytes
 * - timestamp: 4 bytes
 * - lat: 4 bytes
 * - lon: 4 bytes
 * - flags: 1 byte
 */
data class SosPacket(
    val senderId: Short,     // random anonymous ID generated at first launch
    val timestamp: Int,      // epoch seconds
    val lat: Float,
    val lon: Float,
    val flags: Byte          // bit0 = SOS, bit1 = location unavailable, bit2 = ACK
) {
    val isSos: Boolean get() = (flags.toInt() and FLAG_SOS) != 0
    val isLocationUnavailable: Boolean get() = (flags.toInt() and FLAG_LOCATION_UNAVAILABLE) != 0
    val isAck: Boolean get() = (flags.toInt() and FLAG_ACK) != 0

    companion object {
        const val FLAG_SOS: Int = 1 shl 0
        const val FLAG_LOCATION_UNAVAILABLE: Int = 1 shl 1
        const val FLAG_ACK: Int = 1 shl 2

        fun create(
            senderId: Short,
            lat: Double?,
            lon: Double?,
            timestampSeconds: Int = (System.currentTimeMillis() / 1000L).toInt(),
            isAck: Boolean = false
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

            return SosPacket(
                senderId = senderId,
                timestamp = timestampSeconds,
                lat = finalLat,
                lon = finalLon,
                flags = flagAccumulator.toByte()
            )
        }
    }
}
