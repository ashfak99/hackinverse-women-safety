package com.brokencoders.narisuraksha.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.Build
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
import java.util.UUID

class BleAdvertiser(
    private val context: Context,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? get() = bluetoothManager?.adapter
    private var advertiser: BluetoothLeAdvertiser? = null

    var isAdvertising = false
        private set

    private var timeoutJob: Job? = null
    private var lastPacket: SosPacket? = null

    // ---------------------------------------------------------------------
    // Session-ownership / concurrency state
    // ---------------------------------------------------------------------
    private val stateLock = Any()

    /** Packet currently owning the (single) advertising slot — set only on start success. */
    private var currentPacket: SosPacket? = null

    /** Packet whose startAdvertising() call is in-flight — correlates callback. */
    private var packetBeingStarted: SosPacket? = null

    /** ACKs deferred because an SOS held the slot. FIFO to preserve ordering. */
    private val pendingAcks = ArrayDeque<SosPacket>()

    /** Guard against re-entering fallback on the same failure loop. */
    private var fallbackAttempted = false

    /**
     * API 26+ chipsets jo multiple advertising sets expose karte hain.
     * NOTE: parallel SOS+ACK path (startAdvertisingSet) abhi implement nahi hai —
     * ye flag future extension ke liye hai. Tab tak single-session priority guard
     * har chipset pe safe hai.
     */
    @Suppress("unused")
    val supportsParallelAdvertisingSets: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                bluetoothAdapter?.isMultipleAdvertisementSupported == true

    // ---------------------------------------------------------------------
    // Callback
    // ---------------------------------------------------------------------
    private val callback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)

            var startedPacket: SosPacket? = null
            synchronized(stateLock) {
                startedPacket = packetBeingStarted
                currentPacket = packetBeingStarted
                packetBeingStarted = null
            }

            isAdvertising = true
            BleDiagnosticsTracker.setAdvertising(true)
            BleDiagnosticsTracker.incrementPacketsSent()

            val packetDesc = when {
                startedPacket?.isAck == true -> "ACK to #${startedPacket?.targetSenderId}"
                startedPacket?.isTest == true -> "TEST SOS #${startedPacket?.senderId}"
                else -> "DISTRESS SOS #${startedPacket?.senderId}"
            }

            BleDiagnosticsTracker.recordEvent(
                "NARI_BLE_ADVERTISE_SUCCESS",
                "BLE Broadcast active [$packetDesc] (LowLatency, HighTx)"
            )
            Log.i(TAG, "BLE SOS Advertising started successfully: $packetDesc")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)

            var failedPacket: SosPacket? = null
            synchronized(stateLock) {
                failedPacket = packetBeingStarted ?: currentPacket
                currentPacket = null
                packetBeingStarted = null
            }

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

            val fp = failedPacket ?: return

            val isDataOrInternalError =
                errorCode == ADVERTISE_FAILED_DATA_TOO_LARGE ||
                errorCode == ADVERTISE_FAILED_INTERNAL_ERROR

            if (isDataOrInternalError && !fallbackAttempted) {
                fallbackAttempted = true
                synchronized(stateLock) { packetBeingStarted = fp }
                BleDiagnosticsTracker.recordEvent(
                    "NARI_BLE_ADVERTISE_START",
                    "Attempting minimal single-frame fallback (service data only)..."
                )
                attemptMinimalFallback(fp)
                return
            }

            // SOS slot released via failure → drain one queued ACK.
            if (fp.isSos) {
                drainPendingAcks()
            }
        }
    }

    // ---------------------------------------------------------------------
    // Start
    // ---------------------------------------------------------------------
    @SuppressLint("MissingPermission")
    fun startAdvertising(packet: SosPacket, timeoutMs: Long = Constants.SOS_ADVERTISE_TIMEOUT_MS) {

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

        // Priority guard — SOS > ACK on single-session chipsets.
        synchronized(stateLock) {
            val occupying = currentPacket ?: packetBeingStarted
            if (occupying?.isSos == true && packet.isAck) {
                Log.w(
                    TAG,
                    "ACK deferred: active SOS has priority " +
                            "(queued=${pendingAcks.size + 1}, target=#${packet.targetSenderId})"
                )
                pendingAcks.addLast(packet)
                BleDiagnosticsTracker.recordEvent(
                    "NARI_BLE_ACK_DEFERRED",
                    "ACK for #${packet.targetSenderId} deferred behind active SOS #${occupying.senderId}"
                )
                return
            }
        }

        // Slot swap. flushPendingAcks=false — warna recursion ho jayega.
        stopAdvertisingInternal(flushPendingAcks = false)

        lastPacket = packet
        synchronized(stateLock) { packetBeingStarted = packet }
        fallbackAttempted = false

        advertiser = adapter.bluetoothLeAdvertiser
        if (advertiser == null) {
            val msg = "BLE Advertising not supported on this device chipset"
            BleDiagnosticsTracker.recordEvent("NARI_BLE_ADVERTISE_FAILURE", msg, isError = true)
            Log.e(TAG, msg)
            synchronized(stateLock) {
                packetBeingStarted = null
                currentPacket = null
            }
            // SOS slot free hua → queued ACK ko chance do
            if (packet.isSos) drainPendingAcks()
            return
        }

        val encodedPacket = PacketCodec.encode(packet)
        val payloadPair = buildAdvertisePayloads(encodedPacket)

        if (payloadPair == null) {
            val msg = "Encoded packet (${encodedPacket.size} bytes) does not fit in " +
                    "legacy BLE advertising (${MAX_LEGACY_ADV_PAYLOAD} byte limit)"
            BleDiagnosticsTracker.recordEvent(
                "NARI_BLE_ADVERTISE_FAILURE", msg, isError = true
            )
            Log.e(TAG, msg)
            synchronized(stateLock) {
                packetBeingStarted = null
                currentPacket = null
            }
            if (packet.isSos) drainPendingAcks()
            return
        }

        val (data, scanResponse) = payloadPair

        val logTag = if (packet.isAck) "NARI_BLE_ACK_SENT" else "NARI_BLE_ADVERTISE_START"
        BleDiagnosticsTracker.recordEvent(
            logTag,
            "Broadcasting packet: senderId=#${packet.senderId}, ack=${packet.isAck}, " +
                    "test=${packet.isTest}, target=#${packet.targetSenderId}, " +
                    "bytes=${encodedPacket.size}, scanResp=${scanResponse != null}"
        )

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .setTimeout(0)
            .build()

        try {
            if (scanResponse != null) {
                advertiser?.startAdvertising(settings, data, scanResponse, callback)
            } else {
                advertiser?.startAdvertising(settings, data, callback)
            }
            Log.i(
                TAG,
                "Initiated BLE advertising for packet: $packet (${encodedPacket.size} bytes, " +
                        "scanResponse=${scanResponse != null})"
            )

            timeoutJob?.cancel()
            timeoutJob = externalScope.launch {
                delay(timeoutMs)
                BleDiagnosticsTracker.recordEvent(
                    "NARI_BLE_ADVERTISE_START",
                    "Advertising timeout (${timeoutMs / 1000}s) reached. Stopping."
                )
                stopAdvertising()
            }
        } catch (e: Exception) {
            BleDiagnosticsTracker.recordEvent(
                "NARI_BLE_ADVERTISE_FAILURE", "Exception: ${e.message}", isError = true
            )
            Log.e(TAG, "Exception starting BLE advertisement", e)
            synchronized(stateLock) {
                currentPacket = null
                packetBeingStarted = null
            }
            if (packet.isSos) drainPendingAcks()
        }
    }

    // ---------------------------------------------------------------------
    // Payload building + size validation
    // ---------------------------------------------------------------------

    /**
     * Builds the (main, scanResponse) AdvertiseData pair that fits within the
     * 31-byte legacy BLE advertising limit.
     *
     * Tiers (first that fits wins):
     *  1. Service data (main) + manufacturer data (scan response)
     *  2. Service data only (main)
     *  3. Manufacturer data only (main)
     *
     * Returns null if even the minimal payload exceeds the limit.
     */
    private fun buildAdvertisePayloads(
        encoded: ByteArray
    ): Pair<AdvertiseData, AdvertiseData?>? {
        val uuid = Constants.SOS_PARCEL_UUID
        val sdOverhead = serviceDataOverhead(uuid)
        val mfgOverhead = AD_MANUFACTURER_DATA_OVERHEAD

        val mainSdSize = AD_FLAGS_SIZE + sdOverhead + encoded.size
        val mainMfgSize = AD_FLAGS_SIZE + mfgOverhead + encoded.size
        val scanRespMfgSize = AD_FLAGS_SIZE + mfgOverhead + encoded.size

        // Tier 1: Service data (main) + manufacturer data (scan response)
        if (mainSdSize <= MAX_LEGACY_ADV_PAYLOAD &&
            scanRespMfgSize <= MAX_LEGACY_ADV_PAYLOAD
        ) {
            val data = AdvertiseData.Builder()
                .addServiceData(uuid, encoded)
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .build()
            val scanResponse = AdvertiseData.Builder()
                .addManufacturerData(Constants.MANUFACTURER_ID, encoded)
                .setIncludeDeviceName(false)
                .build()
            return data to scanResponse
        }

        // Tier 2: Service data only (main)
        if (mainSdSize <= MAX_LEGACY_ADV_PAYLOAD) {
            val data = AdvertiseData.Builder()
                .addServiceData(uuid, encoded)
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .build()
            return data to null
        }

        // Tier 3: Manufacturer data only (main)
        if (mainMfgSize <= MAX_LEGACY_ADV_PAYLOAD) {
            val data = AdvertiseData.Builder()
                .addManufacturerData(Constants.MANUFACTURER_ID, encoded)
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .build()
            return data to null
        }

        return null
    }

    /**
     * AD structure overhead for a Service Data field, in bytes.
     * Layout: length(1) + type(1) + UUID(2 or 16) = 4 or 18.
     */
    private fun serviceDataOverhead(uuid: ParcelUuid): Int =
        if (isShortUuid(uuid.uuid)) AD_SERVICE_DATA_16BIT_OVERHEAD
        else AD_SERVICE_DATA_128BIT_OVERHEAD

    /**
     * A "short" (16-bit) Bluetooth UUID is always of the form
     * 0000XXXX-0000-1000-8000-00805F9B34FB.
     */
    private fun isShortUuid(uuid: UUID): Boolean {
        val s = uuid.toString().lowercase()
        return s.length == 36 &&
                s.startsWith("0000") &&
                s.endsWith("0000-1000-8000-00805f9b34fb")
    }

    // ---------------------------------------------------------------------
    // Fallback
    // ---------------------------------------------------------------------
    @SuppressLint("MissingPermission")
    private fun attemptMinimalFallback(packet: SosPacket) {
        val adv = advertiser
        if (adv == null) {
            BleDiagnosticsTracker.recordEvent(
                "NARI_BLE_ADVERTISE_FAILURE",
                "Fallback skipped: advertiser unavailable",
                isError = true
            )
            synchronized(stateLock) {
                packetBeingStarted = null
                currentPacket = null
            }
            if (packet.isSos) drainPendingAcks()
            return
        }

        val encodedPacket = PacketCodec.encode(packet)
        val uuid = Constants.SOS_PARCEL_UUID
        val sdOverhead = serviceDataOverhead(uuid)
        val required = AD_FLAGS_SIZE + sdOverhead + encodedPacket.size

        if (required > MAX_LEGACY_ADV_PAYLOAD) {
            BleDiagnosticsTracker.recordEvent(
                "NARI_BLE_ADVERTISE_FAILURE",
                "Fallback skipped: packet too large even for service-data-only " +
                        "(need ${required}B, limit ${MAX_LEGACY_ADV_PAYLOAD}B)",
                isError = true
            )
            Log.e(TAG, "Cannot advertise ${encodedPacket.size}B packet — exceeds legacy limit")
            synchronized(stateLock) {
                packetBeingStarted = null
                currentPacket = null
            }
            if (packet.isSos) drainPendingAcks()
            return
        }

        try {
            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(false)
                .build()

            val data = AdvertiseData.Builder()
                .addServiceData(uuid, encodedPacket)
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .build()

            adv.startAdvertising(settings, data, callback)
        } catch (e: Exception) {
            BleDiagnosticsTracker.recordEvent(
                "NARI_BLE_ADVERTISE_FAILURE",
                "Minimal fallback failed: ${e.message}",
                isError = true
            )
            synchronized(stateLock) {
                packetBeingStarted = null
                currentPacket = null
            }
            if (packet.isSos) drainPendingAcks()
        }
    }

    // ---------------------------------------------------------------------
    // Stop
    // ---------------------------------------------------------------------
    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        stopAdvertisingInternal(flushPendingAcks = true)
    }

    @SuppressLint("MissingPermission")
    private fun stopAdvertisingInternal(flushPendingAcks: Boolean) {
        timeoutJob?.cancel()
        timeoutJob = null

        var wasSos = false
        synchronized(stateLock) {
            wasSos = (currentPacket ?: packetBeingStarted)?.isSos == true
            currentPacket = null
            packetBeingStarted = null
        }

        if (isAdvertising && advertiser != null) {
            try {
                advertiser?.stopAdvertising(callback)
                BleDiagnosticsTracker.recordEvent(
                    "NARI_BLE_ADVERTISE_START", "BLE SOS Advertising stopped"
                )
                Log.i(TAG, "BLE SOS Advertising stopped")
            } catch (e: Exception) {
                Log.e(TAG, "Exception stopping BLE advertisement", e)
            }
        }
        isAdvertising = false
        BleDiagnosticsTracker.setAdvertising(false)

        if (flushPendingAcks && wasSos) {
            drainPendingAcks()
        }
    }

    // ---------------------------------------------------------------------
    // Queue drain
    // ---------------------------------------------------------------------
    private fun drainPendingAcks() {
        val next: SosPacket? = synchronized(stateLock) {
            pendingAcks.removeFirstOrNull()
        } ?: return

        Log.i(TAG, "Draining deferred ACK for #${next.targetSenderId} (remaining=${pendingAcks.size})")
        BleDiagnosticsTracker.recordEvent(
            "NARI_BLE_ACK_DRAINED",
            "Flushing deferred ACK for #${next.targetSenderId}"
        )
        // Recursion safe: startAdvertising → stopAdvertisingInternal(false) →
        // guard passes (slot empty) → advertiser actually starts.
        startAdvertising(next)
    }

    companion object {
        private const val TAG = "BleAdvertiser"

        /** Legacy BLE advertising PDU hard limit. */
        private const val MAX_LEGACY_ADV_PAYLOAD = 31

        /** Flags AD structure: length(1) + type(1) + flags(1). */
        private const val AD_FLAGS_SIZE = 3

        /** Service Data with 16-bit UUID: length(1) + type(1) + uuid(2). */
        private const val AD_SERVICE_DATA_16BIT_OVERHEAD = 4

        /** Service Data with 128-bit UUID: length(1) + type(1) + uuid(16). */
        private const val AD_SERVICE_DATA_128BIT_OVERHEAD = 18

        /** Manufacturer Data: length(1) + type(1) + mfgId(2). */
        private const val AD_MANUFACTURER_DATA_OVERHEAD = 4
    }
}