package com.brokencoders.narisuraksha.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
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
 * Uses compact 16-bit Service UUID + 15-byte Service Data (total 23 bytes in AD structure),
 * strictly guaranteeing it fits within legacy 31-byte advertisement limits across all chipsets.
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
            Log.i(TAG, "BLE SOS Advertising started successfully (Packet fits in legacy 31B)")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            isAdvertising = false
            val errorMsg = when (errorCode) {
                ADVERTISE_FAILED_DATA_TOO_LARGE -> "ADVERTISE_FAILED_DATA_TOO_LARGE (Code 1)"
                ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> "ADVERTISE_FAILED_TOO_MANY_ADVERTISERS (Code 2)"
                ADVERTISE_FAILED_ALREADY_STARTED -> "ADVERTISE_FAILED_ALREADY_STARTED (Code 3)"
                ADVERTISE_FAILED_INTERNAL_ERROR -> "ADVERTISE_FAILED_INTERNAL_ERROR (Code 4)"
                ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> "ADVERTISE_FAILED_FEATURE_UNSUPPORTED (Code 5)"
                else -> "Error Code $errorCode"
            }
            Log.e(TAG, "BLE SOS Advertising failed: $errorMsg")
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
            Log.e(TAG, "BLE Advertising not supported on this device chipset")
            return
        }

        val encodedPacket = PacketCodec.encode(packet)

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .setTimeout(0) // handled by coroutine
            .build()

        // 16-bit Service UUID (4 bytes) + 16-bit Service Data (19 bytes) = 23 bytes total!
        // Easily fits inside 31-byte legacy limit without chipset truncation.
        val data = AdvertiseData.Builder()
            .addServiceUuid(Constants.SOS_PARCEL_UUID)
            .addServiceData(Constants.SOS_PARCEL_UUID, encodedPacket)
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .build()

        // Scan response adds manufacturer data fallback
        val scanResponse = AdvertiseData.Builder()
            .addManufacturerData(Constants.MANUFACTURER_ID, encodedPacket)
            .setIncludeDeviceName(false)
            .build()

        try {
            advertiser?.startAdvertising(settings, data, scanResponse, callback)
            Log.i(TAG, "Initiated BLE advertising for packet: $packet (${encodedPacket.size} bytes)")

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
