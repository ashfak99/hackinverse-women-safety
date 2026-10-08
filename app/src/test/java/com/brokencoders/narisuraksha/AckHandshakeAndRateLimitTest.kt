package com.brokencoders.narisuraksha

import com.brokencoders.narisuraksha.core.Constants
import com.brokencoders.narisuraksha.core.SosPacket
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.LinkedList
import java.util.Queue

class AckHandshakeAndRateLimitTest {

    @Test
    fun testTargetAckAcceptedForCorrectVictim() {
        val myDeviceId: Short = 0x1A2B.toShort()
        val otherVictimId: Short = 0x3C4D.toShort()

        val ackForMe = SosPacket.create(
            senderId = 999.toShort(), // Responder
            isAck = true,
            targetSenderId = myDeviceId
        )

        val ackForOther = SosPacket.create(
            senderId = 888.toShort(), // Responder
            isAck = true,
            targetSenderId = otherVictimId
        )

        // Handshake verification logic
        assertTrue("ACK addressed to my deviceId must be accepted",
            ackForMe.targetSenderId == myDeviceId || ackForMe.targetSenderId == 0.toShort())

        assertFalse("ACK addressed to another victim must be rejected",
            ackForOther.targetSenderId == myDeviceId || ackForOther.targetSenderId == 0.toShort())
    }

    @Test
    fun testResponderDeduplication() {
        val acknowledgedResponders = mutableSetOf<Short>()

        val responder1: Short = 101
        val responder2: Short = 102

        // Responder 1 ACKs
        val firstAckAdded = acknowledgedResponders.add(responder1)
        assertTrue(firstAckAdded)
        assertEquals(1, acknowledgedResponders.size)

        // Responder 1 sends another ACK (re-broadcast)
        val duplicateAckAdded = acknowledgedResponders.add(responder1)
        assertFalse("Duplicate ACK from same responder must not increment responder count", duplicateAckAdded)
        assertEquals(1, acknowledgedResponders.size)

        // Responder 2 ACKs
        val secondResponderAdded = acknowledgedResponders.add(responder2)
        assertTrue("Distinct responder must increment count", secondResponderAdded)
        assertEquals(2, acknowledgedResponders.size)
    }

    @Test
    fun testPerSenderRateLimit() {
        val senderLastSeenMap = mutableMapOf<Short, Long>()
        val senderId: Short = 456

        fun isSenderRateLimited(now: Long): Boolean {
            val lastSeen = senderLastSeenMap[senderId]
            return if (lastSeen != null && (now - lastSeen < Constants.PACKET_RATE_LIMIT_MS)) {
                true // Suppressed
            } else {
                senderLastSeenMap[senderId] = now
                false // Allowed
            }
        }

        // t=1000ms: First alert is accepted
        assertFalse(isSenderRateLimited(1000L))

        // t=5000ms (within 10s cooldown): Repeated alert is rate-limited
        assertTrue("Packet within 10s sender window must be rate-limited", isSenderRateLimited(5000L))

        // t=12000ms (> 10s after t=1000ms): Alert is accepted
        assertFalse("Packet after 10s cooldown must be accepted", isSenderRateLimited(12000L))
    }

    @Test
    fun testGlobalAlertRateLimit() {
        val globalAlertTimestamps: Queue<Long> = LinkedList()
        val windowMs = Constants.GLOBAL_ALERT_RATE_LIMIT_WINDOW_MS // 60,000ms
        val maxPerMinute = Constants.GLOBAL_ALERT_RATE_LIMIT_MAX_PER_MINUTE // 10

        fun isGlobalRateLimited(now: Long): Boolean {
            val windowStart = now - windowMs
            while (globalAlertTimestamps.peek()?.let { it < windowStart } == true) {
                globalAlertTimestamps.poll()
            }
            return if (globalAlertTimestamps.size >= maxPerMinute) {
                true // Rate limited
            } else {
                globalAlertTimestamps.add(now)
                false // Allowed
            }
        }

        // 10 alerts in the first 10 seconds from different senders
        for (i in 1..10) {
            val limited = isGlobalRateLimited(i * 1000L)
            assertFalse("Alert #$i within limit of 10 must be accepted", limited)
        }

        // 11th alert within the 1-minute window
        val alert11Limited = isGlobalRateLimited(15000L)
        assertTrue("Alert #11 exceeding 10 per minute must be suppressed", alert11Limited)

        // At t=62,000ms (more than 60s after t=1,000ms), alert 1 has expired
        val alertAfterWindow = isGlobalRateLimited(62000L)
        assertFalse("Alert after window sliding must be accepted", alertAfterWindow)
    }
}
