package com.brokencoders.narisuraksha.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import com.brokencoders.narisuraksha.core.Constants
import com.brokencoders.narisuraksha.core.DeviceIdProvider
import com.brokencoders.narisuraksha.core.PermissionHelper
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Scans for nearby BLE SOS broadcasts.
 * Supports 16-bit Service UUID, 128-bit UUID, and Manufacturer Data fallback.
 * Decodes packets, rate-limits alerts per sender ID, applies global alert rate limiting, and emits ReceivedSos events with RSSI.
 */
class BleScanner(
    private val context: Context,
    private val deviceIdProvider: DeviceIdProvider
) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? get() = bluetoothManager?.adapter
    private var scanner: BluetoothLeScanner? = null

    private val _received = MutableSharedFlow<ReceivedSos>(
        replay = 1,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val received: Flow<ReceivedSos> = _received.asSharedFlow()

    private var isScanning = false
    // Map to rate-limit and dedupe packets: key = senderId (Short), value = last received system time
    private val senderLastSeenMap = ConcurrentHashMap<Short, Long>()
    private val packetDedupeMap = ConcurrentHashMap<String, Long>()
    // Sliding window of alert timestamps across all senders (global rate limit against spoofed ID rotations)
    private val globalAlertTimestamps = ConcurrentLinkedQueue<Long>()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            processScanResult(result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            results?.forEach { processScanResult(it) }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            isScanning = false
            Log.e(TAG, "BLE Scan failed with error code: $errorCode")
        }
    }

    private fun processScanResult(result: ScanResult?) {
        if (result == null) return

        val scanRecord = result.scanRecord ?: return
        
        // 1. Try 16-bit Service Data
        var payloadBytes = scanRecord.getServiceData(Constants.SOS_PARCEL_UUID)

        // 2. Try 128-bit Legacy Service Data
        if (payloadBytes == null) {
            payloadBytes = scanRecord.getServiceData(ParcelUuid(Constants.SOS_LEGACY_128_UUID))
        }

        // 3. Try Manufacturer Data fallback
        if (payloadBytes == null) {
            payloadBytes = scanRecord.getManufacturerSpecificData(Constants.MANUFACTURER_ID)
        }

        // 4. Scan all available service data entries if UUID variation occurred
        if (payloadBytes == null && scanRecord.serviceData != null) {
            for ((_, data) in scanRecord.serviceData) {
                if (data.size >= PacketCodec.LEGACY_PACKET_SIZE_BYTES) {
                    payloadBytes = data
                    break
                }
            }
        }

        if (payloadBytes == null) return

        val packet = PacketCodec.decode(payloadBytes) ?: return

        // Ignore our own broadcasted packets
        if (packet.senderId == deviceIdProvider.deviceId) {
            return
        }

        val now = System.currentTimeMillis()
        val dedupeKey = "${packet.senderId}:${packet.timestamp}"
        val lastSeenForPacket = packetDedupeMap[dedupeKey]
        val lastSeenForSender = senderLastSeenMap[packet.senderId]

        // Clean up old seen entries (> 3 minutes)
        if (packetDedupeMap.size > 200) {
            packetDedupeMap.entries.removeIf { now - it.value > 180_000L }
            senderLastSeenMap.entries.removeIf { now - it.value > 180_000L }
        }

        // Global rate limit: cap alerts across all sender IDs to mitigate spoofed ID-rotation flood attacks
        if (!packet.isAck) {
            val windowStart = now - Constants.GLOBAL_ALERT_RATE_LIMIT_WINDOW_MS
            while (globalAlertTimestamps.peek()?.let { it < windowStart } == true) {
                globalAlertTimestamps.poll()
            }
            if (globalAlertTimestamps.size >= Constants.GLOBAL_ALERT_RATE_LIMIT_MAX_PER_MINUTE) {
                Log.w(TAG, "Global alert rate limit reached (${Constants.GLOBAL_ALERT_RATE_LIMIT_MAX_PER_MINUTE}/min). Suppressing alert from #${packet.senderId}")
                return
            }
        }

        // Rate-limit incoming alerts per sender ID (10s cooldown) unless it is an ACK
        if (!packet.isAck && lastSeenForSender != null && (now - lastSeenForSender < Constants.PACKET_RATE_LIMIT_MS)) {
            return
        }

        // Dedupe exact identical packet
        if (lastSeenForPacket != null && (now - lastSeenForPacket < 5_000L)) {
            return
        }

        if (!packet.isAck) {
            globalAlertTimestamps.add(now)
        }

        packetDedupeMap[dedupeKey] = now
        senderLastSeenMap[packet.senderId] = now

        Log.i(TAG, "Decoded valid SOS packet from sender ${packet.senderId}, RSSI: ${result.rssi} dBm, isAck: ${packet.isAck}")

        val receivedSos = ReceivedSos(
            packet = packet,
            rssi = result.rssi,
            receivedAt = now
        )
        _received.tryEmit(receivedSos)
    }

    @SuppressLint("MissingPermission")
    fun startScanning() {
        if (isScanning) {
            Log.d(TAG, "BLE scan already active")
            return
        }

        if (!PermissionHelper.hasBluetoothPermissions(context)) {
            Log.w(TAG, "Missing Bluetooth permissions to scan")
            return
        }

        val adapter = bluetoothAdapter
        if (adapter == null || !adapter.isEnabled) {
            Log.w(TAG, "Bluetooth disabled or unavailable for scanning")
            return
        }

        scanner = adapter.bluetoothLeScanner
        if (scanner == null) {
            Log.e(TAG, "BLE Scanner not available")
            return
        }

        // Build filters for 16-bit UUID, 128-bit UUID, and Manufacturer Data
        val filters = listOf(
            ScanFilter.Builder().setServiceUuid(Constants.SOS_PARCEL_UUID).build(),
            ScanFilter.Builder().setServiceUuid(ParcelUuid(Constants.SOS_LEGACY_128_UUID)).build(),
            ScanFilter.Builder().setManufacturerData(Constants.MANUFACTURER_ID, byteArrayOf()).build()
        )

        val settingsBuilder = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            settingsBuilder.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            settingsBuilder.setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
        }

        try {
            scanner?.startScan(filters, settingsBuilder.build(), scanCallback)
            isScanning = true
            Log.i(TAG, "BLE Scanner started with multi-payload filters")
        } catch (e: Exception) {
            Log.e(TAG, "Exception starting BLE scan with filters, trying fallback scan without filters", e)
            try {
                scanner?.startScan(scanCallback)
                isScanning = true
            } catch (e2: Exception) {
                Log.e(TAG, "Fallback BLE scan failed", e2)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        if (!isScanning) return
        try {
            scanner?.stopScan(scanCallback)
            Log.i(TAG, "BLE Scanner stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Exception stopping BLE scan", e)
        } finally {
            isScanning = false
        }
    }

    companion object {
        private const val TAG = "BleScanner"
    }
}
