package com.brokencoders.narisuraksha.trigger

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Fake implementation of SosTrigger for UI testing and previews.
 */
class FakeSosTrigger : SosTrigger {
    private val _events = MutableSharedFlow<SosEvent>(replay = 1)
    override val events: Flow<SosEvent> = _events.asSharedFlow()

    fun emitEvent(event: SosEvent) {
        _events.tryEmit(event)
    }

    fun simulateTriggerCountdown() {
        _events.tryEmit(SosEvent.CountdownStarted)
    }

    fun simulateCancel() {
        _events.tryEmit(SosEvent.Cancelled)
    }

    fun simulateConfirmed(audioPath: String? = "/data/fake_audio.m4a", lat: Double? = 28.6139, lon: Double? = 77.2090) {
        _events.tryEmit(SosEvent.Confirmed(audioPath, lat, lon))
    }
}
