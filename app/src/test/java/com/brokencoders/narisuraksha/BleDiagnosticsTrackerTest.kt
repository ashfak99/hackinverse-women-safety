package com.brokencoders.narisuraksha

import com.brokencoders.narisuraksha.ble.BleDiagnosticsTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BleDiagnosticsTrackerTest {

    @Before
    fun setUp() {
        BleDiagnosticsTracker.clearLogs()
    }

    @Test
    fun testInitialCountersAreZero() {
        assertEquals(0, BleDiagnosticsTracker.packetsSentCount.value)
        assertEquals(0, BleDiagnosticsTracker.packetsReceivedCount.value)
        assertEquals(0, BleDiagnosticsTracker.acksSentCount.value)
        assertEquals(0, BleDiagnosticsTracker.acksReceivedCount.value)
        assertEquals(0, BleDiagnosticsTracker.rejectedPacketsCount.value)
    }

    @Test
    fun testCounterIncrements() {
        BleDiagnosticsTracker.incrementPacketsSent()
        BleDiagnosticsTracker.incrementPacketsSent()
        assertEquals(2, BleDiagnosticsTracker.packetsSentCount.value)

        BleDiagnosticsTracker.incrementPacketsReceived()
        assertEquals(1, BleDiagnosticsTracker.packetsReceivedCount.value)

        BleDiagnosticsTracker.incrementAcksSent()
        assertEquals(1, BleDiagnosticsTracker.acksSentCount.value)

        BleDiagnosticsTracker.incrementAcksReceived()
        BleDiagnosticsTracker.incrementAcksReceived()
        assertEquals(2, BleDiagnosticsTracker.acksReceivedCount.value)

        BleDiagnosticsTracker.incrementRejectedPackets()
        assertEquals(1, BleDiagnosticsTracker.rejectedPacketsCount.value)
    }

    @Test
    fun testDebugScanModeToggle() {
        BleDiagnosticsTracker.setDebugScanMode(true)
        assertTrue(BleDiagnosticsTracker.debugScanModeEnabled.value)

        BleDiagnosticsTracker.setDebugScanMode(false)
        org.junit.Assert.assertFalse(BleDiagnosticsTracker.debugScanModeEnabled.value)
    }

    @Test
    fun testRecordEventAndRingBufferCapacity() {
        for (i in 1..100) {
            BleDiagnosticsTracker.recordEvent("TEST_TAG", "Message #$i")
        }

        val logs = BleDiagnosticsTracker.logs.value
        // Capacity is capped at 80 items
        assertEquals(80, logs.size)
        // Most recent event should be at index 0
        assertEquals("Message #100", logs[0].message)
    }
}
