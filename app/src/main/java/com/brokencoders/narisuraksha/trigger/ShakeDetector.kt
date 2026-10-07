package com.brokencoders.narisuraksha.trigger

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.brokencoders.narisuraksha.core.Constants
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.math.sqrt

/**
 * Shake detector that uses the accelerometer.
 * Triggers when 3 spikes > 2.7g occur within a 2-second window, with a 3-second cooldown.
 */
class ShakeDetector(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _shakeEvents = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val shakeEvents: Flow<Unit> = _shakeEvents.asSharedFlow()

    private val spikeTimestamps = ArrayDeque<Long>()
    private var lastTriggerTimestamp: Long = 0L
    private var isListening = false

    fun start() {
        if (isListening || accelerometer == null || sensorManager == null) return
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        isListening = true
        Log.d(TAG, "ShakeDetector started")
    }

    fun stop() {
        if (!isListening || sensorManager == null) return
        sensorManager.unregisterListener(this)
        isListening = false
        spikeTimestamps.clear()
        Log.d(TAG, "ShakeDetector stopped")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val now = System.currentTimeMillis()

        // Enforce cooldown
        if (now - lastTriggerTimestamp < Constants.SHAKE_COOLDOWN_MS) {
            return
        }

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Calculate total g-force (Earth gravity is ~9.80665 m/s^2 = 1.0g)
        val acceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val gForce = acceleration / SensorManager.GRAVITY_EARTH

        if (gForce >= Constants.SHAKE_THRESHOLD_G) {
            // Evict spikes outside the sliding window (2 seconds)
            while (spikeTimestamps.isNotEmpty() && now - spikeTimestamps.first() > Constants.SHAKE_SPIKE_WINDOW_MS) {
                spikeTimestamps.removeFirst()
            }

            spikeTimestamps.addLast(now)

            if (spikeTimestamps.size >= Constants.SHAKE_REQUIRED_SPIKES) {
                Log.i(TAG, "Shake pattern detected! Triggering SOS countdown.")
                lastTriggerTimestamp = now
                spikeTimestamps.clear()
                _shakeEvents.tryEmit(Unit)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    companion object {
        private const val TAG = "ShakeDetector"
    }
}
