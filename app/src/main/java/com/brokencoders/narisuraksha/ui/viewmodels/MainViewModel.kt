package com.brokencoders.narisuraksha.ui.viewmodels

import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.brokencoders.narisuraksha.ble.BleTransport
import com.brokencoders.narisuraksha.ble.ReceivedSos
import com.brokencoders.narisuraksha.core.DeviceIdProvider
import com.brokencoders.narisuraksha.core.SosPacket
import com.brokencoders.narisuraksha.data.UserPreferencesRepository
import com.brokencoders.narisuraksha.service.ScanForegroundService
import com.brokencoders.narisuraksha.service.SosForegroundService
import com.brokencoders.narisuraksha.trigger.ShakeDetector
import com.brokencoders.narisuraksha.trigger.SosEvent
import com.brokencoders.narisuraksha.trigger.SosManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val context: Context,
    private val sosManager: SosManager,
    private val bleTransport: BleTransport,
    private val shakeDetector: ShakeDetector,
    private val preferencesRepository: UserPreferencesRepository,
    private val deviceIdProvider: DeviceIdProvider
) : ViewModel() {

    val isCountingDown: StateFlow<Boolean> = sosManager.isCountingDown
    val countdownSeconds: StateFlow<Int> = sosManager.countdownSeconds
    val isSosActive: StateFlow<Boolean> = sosManager.isSosActive

    val isShakeEnabled: StateFlow<Boolean> = preferencesRepository.isShakeDetectionEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val isScanEnabled: StateFlow<Boolean> = preferencesRepository.isGuardianScanEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val isDecoyEnabled: StateFlow<Boolean> = preferencesRepository.isDecoyModeEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val secretDecoyCode: StateFlow<String> = preferencesRepository.secretDecoyCode
        .stateIn(viewModelScope, SharingStarted.Eagerly, "1234")

    val isOnboardingCompleted: StateFlow<Boolean> = preferencesRepository.isOnboardingCompleted
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val anonymousDeviceId: Short get() = deviceIdProvider.deviceId

    private val _latestReceivedAlert = MutableStateFlow<ReceivedSos?>(null)
    val latestReceivedAlert: StateFlow<ReceivedSos?> = _latestReceivedAlert.asStateFlow()

    private val _responderAckCount = MutableStateFlow(0)
    val responderAckCount: StateFlow<Int> = _responderAckCount.asStateFlow()
    // Tracks unique responder IDs who have acknowledged this device's current SOS beacon
    private val acknowledgedResponders = mutableSetOf<Short>()

    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()

    private val _isSirenOn = MutableStateFlow(false)
    val isSirenOn: StateFlow<Boolean> = _isSirenOn.asStateFlow()

    private var alarmRingtone: Ringtone? = null

    init {
        // Collect SOS confirmation to launch SosForegroundService
        viewModelScope.launch {
            sosManager.events.collectLatest { event ->
                when (event) {
                    is SosEvent.CountdownStarted -> {
                        Log.i(TAG, "SosEvent: Countdown started")
                        acknowledgedResponders.clear()
                        _responderAckCount.value = 0
                    }
                    is SosEvent.Cancelled -> {
                        Log.i(TAG, "SosEvent: SOS Cancelled")
                        acknowledgedResponders.clear()
                        _responderAckCount.value = 0
                        SosForegroundService.stop(context)
                    }
                    is SosEvent.Confirmed -> {
                        Log.i(TAG, "SosEvent: SOS Confirmed! Launching broadcast service...")
                        acknowledgedResponders.clear()
                        _responderAckCount.value = 0
                        SosForegroundService.start(
                            context = context,
                            lat = event.lat,
                            lon = event.lon,
                            audioPath = event.audioPath
                        )
                    }
                }
            }
        }

        // Collect incoming BLE alerts & ACKs
        viewModelScope.launch {
            bleTransport.received.collectLatest { receivedSos ->
                if (receivedSos.packet.isAck) {
                    val targetId = receivedSos.packet.targetSenderId
                    val myDeviceId = deviceIdProvider.deviceId
                    // Only count ACK if addressed to our device (or 0 for legacy broadcast ACK)
                    if (targetId == 0.toShort() || targetId == myDeviceId) {
                        val responderId = receivedSos.packet.senderId
                        if (acknowledgedResponders.add(responderId)) {
                            Log.i(TAG, "Distinct responder #$responderId confirmed our distress beacon. Total responders: ${acknowledgedResponders.size}")
                            _responderAckCount.value = acknowledgedResponders.size
                        } else {
                            Log.d(TAG, "Duplicate ACK from responder #$responderId ignored")
                        }
                    } else {
                        Log.d(TAG, "Ignoring ACK intended for sender #$targetId (my deviceId is #$myDeviceId)")
                    }
                } else {
                    _latestReceivedAlert.value = receivedSos
                }
            }
        }

        // Collect shake events to trigger SOS if shake detection is enabled
        viewModelScope.launch {
            shakeDetector.shakeEvents.collectLatest {
                if (isShakeEnabled.value) {
                    Log.i(TAG, "Shake event received! Triggering SOS countdown...")
                    triggerSos()
                }
            }
        }

        // Sync shake detector state
        viewModelScope.launch {
            preferencesRepository.isShakeDetectionEnabled.collectLatest { enabled ->
                if (enabled) {
                    shakeDetector.start()
                } else {
                    shakeDetector.stop()
                }
            }
        }
    }

    fun triggerSos() {
        acknowledgedResponders.clear()
        _responderAckCount.value = 0
        sosManager.triggerSos()
    }

    fun cancelCountdown() {
        acknowledgedResponders.clear()
        _responderAckCount.value = 0
        sosManager.cancelCountdown()
    }

    fun stopSos() {
        acknowledgedResponders.clear()
        _responderAckCount.value = 0
        sosManager.stopSos()
        SosForegroundService.stop(context)
    }

    fun sendResponderAck(targetSenderId: Short) {
        Log.i(TAG, "Sending responder ACK beacon targeting sender #$targetSenderId")
        val ackPacket = SosPacket.create(
            senderId = deviceIdProvider.deviceId,
            lat = null,
            lon = null,
            isAck = true,
            targetSenderId = targetSenderId
        )
        bleTransport.startAdvertising(ackPacket)
    }

    fun updateSecretDecoyCode(newPin: String) {
        if (newPin.length == 4 && newPin.all { it.isDigit() }) {
            viewModelScope.launch {
                preferencesRepository.setSecretDecoyCode(newPin)
                Log.i(TAG, "Decoy PIN updated to: $newPin")
            }
        }
    }

    fun setOnboardingCompleted(completed: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setOnboardingCompleted(completed)
        }
    }

    fun setShakeDetectionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setShakeDetectionEnabled(enabled)
        }
    }

    fun setGuardianScanEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setGuardianScanEnabled(enabled)
            if (enabled) {
                ScanForegroundService.start(context)
            } else {
                ScanForegroundService.stop(context)
            }
        }
    }

    fun setDecoyModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDecoyModeEnabled(enabled)
        }
    }

    fun toggleFlashlight() {
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            val cameraId = cameraManager?.cameraIdList?.firstOrNull() ?: return
            val newState = !_isFlashlightOn.value
            cameraManager.setTorchMode(cameraId, newState)
            _isFlashlightOn.value = newState
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling flashlight", e)
        }
    }

    fun toggleSiren() {
        try {
            val newState = !_isSirenOn.value
            if (newState) {
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                alarmRingtone = RingtoneManager.getRingtone(context, alarmUri)?.apply {
                    audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                    play()
                }
                _isSirenOn.value = true
            } else {
                alarmRingtone?.stop()
                alarmRingtone = null
                _isSirenOn.value = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling siren", e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        alarmRingtone?.stop()
    }

    companion object {
        private const val TAG = "MainViewModel"

        fun provideFactory(
            context: Context,
            sosManager: SosManager,
            bleTransport: BleTransport,
            shakeDetector: ShakeDetector,
            preferencesRepository: UserPreferencesRepository,
            deviceIdProvider: DeviceIdProvider
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(
                    context,
                    sosManager,
                    bleTransport,
                    shakeDetector,
                    preferencesRepository,
                    deviceIdProvider
                ) as T
            }
        }
    }
}
