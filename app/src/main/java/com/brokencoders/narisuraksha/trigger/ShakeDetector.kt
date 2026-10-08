package com.brokencoders.narisuraksha.trigger

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Shake detector that uses the accelerometer.
 * Triggers when 3 spikes > 2.7g occur within a 2-second window, with a 3-second cooldown.
 * Includes minimum spike interval debouncing to eliminate false positives from phone placement or drops.
 */
class ShakeDetector(
    context: Context,
    val engine: ShakeDetectionEngine = ShakeDetectionEngine()
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _shakeEvents = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val shakeEvents: Flow<Unit> = _shakeEvents.asSharedFlow()

    val diagnostics: StateFlow<ShakeDiagnostics> get() = engine.diagnostics

    val isSensorAvailable: Boolean get() = accelerometer != null && sensorManager != null
    var isListening: Boolean = false
        private set

    init {
        engine.updateListeningState(isListening = false, isSensorAvailable = isSensorAvailable)
    }

    @Synchronized
    fun start() {
        if (isListening || !isSensorAvailable) {
            engine.updateListeningState(isListening = isListening, isSensorAvailable = isSensorAvailable)
            return
        }
        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        isListening = true
        engine.updateListeningState(isListening = true, isSensorAvailable = true)
        Log.d(TAG, "ShakeDetector started")
    }

    @Synchronized
    fun stop() {
        if (!isListening) return
        sensorManager?.unregisterListener(this)
        isListening = false
        engine.reset()
        engine.updateListeningState(isListening = false, isSensorAvailable = isSensorAvailable)
        Log.d(TAG, "ShakeDetector stopped")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val now = System.currentTimeMillis()
        val triggered = engine.processAcceleration(
            x = event.values[0],
            y = event.values[1],
            z = event.values[2],
            timestampMs = now
        )

        if (triggered) {
            Log.i(TAG, "Shake pattern detected! Triggering SOS countdown.")
            _shakeEvents.tryEmit(Unit)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    companion object {
        private const val TAG = "ShakeDetector"
    }
}
