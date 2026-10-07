package com.brokencoders.narisuraksha.trigger

sealed interface SosEvent {
    data object CountdownStarted : SosEvent
    data object Cancelled : SosEvent
    data class Confirmed(val audioPath: String?, val lat: Double?, val lon: Double?) : SosEvent
}
