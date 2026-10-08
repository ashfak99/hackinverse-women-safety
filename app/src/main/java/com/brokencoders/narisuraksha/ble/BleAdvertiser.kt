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
 * Uses compact 16-bit Service UUID + 17-byte Service Data (total 25 bytes in AD structure),
 * strictly guaranteeing it fits within legacy 31-byte advertisement limits across all chipsets.
 */
class BleAdvertiser(
    private val context: Context,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? get() = bluetoothManager?.adapter
    private var advertiser: BluetoothLeAdvertiser? = null

    var isAdvertising = false
        private set

    private var timeoutJob: Job? = null
    private var lastPacket: SosPacket? = null

    private val callback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            isAdvertising = true
            BleDiagnosticsTracker.setAdvertising(true)
            BleDiagnosticsTracker.incrementPacketsSent()

            val packetDesc = if (lastPacket?.isAck == true) {
                "ACK to #${lastPacket?.targetSenderId}"
            } else if (lastPacket?.isTest == true) {
                "TEST SOS #${lastPacket?.senderId}"
            } else {
                "DISTRESS SOS #${lastPacket?.senderId}"
            }

            BleDiagnosticsTracker.recordEvent(
                "NARI_BLE_ADVERTISE_SUCCESS",
                "BLE Broadcast active [$packetDesc] (LowLatency, HighTx)"
            )
            Log.i(TAG, "BLE SOS Advertising started successfully: $packetDesc")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            isAdvertising = false
            BleDiagnosticsTracker.setAdvertising(false)
            val errorMsg = when (errorCode) {
                ADVERTISE_FAILED_DATA_TOO_LARGE -> "ADVERTISE_FAILED_DATA_TOO_LARGE (Code 1)"
                ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> "ADVERTISE_FAILED_TOO_MANY_ADVERTISERS (Code 2)"
                ADVERTISE_FAILED_ALREADY_STARTED -> "ADVERTISE_FAILED_ALREADY_STARTED (Code 3)"
                ADVERTISE_FAILED_INTERNAL_ERROR -> "ADVERTISE_FAILED_INTERNAL_ERROR (Code 4)"
                ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> "ADVERTISE_FAILED_FEATURE_UNSUPPORTED (Code 5)"
                else -> "Error Code $errorCode"
            }
            BleDiagnosticsTracker.recordEvent(
                "NARI_BLE_ADVERTISE_FAILURE",
                "BLE Advertising failed: $errorMsg",
                isError = true
            )
            Log.e(TAG, "BLE SOS Advertising failed: $errorMsg")

            // Auto-recovery for physical chipsets that reject scanResponse
            if (errorCode == ADVERTISE_FAILED_DATA_TOO_LARGE || errorCode == ADVERTISE_FAILED_INTERNAL_ERROR) {
                lastPacket?.let { pkt ->
                    BleDiagnosticsTracker.recordEvent(
                        "NARI_BLE_ADVERTISE_START",
                        "Attempting single-frame fallback advertising without scan response..."
                    )
                    attemptSingleFrameFallback(pkt)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startAdvertising(packet: SosPacket, timeoutMs: Long = Constants.SOS_ADVERTISE_TIMEOUT_MS) {
        lastPacket = packet

        if (!PermissionHelper.hasBluetoothAdvertisePermission(context)) {
            val msg = "Missing BLUETOOTH_ADVERTISE permission"
            BleDiagnosticsTracker.recordEvent("NARI_BLE_ADVERTISE_FAILURE", msg, isError = true)
            Log.w(TAG, msg)
            return
        }

        val adapter = bluetoothAdapter
        if (adapter == null || !adapter.isEnabled) {
            val msg = "Bluetooth is disabled or unavailable"
            BleDiagnosticsTracker.recordEvent("NARI_BLE_ADVERTISE_FAILURE", msg, isError = true)
            Log.w(TAG, msg)
            return
        }

        stopAdvertising()

        advertiser = adapter.bluetoothLeAdvertiser
        if (advertiser == null) {
            val msg = "BLE Advertising not supported on this device chipset"
            BleDiagnosticsTracker.recordEvent("NARI_BLE_ADVERTISE_FAILURE", msg, isError = true)
            Log.e(TAG, msg)
            return
        }

        val encodedPacket = PacketCodec.encode(packet)
        val logTag = if (packet.isAck) "NARI_BLE_ACK_SENT" else "NARI_BLE_ADVERTISE_START"
        BleDiagnosticsTracker.recordEvent(
            logTag,
            "Broadcasting packet: senderId=#${packet.senderId}, ack=${packet.isAck}, test=${packet.isTest}, target=#${packet.targetSenderId}, bytes=${encodedPacket.size}"
        )

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .setTimeout(0) // handled by coroutine timeout
            .build()

        // AD Record 1 (16-bit Service UUID: 4 bytes) + AD Record 2 (16-bit Service Data: 21 bytes) = 25 bytes total.
        // Easily fits inside 31-byte legacy advertising packet limit without chipset truncation.
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

            // Schedule auto-stop
            timeoutJob?.cancel()
            timeoutJob = externalScope.launch {
                delay(timeoutMs)
                BleDiagnosticsTracker.recordEvent("NARI_BLE_ADVERTISE_START", "Advertising timeout (${timeoutMs / 1000}s) reached. Stopping.")
                stopAdvertising()
            }
        } catch (e: Exception) {
            BleDiagnosticsTracker.recordEvent("NARI_BLE_ADVERTISE_FAILURE", "Exception: ${e.message}", isError = true)
            Log.e(TAG, "Exception starting BLE advertisement", e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun attemptSingleFrameFallback(packet: SosPacket) {
        try {
            val encodedPacket = PacketCodec.encode(packet)
            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(false)
                .build()

            val data = AdvertiseData.Builder()
                .addServiceData(Constants.SOS_PARCEL_UUID, encodedPacket)
                .setIncludeDeviceName(false)
                .build()

            advertiser?.startAdvertising(settings, data, callback)
        } catch (e: Exception) {
            BleDiagnosticsTracker.recordEvent("NARI_BLE_ADVERTISE_FAILURE", "Fallback attempt failed: ${e.message}", isError = true)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        timeoutJob?.cancel()
        timeoutJob = null

        if (isAdvertising && advertiser != null) {
            try {
                advertiser?.stopAdvertising(callback)
                BleDiagnosticsTracker.recordEvent("NARI_BLE_ADVERTISE_START", "BLE SOS Advertising stopped")
                Log.i(TAG, "BLE SOS Advertising stopped")
            } catch (e: Exception) {
                Log.e(TAG, "Exception stopping BLE advertisement", e)
            }
        }
        isAdvertising = false
        BleDiagnosticsTracker.setAdvertising(false)
    }

    companion object {
        private const val TAG = "BleAdvertiser"
    }
}
