package com.brokencoders.narisuraksha.ble

import com.brokencoders.narisuraksha.core.SosPacket
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Encodes and decodes SosPacket to/from binary format.
 *
 * Wire specification (17 bytes total):
 * [0]      - Magic Byte (0x53 'S', 1 byte)
 * [1]      - Protocol Version (0x01, 1 byte)
 * [2..3]   - senderId (Short, 2 bytes)
 * [4..7]   - timestamp (Int epoch seconds / targetSenderId for ACK, 4 bytes)
 * [8..11]  - latitude (Float IEEE 754, 4 bytes)
 * [12..15] - longitude (Float IEEE 754, 4 bytes)
 * [16]     - flags (Byte, 1 byte)
 *
 * Total Service Data length in AD structure = 1 (Type 0x16) + 2 (UUID 0xFDE1) + 17 = 20 bytes (0x14).
 * Backwards-compatible with un-versioned 15-byte packets.
 */
object PacketCodec {

    const val MAGIC_BYTE: Byte = 0x53 // ASCII 'S'
    const val PROTOCOL_VERSION: Byte = 0x01
    const val PACKET_SIZE_BYTES = 17
    const val LEGACY_PACKET_SIZE_BYTES = 15

    fun encode(packet: SosPacket): ByteArray {
        val buffer = ByteBuffer.allocate(PACKET_SIZE_BYTES)
        buffer.order(ByteOrder.BIG_ENDIAN)
        buffer.put(MAGIC_BYTE)
        buffer.put(PROTOCOL_VERSION)
        buffer.putShort(packet.senderId)
        buffer.putInt(packet.timestamp)
        buffer.putFloat(packet.lat)
        buffer.putFloat(packet.lon)
        buffer.put(packet.flags)
        return buffer.array()
    }

    fun decode(bytes: ByteArray?): SosPacket? {
        if (bytes == null || bytes.size < LEGACY_PACKET_SIZE_BYTES) {
            return null
        }

        return try {
            val buffer = ByteBuffer.wrap(bytes)
            buffer.order(ByteOrder.BIG_ENDIAN)

            if (bytes.size >= PACKET_SIZE_BYTES && bytes[0] == MAGIC_BYTE) {
                val magic = buffer.get() // 0x53
                val version = buffer.get() // 0x01
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
            } else {
                // Fallback for legacy 15-byte payload
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
            }
        } catch (e: Exception) {
            null
        }
    }
}
