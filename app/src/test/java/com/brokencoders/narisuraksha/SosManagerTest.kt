package com.brokencoders.narisuraksha

import com.brokencoders.narisuraksha.capture.AudioEvidenceCapture
import com.brokencoders.narisuraksha.capture.Coordinates
import com.brokencoders.narisuraksha.capture.LocationCoordinateProvider
import com.brokencoders.narisuraksha.core.Constants
import com.brokencoders.narisuraksha.trigger.SosEvent
import com.brokencoders.narisuraksha.trigger.SosManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SosManagerTest {

    private class FakeAudioRecorder : AudioEvidenceCapture {
        var startRecordingCalled = false
        var stopRecordingCalled = false
        var returnPath: String? = "/data/user/0/recordings/test.m4a"

        override fun startRecording(): String? {
            startRecordingCalled = true
            return returnPath
        }

        override fun stopRecording(): String? {
            stopRecordingCalled = true
            return returnPath
        }
    }

    private class FakeLocationProvider : LocationCoordinateProvider {
        var getCurrentLocationCalled = false
        var mockCoordinates: Coordinates? = Coordinates(lat = 28.6139, lon = 77.2090)

        private val _currentCoordinates = MutableStateFlow<Coordinates?>(mockCoordinates)
        override val currentCoordinates: StateFlow<Coordinates?> = _currentCoordinates

        override suspend fun getCurrentLocation(): Coordinates? {
            getCurrentLocationCalled = true
            return mockCoordinates
        }
    }

    private lateinit var fakeAudioRecorder: FakeAudioRecorder
    private lateinit var fakeLocationProvider: FakeLocationProvider
    private lateinit var testScope: CoroutineScope
    private lateinit var sosManager: SosManager

    @Before
    fun setUp() {
        fakeAudioRecorder = FakeAudioRecorder()
        fakeLocationProvider = FakeLocationProvider()
        testScope = CoroutineScope(Dispatchers.Default)
        // Use 25ms step delay so the 3-step countdown completes in 75ms during tests
        sosManager = SosManager(
            context = null,
            audioRecorder = fakeAudioRecorder,
            locationProvider = fakeLocationProvider,
            externalScope = testScope,
            countdownStepDelayMs = 25L
        )
    }

    @Test
    fun testCountdownDurationIsThreeSeconds() {
        assertEquals(3, Constants.COUNTDOWN_DURATION_SECONDS)
        assertEquals(3, sosManager.countdownSeconds.value)
    }

    @Test
    fun testFullSosCountdownAndConfirmation() = runBlocking {
        assertFalse(sosManager.isCountingDown.value)
        assertFalse(sosManager.isSosActive.value)

        val collectedEvents = mutableListOf<SosEvent>()
        val collectJob = launch {
            sosManager.events.collect { collectedEvents.add(it) }
        }

        sosManager.triggerSos()

        assertTrue(sosManager.isCountingDown.value)
        assertFalse(sosManager.isSosActive.value)

        // Wait for 3-second countdown (3 * 25ms = 75ms, plus buffer)
        kotlinx.coroutines.delay(180L)

        assertFalse(sosManager.isCountingDown.value)
        assertTrue(sosManager.isSosActive.value)
        assertEquals(0, sosManager.countdownSeconds.value)

        assertTrue(fakeAudioRecorder.startRecordingCalled)
        assertTrue(fakeLocationProvider.getCurrentLocationCalled)

        assertTrue(collectedEvents.any { it is SosEvent.CountdownStarted })
        val confirmedEvent = collectedEvents.filterIsInstance<SosEvent.Confirmed>().firstOrNull()
        assertNotNull(confirmedEvent)
        assertEquals(28.6139, confirmedEvent!!.lat!!, 0.0001)
        assertEquals(77.2090, confirmedEvent.lon!!, 0.0001)
        assertEquals("/data/user/0/recordings/test.m4a", confirmedEvent.audioPath)

        collectJob.cancel()
    }

    @Test
    fun testCancelCountdownAbortsSos() = runBlocking {
        val collectedEvents = mutableListOf<SosEvent>()
        val collectJob = launch {
            sosManager.events.collect { collectedEvents.add(it) }
        }

        sosManager.triggerSos()
        assertTrue(sosManager.isCountingDown.value)

        // Cancel during countdown
        sosManager.cancelCountdown()

        assertFalse(sosManager.isCountingDown.value)
        assertFalse(sosManager.isSosActive.value)
        assertEquals(3, sosManager.countdownSeconds.value)

        // Wait to verify countdown does not proceed
        kotlinx.coroutines.delay(150L)

        assertFalse(sosManager.isSosActive.value)
        assertFalse(fakeAudioRecorder.startRecordingCalled)
        assertTrue(collectedEvents.any { it is SosEvent.Cancelled })
        assertFalse(collectedEvents.any { it is SosEvent.Confirmed })

        collectJob.cancel()
    }

    @Test
    fun testDuplicateTriggerIgnoredDuringCountdown() = runBlocking {
        sosManager.triggerSos()
        assertTrue(sosManager.isCountingDown.value)

        // Second trigger while counting down must be ignored
        sosManager.triggerSos()
        assertTrue(sosManager.isCountingDown.value)

        sosManager.cancelCountdown()
    }

    @Test
    fun testStopSosStopsRecordingAndResets() = runBlocking {
        sosManager.triggerSos()
        kotlinx.coroutines.delay(150L)
        assertTrue(sosManager.isSosActive.value)

        sosManager.stopSos()
        assertFalse(sosManager.isSosActive.value)
        assertTrue(fakeAudioRecorder.stopRecordingCalled)
    }

    @Test
    fun testSosConfirmedEvenIfAudioFails() = runBlocking {
        fakeAudioRecorder.returnPath = null // Audio failed to initialize

        val collectedEvents = mutableListOf<SosEvent>()
        val collectJob = launch {
            sosManager.events.collect { collectedEvents.add(it) }
        }

        sosManager.triggerSos()
        kotlinx.coroutines.delay(150L)

        assertTrue(sosManager.isSosActive.value)
        val confirmed = collectedEvents.filterIsInstance<SosEvent.Confirmed>().firstOrNull()
        assertNotNull(confirmed)
        assertNull(confirmed!!.audioPath) // Audio path is null, but SOS confirmed successfully!

        collectJob.cancel()
    }
}
