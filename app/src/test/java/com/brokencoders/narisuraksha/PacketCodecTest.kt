package com.brokencoders.narisuraksha

import com.brokencoders.narisuraksha.ble.PacketCodec
import com.brokencoders.narisuraksha.core.SosPacket
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PacketCodecTest {

    @Test
    fun testEncodeDecodeRoundTrip() {
        val original = SosPacket(
            senderId = 12345.toShort(),
            timestamp = 1717171717,
            lat = 28.613939f,
            lon = 77.209023f,
            flags = SosPacket.FLAG_SOS.toByte()
        )

        val encoded = PacketCodec.encode(original)
        // 1 magic + 1 version + 2 sender + 4 timestamp + 4 lat + 4 lon + 1 flags = 17 bytes
        assertEquals(17, encoded.size)
        assertEquals(0x53.toByte(), encoded[0]) // Magic byte 'S'
        assertEquals(0x01.toByte(), encoded[1]) // Protocol version

        val decoded = PacketCodec.decode(encoded)
        assertNotNull(decoded)
        assertEquals(original.senderId, decoded!!.senderId)
        assertEquals(original.timestamp, decoded.timestamp)
        assertEquals(original.lat, decoded.lat, 0.0001f)
        assertEquals(original.lon, decoded.lon, 0.0001f)
        assertEquals(original.flags, decoded.flags)
        assertTrue(decoded.isSos)
    }

    @Test
    fun testLegacy15ByteDecodeFallback() {
        // Construct raw 15-byte packet without magic/version header
        val legacyBuffer = java.nio.ByteBuffer.allocate(15)
        legacyBuffer.order(java.nio.ByteOrder.BIG_ENDIAN)
        legacyBuffer.putShort(999.toShort())
        legacyBuffer.putInt(1650000000)
        legacyBuffer.putFloat(12.34f)
        legacyBuffer.putFloat(56.78f)
        legacyBuffer.put(SosPacket.FLAG_SOS.toByte())

        val decoded = PacketCodec.decode(legacyBuffer.array())
        assertNotNull(decoded)
        assertEquals(999.toShort(), decoded!!.senderId)
        assertEquals(1650000000, decoded.timestamp)
        assertEquals(12.34f, decoded.lat, 0.0001f)
        assertEquals(56.78f, decoded.lon, 0.0001f)
        assertTrue(decoded.isSos)
    }

    @Test
    fun testAckTargetSenderId() {
        val ackPacket = SosPacket.create(
            senderId = 555.toShort(),
            lat = null,
            lon = null,
            isAck = true,
            targetSenderId = 12345.toShort()
        )

        val encoded = PacketCodec.encode(ackPacket)
        val decoded = PacketCodec.decode(encoded)
        assertNotNull(decoded)
        assertTrue(decoded!!.isAck)
        assertEquals(555.toShort(), decoded.senderId)
        assertEquals(12345.toShort(), decoded.targetSenderId)
    }

    @Test
    fun testLocationUnavailableFlag() {
        val packet = SosPacket.create(
            senderId = (-4321).toShort(),
            lat = null,
            lon = null,
            timestampSeconds = 1600000000
        )

        val encoded = PacketCodec.encode(packet)
        assertEquals(17, encoded.size)
        val decoded = PacketCodec.decode(encoded)

        assertNotNull(decoded)
        assertTrue(decoded!!.isLocationUnavailable)
        assertTrue(decoded.isSos)
        assertEquals(0.0f, decoded.lat, 0.0001f)
        assertEquals(0.0f, decoded.lon, 0.0001f)
    }
}
