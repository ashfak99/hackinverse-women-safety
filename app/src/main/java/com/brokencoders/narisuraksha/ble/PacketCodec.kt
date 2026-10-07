package com.brokencoders.narisuraksha.ble

import com.brokencoders.narisuraksha.core.SosPacket
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Encodes and decodes SosPacket to/from compact 15-byte binary format.
 *
 * Byte structure (15 bytes total):
 * [0..1]   - senderId (Short, 2 bytes)
 * [2..5]   - timestamp (Int epoch seconds, 4 bytes)
 * [6..9]   - latitude (Float IEEE 754, 4 bytes)
 * [10..13] - longitude (Float IEEE 754, 4 bytes)
 * [14]     - flags (Byte, 1 byte)
 */
object PacketCodec {

    const val PACKET_SIZE_BYTES = 15

    fun encode(packet: SosPacket): ByteArray {
        val buffer = ByteBuffer.allocate(PACKET_SIZE_BYTES)
        buffer.order(ByteOrder.BIG_ENDIAN)
        buffer.putShort(packet.senderId)
        buffer.putInt(packet.timestamp)
        buffer.putFloat(packet.lat)
        buffer.putFloat(packet.lon)
        buffer.put(packet.flags)
        return buffer.array()
    }

    fun decode(bytes: ByteArray?): SosPacket? {
        if (bytes == null || bytes.size < PACKET_SIZE_BYTES) {
            return null
        }

        return try {
            val buffer = ByteBuffer.wrap(bytes)
            buffer.order(ByteOrder.BIG_ENDIAN)
            val senderId = buffer.short
            val timestamp = buffer.int
            val lat = buffer.float
            val lon = buffer.float
            val flags = buffer.get()

            SosPacket(
                senderId = senderId,
                timestamp = timestamp,
                lat = lat,
                lon = lon,
                flags = flags
            )
        } catch (e: Exception) {
            null
        }
    }
}
