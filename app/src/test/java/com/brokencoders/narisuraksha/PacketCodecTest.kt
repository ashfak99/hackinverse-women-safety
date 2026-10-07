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
        assertEquals(15, encoded.size)

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
    fun testLocationUnavailableFlag() {
        val packet = SosPacket.create(
            senderId = (-4321).toShort(),
            lat = null,
            lon = null,
            timestampSeconds = 1600000000
        )

        val encoded = PacketCodec.encode(packet)
        val decoded = PacketCodec.decode(encoded)

        assertNotNull(decoded)
        assertTrue(decoded!!.isLocationUnavailable)
        assertTrue(decoded.isSos)
        assertEquals(0.0f, decoded.lat, 0.0001f)
        assertEquals(0.0f, decoded.lon, 0.0001f)
    }
}
