package com.brokencoders.narisuraksha

import android.app.Application
import android.util.Log
import com.brokencoders.narisuraksha.ble.BleAdvertiser
import com.brokencoders.narisuraksha.ble.BleScanner
import com.brokencoders.narisuraksha.ble.BleTransport
import com.brokencoders.narisuraksha.ble.BleTransportImpl
import com.brokencoders.narisuraksha.capture.AudioRecorder
import com.brokencoders.narisuraksha.capture.LocationProvider
import com.brokencoders.narisuraksha.core.DeviceIdProvider
import com.brokencoders.narisuraksha.core.PermissionHelper
import com.brokencoders.narisuraksha.data.AppDatabase
import com.brokencoders.narisuraksha.data.UserPreferencesRepository
import com.brokencoders.narisuraksha.service.NotificationHelper
import com.brokencoders.narisuraksha.service.ScanForegroundService
import com.brokencoders.narisuraksha.trigger.ShakeDetector
import com.brokencoders.narisuraksha.trigger.SosManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NariSurakshaApp : Application() {

    private val applicationScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this) }
    val emergencyContactDao by lazy { database.emergencyContactDao() }
    val preferencesRepository by lazy { UserPreferencesRepository(this) }
    val deviceIdProvider by lazy { DeviceIdProvider(this) }
    val audioRecorder by lazy { AudioRecorder(this) }
    val locationProvider by lazy { LocationProvider(this) }
    val sosManager by lazy { SosManager(this, audioRecorder, locationProvider, applicationScope) }

    val bleAdvertiser by lazy { BleAdvertiser(this, applicationScope) }
    val bleScanner by lazy { BleScanner(this, deviceIdProvider) }
    val bleTransport: BleTransport by lazy { BleTransportImpl(bleAdvertiser, bleScanner) }

    val shakeDetector by lazy { ShakeDetector(this) }
    val notificationHelper by lazy { NotificationHelper(this) }

    override fun onCreate() {
        super.onCreate()
        Log.i("NariSurakshaApp", "Application initialized with Anonymous Device ID: ${deviceIdProvider.deviceId}")

        // Initialize Background Guardian Scanner if enabled
        applicationScope.launch {
            val isScanEnabled = preferencesRepository.isGuardianScanEnabled.first()
            if (isScanEnabled && PermissionHelper.hasBluetoothPermissions(this@NariSurakshaApp)) {
                try {
                    ScanForegroundService.start(this@NariSurakshaApp)
                } catch (e: Exception) {
                    Log.w("NariSurakshaApp", "Could not start ScanForegroundService at launch: ${e.message}")
                }
            }
        }
    }
}
