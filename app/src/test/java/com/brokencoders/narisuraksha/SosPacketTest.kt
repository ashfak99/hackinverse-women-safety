package com.brokencoders.narisuraksha

import com.brokencoders.narisuraksha.core.SosPacket
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SosPacketTest {

    @Test
    fun testDefaultSosPacketFlags() {
        val packet = SosPacket.create(
            senderId = 100.toShort(),
            lat = 28.5,
            lon = 77.3
        )

        assertTrue(packet.isSos)
        assertFalse(packet.isAck)
        assertFalse(packet.isTest)
        assertFalse(packet.isLocationUnavailable)
        assertEquals(100.toShort(), packet.senderId)
    }

    @Test
    fun testAckPacketFlags() {
        val ack = SosPacket.create(
            senderId = 200.toShort(),
            isAck = true,
            targetSenderId = 100.toShort()
        )

        assertFalse(ack.isSos)
        assertTrue(ack.isAck)
        assertFalse(ack.isTest)
        assertEquals(100.toShort(), ack.targetSenderId)
    }

    @Test
    fun testTestBroadcastPacketFlags() {
        val testPacket = SosPacket.create(
            senderId = 300.toShort(),
            isTest = true
        )

        assertFalse(testPacket.isSos)
        assertFalse(testPacket.isAck)
        assertTrue(testPacket.isTest)
    }

    @Test
    fun testLocationUnavailableFlags() {
        val noLocationPacket = SosPacket.create(
            senderId = 400.toShort(),
            lat = null,
            lon = null
        )

        assertTrue(noLocationPacket.isSos)
        assertTrue(noLocationPacket.isLocationUnavailable)
        assertEquals(0.0f, noLocationPacket.lat, 0.0001f)
        assertEquals(0.0f, noLocationPacket.lon, 0.0001f)
    }

    @Test
    fun testTargetSenderIdPackingAndUnpacking() {
        val targetId: Short = 0x1234.toShort()
        val packet = SosPacket.create(
            senderId = 99.toShort(),
            isAck = true,
            targetSenderId = targetId
        )

        assertEquals(targetId, packet.targetSenderId)
    }
}
