package com.brokencoders.narisuraksha

import com.brokencoders.narisuraksha.trigger.ShakeDetectionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ShakeDetectionEngineTest {

    private lateinit var engine: ShakeDetectionEngine

    // 3.0g acceleration vector on X axis (3.0 * 9.80665 m/s^2 ≈ 29.42 m/s^2)
    private val spikeX = 30.0f
    private val spikeY = 0.0f
    private val spikeZ = 0.0f

    // Weak 1.2g normal movement (1.2 * 9.80665 m/s^2 ≈ 11.77 m/s^2)
    private val weakX = 11.8f
    private val weakY = 0.0f
    private val weakZ = 0.0f

    @Before
    fun setUp() {
        engine = ShakeDetectionEngine(
            thresholdG = 2.7f,
            requiredSpikes = 3,
            windowMs = 2000L,
            cooldownMs = 3000L,
            minIntervalMs = 220L
        )
    }

    @Test
    fun test0SpikesNoSos() {
        val triggered = engine.processAcceleration(weakX, weakY, weakZ, 1000L)
        assertFalse(triggered)
        assertEquals(0, engine.diagnostics.value.spikeCount)
    }

    @Test
    fun test1SpikeNoSos() {
        val triggered = engine.processAcceleration(spikeX, spikeY, spikeZ, 1000L)
        assertFalse(triggered)
        assertEquals(1, engine.diagnostics.value.spikeCount)
    }

    @Test
    fun test2SpikesNoSos() {
        engine.processAcceleration(spikeX, spikeY, spikeZ, 1000L)
        val triggered = engine.processAcceleration(spikeX, spikeY, spikeZ, 1300L)
        assertFalse(triggered)
        assertEquals(2, engine.diagnostics.value.spikeCount)
    }

    @Test
    fun test3ValidSpikesWithin2SecondsTriggersSos() {
        // Spike 1 at t=1000ms
        assertFalse(engine.processAcceleration(spikeX, spikeY, spikeZ, 1000L))
        assertEquals(1, engine.diagnostics.value.spikeCount)

        // Spike 2 at t=1300ms
        assertFalse(engine.processAcceleration(spikeX, spikeY, spikeZ, 1300L))
        assertEquals(2, engine.diagnostics.value.spikeCount)

        // Spike 3 at t=1600ms (all within 600ms <= 2000ms window)
        val triggered = engine.processAcceleration(spikeX, spikeY, spikeZ, 1600L)
        assertTrue("3 valid spikes within 2 seconds must trigger SOS", triggered)
        assertEquals(0, engine.diagnostics.value.spikeCount) // Reset after trigger
        assertTrue(engine.diagnostics.value.isCooldownActive)
    }

    @Test
    fun test3SpikesOutsideWindowNoTrigger() {
        // Spike 1 at t=1000ms
        engine.processAcceleration(spikeX, spikeY, spikeZ, 1000L)
        // Spike 2 at t=1500ms
        engine.processAcceleration(spikeX, spikeY, spikeZ, 1500L)
        // Spike 3 at t=3500ms (2500ms after Spike 1, so Spike 1 has expired from the 2000ms window)
        val triggered = engine.processAcceleration(spikeX, spikeY, spikeZ, 3500L)
        assertFalse("Spikes spaced beyond 2000ms window must not trigger SOS", triggered)
        // Spike 1 expired, so count should be Spike 2 + Spike 3 = 2
        assertEquals(2, engine.diagnostics.value.spikeCount)
    }

    @Test
    fun testSpikeDuringCooldownIgnored() {
        // Trigger SOS at t=1600ms
        engine.processAcceleration(spikeX, spikeY, spikeZ, 1000L)
        engine.processAcceleration(spikeX, spikeY, spikeZ, 1300L)
        val triggered = engine.processAcceleration(spikeX, spikeY, spikeZ, 1600L)
        assertTrue(triggered)

        // Cooldown is 3000ms (until t=4600ms). Spikes during cooldown must be ignored.
        val duringCooldown1 = engine.processAcceleration(spikeX, spikeY, spikeZ, 2000L)
        assertFalse(duringCooldown1)
        val duringCooldown2 = engine.processAcceleration(spikeX, spikeY, spikeZ, 3000L)
        assertFalse(duringCooldown2)
        assertEquals(0, engine.diagnostics.value.spikeCount)

        // After cooldown expires at t=5000ms, new spikes can be detected
        val afterCooldown = engine.processAcceleration(spikeX, spikeY, spikeZ, 5000L)
        assertFalse(afterCooldown)
        assertEquals(1, engine.diagnostics.value.spikeCount)
    }

    @Test
    fun testWeakAccelerationIgnored() {
        // Feed normal movements (e.g. walking or table placement at 1.2g)
        for (i in 1..10) {
            val triggered = engine.processAcceleration(weakX, weakY, weakZ, 1000L + (i * 250L))
            assertFalse(triggered)
        }
        assertEquals(0, engine.diagnostics.value.spikeCount)
    }

    @Test
    fun testSingleBumpMechanicalOscillationsDebounced() {
        // When a phone hits a table, accelerometer vibrates with multiple samples in 50ms
        // Spike at t=1000ms
        engine.processAcceleration(spikeX, spikeY, spikeZ, 1000L)
        // Rapid bounce at t=1020ms (< 220ms debounce threshold)
        engine.processAcceleration(spikeX, spikeY, spikeZ, 1020L)
        // Rapid bounce at t=1050ms (< 220ms debounce threshold)
        val triggered = engine.processAcceleration(spikeX, spikeY, spikeZ, 1050L)

        assertFalse("Single-impact bounce must be debounced and not trigger SOS", triggered)
        assertEquals(1, engine.diagnostics.value.spikeCount)
    }

    @Test
    fun testSensorUnavailableGracefulHandling() {
        engine.updateListeningState(isListening = false, isSensorAvailable = false)
        assertFalse(engine.diagnostics.value.isSensorAvailable)
        assertFalse(engine.diagnostics.value.isListening)
    }
}
