
package com.brokencoders.narisuraksha.ble

import com.brokencoders.narisuraksha.core.SosPacket
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Compact binary encoder/decoder for SosPacket.
 *
 * Versioned packet (17 bytes):
 * [0]      - Magic byte
 * [1]      - Protocol version
 * [2..3]   - Sender ID
 * [4..7]   - Timestamp
 * [8..11]  - Latitude
 * [12..15] - Longitude
 * [16]     - Flags
 *
 * Legacy packet: 15 bytes.
 */
object PacketCodec {

    const val MAGIC_BYTE: Byte = 0x53
    const val PROTOCOL_VERSION: Byte = 0x01

    const val PACKET_SIZE_BYTES = 17
    const val LEGACY_PACKET_SIZE_BYTES = 15

    fun encode(packet: SosPacket): ByteArray? {
        return encode(packet, PACKET_SIZE_BYTES)
    }

    fun encode(packet: SosPacket, maxSize: Int): ByteArray? {
        if (maxSize < LEGACY_PACKET_SIZE_BYTES) return null

        val buffer = ByteBuffer.allocate(PACKET_SIZE_BYTES)
            .order(ByteOrder.BIG_ENDIAN)

        buffer.put(MAGIC_BYTE)
        buffer.put(PROTOCOL_VERSION)
        buffer.putShort(packet.senderId)
        buffer.putInt(packet.timestamp)
        buffer.putFloat(packet.lat)
        buffer.putFloat(packet.lon)
        buffer.put(packet.flags)

        val encoded = buffer.array()

        return when {
            encoded.size <= maxSize -> encoded
            maxSize >= LEGACY_PACKET_SIZE_BYTES -> {
                val legacy = ByteBuffer.allocate(LEGACY_PACKET_SIZE_BYTES)
                    .order(ByteOrder.BIG_ENDIAN)

                legacy.putShort(packet.senderId)
                legacy.putInt(packet.timestamp)
                legacy.putFloat(packet.lat)
                legacy.putFloat(packet.lon)
                legacy.put(packet.flags)

                legacy.array()
            }
            else -> null
        }
    }

    fun decode(bytes: ByteArray?): SosPacket? {
        if (bytes == null) return null

        return try {
            val buffer = ByteBuffer.wrap(bytes)
                .order(ByteOrder.BIG_ENDIAN)

            when {
                bytes.size == PACKET_SIZE_BYTES &&
                    bytes[0] == MAGIC_BYTE &&
                    bytes[1] == PROTOCOL_VERSION -> {

                    buffer.get() // Magic byte
                    buffer.get() // Protocol version

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

                bytes.size == LEGACY_PACKET_SIZE_BYTES -> {
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

                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}