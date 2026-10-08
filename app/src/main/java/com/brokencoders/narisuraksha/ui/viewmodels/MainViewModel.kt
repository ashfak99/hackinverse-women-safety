package com.brokencoders.narisuraksha.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.brokencoders.narisuraksha.ble.BleAdvertiser
import com.brokencoders.narisuraksha.ble.BleDiagnosticsTracker
import com.brokencoders.narisuraksha.ble.BleScanner
import com.brokencoders.narisuraksha.ble.BleTransport
import com.brokencoders.narisuraksha.ble.ReceivedSos
import com.brokencoders.narisuraksha.capture.Coordinates
import com.brokencoders.narisuraksha.capture.LocationProvider
import com.brokencoders.narisuraksha.core.DeviceIdProvider
import com.brokencoders.narisuraksha.core.PermissionHelper
import com.brokencoders.narisuraksha.core.SosPacket
import com.brokencoders.narisuraksha.data.EmergencyContactDao
import com.brokencoders.narisuraksha.data.EmergencyContactEntity
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(
    private val context: Context,
    private val sosManager: SosManager,
    private val bleTransport: BleTransport,
    private val shakeDetector: ShakeDetector,
    private val preferencesRepository: UserPreferencesRepository,
    private val deviceIdProvider: DeviceIdProvider,
    private val emergencyContactDao: EmergencyContactDao,
    private val locationProvider: LocationProvider,
    private val bleAdvertiser: BleAdvertiser,
    private val bleScanner: BleScanner
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

    // Location Telemetry
    val currentCoordinates: StateFlow<Coordinates?> = locationProvider.currentCoordinates

    // Emergency Contacts from Room
    val emergencyContacts: StateFlow<List<EmergencyContactEntity>> = emergencyContactDao.getAllContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val primaryContact: StateFlow<EmergencyContactEntity?> = emergencyContactDao.getPrimaryContact()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // BLE Diagnostics
    val bleDiagnostics = BleDiagnosticsTracker

    // Shake Diagnostics
    val shakeDiagnostics: StateFlow<com.brokencoders.narisuraksha.trigger.ShakeDiagnostics> = shakeDetector.diagnostics

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

    private val _isTestBroadcasting = MutableStateFlow(false)
    val isTestBroadcasting: StateFlow<Boolean> = _isTestBroadcasting.asStateFlow()

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
                            Log.i(TAG, "Distinct responder #$responderId confirmed distress. Total: ${acknowledgedResponders.size}")
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

        // Fetch location on startup
        refreshLocation()
    }

    /**
     * Proactively verifies that background guardian scan is running if user has enabled it.
     */
    fun ensureGuardianScanActive() {
        if (isScanEnabled.value && PermissionHelper.hasBluetoothScanPermission(context)) {
            try {
                ScanForegroundService.start(context)
            } catch (e: Exception) {
                Log.w(TAG, "Could not start ScanForegroundService: ${e.message}")
            }
        }
    }

    fun refreshLocation() {
        viewModelScope.launch {
            locationProvider.getCurrentLocation()
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

    /**
     * Starts a harmless 10-second test broadcast flagged with FLAG_TEST.
     * Displays in nearby debug screens without triggering sirens or emergency workflows.
     */
    fun startTestBroadcast() {
        if (_isTestBroadcasting.value) return
        _isTestBroadcasting.value = true

        val coords = currentCoordinates.value
        val testPacket = SosPacket.create(
            senderId = deviceIdProvider.deviceId,
            lat = coords?.lat,
            lon = coords?.lon,
            isTest = true
        )

        bleDiagnostics.recordEvent(
            "NARI_BLE_ADVERTISE_START",
            "Broadcasting 10-second TEST beacon (FLAG_TEST set)"
        )
        bleAdvertiser.startAdvertising(testPacket, timeoutMs = 10_000L)

        viewModelScope.launch {
            kotlinx.coroutines.delay(10_000L)
            _isTestBroadcasting.value = false
        }
    }

    fun stopTestBroadcast() {
        bleAdvertiser.stopAdvertising()
        _isTestBroadcasting.value = false
    }

    fun restartBleScan() {
        bleScanner.restartScanning()
    }

    fun toggleDebugScanMode(enabled: Boolean) {
        bleDiagnostics.setDebugScanMode(enabled)
        bleScanner.restartScanning()
    }

    // ==========================================
    // Emergency Contacts Management
    // ==========================================

    fun addEmergencyContact(name: String, phoneNumber: String, relationship: String, isPrimary: Boolean) {
        if (name.isBlank() || phoneNumber.isBlank()) return
        viewModelScope.launch {
            val contact = EmergencyContactEntity(
                name = name.trim(),
                phoneNumber = phoneNumber.trim(),
                relationship = relationship.trim().ifEmpty { "Contact" },
                isPrimary = isPrimary
            )
            val newId = emergencyContactDao.insertContact(contact)
            if (isPrimary) {
                emergencyContactDao.setAsPrimary(newId)
            }
        }
    }

    fun updateEmergencyContact(contact: EmergencyContactEntity) {
        viewModelScope.launch {
            emergencyContactDao.updateContact(contact)
            if (contact.isPrimary) {
                emergencyContactDao.setAsPrimary(contact.id)
            }
        }
    }

    fun deleteEmergencyContact(contact: EmergencyContactEntity) {
        viewModelScope.launch {
            emergencyContactDao.deleteContact(contact)
        }
    }

    fun setPrimaryContact(contactId: Long) {
        viewModelScope.launch {
            emergencyContactDao.setAsPrimary(contactId)
        }
    }

    /**
     * Directly launches phone dialer intent for the contact.
     */
    fun callContact(phoneNumber: String) {
        try {
            val cleanPhone = phoneNumber.filter { it.isDigit() || it == '+' }
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open dialer: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Composes pre-filled emergency SOS SMS for contact.
     */
    fun sendEmergencySms(phoneNumber: String, customLocationText: String? = null) {
        try {
            val cleanPhone = phoneNumber.filter { it.isDigit() || it == '+' }
            val coords = currentCoordinates.value
            val locationString = when {
                customLocationText != null -> customLocationText
                coords != null -> "Lat: ${coords.lat}, Lon: ${coords.lon} (Accuracy: ±${coords.accuracy?.toInt() ?: 15}m)"
                else -> "Location unavailable (Offline)"
            }
            val time = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault()).format(Date())

            val message = """
                Nari-Suraksha SOS ALERT

                I may be in danger and need help.

                Location:
                $locationString

                Time:
                $time

                Please contact me or emergency services immediately.
            """.trimIndent()

            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$cleanPhone")
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open SMS app: ${e.message}", Toast.LENGTH_SHORT).show()
        }
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
            if (completed) {
                ensureGuardianScanActive()
            }
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
            deviceIdProvider: DeviceIdProvider,
            emergencyContactDao: EmergencyContactDao,
            locationProvider: LocationProvider,
            bleAdvertiser: BleAdvertiser,
            bleScanner: BleScanner
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(
                    context,
                    sosManager,
                    bleTransport,
                    shakeDetector,
                    preferencesRepository,
                    deviceIdProvider,
                    emergencyContactDao,
                    locationProvider,
                    bleAdvertiser,
                    bleScanner
                ) as T
            }
        }
    }
}
