package com.brokencoders.narisuraksha.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import com.brokencoders.narisuraksha.core.Constants
import com.brokencoders.narisuraksha.core.PermissionHelper
import com.brokencoders.narisuraksha.core.SosPacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Broadcasts anonymous SOS packets over Bluetooth Low Energy (BLE).
 * Operates offline without internet, cell towers, or centralized servers.
 */
class BleAdvertiser(
    private val context: Context,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? get() = bluetoothManager?.adapter
    private var advertiser: BluetoothLeAdvertiser? = null

    private var isAdvertising = false
    private var timeoutJob: Job? = null

    private val callback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            isAdvertising = true
            Log.i(TAG, "BLE SOS Advertising started successfully")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            isAdvertising = false
            Log.e(TAG, "BLE SOS Advertising failed with error code: $errorCode")
        }
    }

    @SuppressLint("MissingPermission")
    fun startAdvertising(packet: SosPacket) {
        if (!PermissionHelper.hasBluetoothPermissions(context)) {
            Log.w(TAG, "Missing Bluetooth permissions to advertise")
            return
        }

        val adapter = bluetoothAdapter
        if (adapter == null || !adapter.isEnabled) {
            Log.w(TAG, "Bluetooth is disabled or unavailable")
            return
        }

        stopAdvertising()

        advertiser = adapter.bluetoothLeAdvertiser
        if (advertiser == null) {
            Log.e(TAG, "BLE Advertising not supported on this device")
            return
        }

        val encodedPacket = PacketCodec.encode(packet)
        val parcelUuid = ParcelUuid(Constants.SOS_SERVICE_UUID)

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .setTimeout(0) // handled by coroutine
            .build()

        val data = AdvertiseData.Builder()
            .addServiceUuid(parcelUuid)
            .addServiceData(parcelUuid, encodedPacket)
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .build()

        try {
            advertiser?.startAdvertising(settings, data, callback)
            Log.i(TAG, "Initiated BLE advertising for packet: $packet")

            // Schedule auto-stop after 60s
            timeoutJob?.cancel()
            timeoutJob = externalScope.launch {
                delay(Constants.SOS_ADVERTISE_TIMEOUT_MS)
                Log.i(TAG, "SOS Advertising timeout (60s) reached. Stopping advertisement.")
                stopAdvertising()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception starting BLE advertisement", e)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        timeoutJob?.cancel()
        timeoutJob = null

        if (isAdvertising && advertiser != null) {
            try {
                advertiser?.stopAdvertising(callback)
                Log.i(TAG, "BLE SOS Advertising stopped")
            } catch (e: Exception) {
                Log.e(TAG, "Exception stopping BLE advertisement", e)
            }
        }
        isAdvertising = false
    }

    companion object {
        private const val TAG = "BleAdvertiser"
    }
}
