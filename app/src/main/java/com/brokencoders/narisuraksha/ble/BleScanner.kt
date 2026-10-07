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

/**
 * Scans for nearby BLE SOS broadcasts.
 * Filters by custom Service UUID, decodes packets, deduplicates repeating packets,
 * and emits ReceivedSos events with RSSI distance information.
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
    // Map to dedupe packets: key = "senderId:timestamp", value = last received system time
    private val seenPackets = ConcurrentHashMap<String, Long>()

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
        val parcelUuid = ParcelUuid(Constants.SOS_SERVICE_UUID)
        var serviceData = scanRecord.getServiceData(parcelUuid)

        // Fallback: search all service data entries if UUID matching had variation
        if (serviceData == null && scanRecord.serviceData != null) {
            for ((_, data) in scanRecord.serviceData) {
                if (data.size >= PacketCodec.PACKET_SIZE_BYTES) {
                    serviceData = data
                    break
                }
            }
        }

        if (serviceData == null) return

        val packet = PacketCodec.decode(serviceData) ?: return

        // Ignore our own broadcasted packets
        if (packet.senderId == deviceIdProvider.deviceId) {
            return
        }

        val now = System.currentTimeMillis()
        val dedupeKey = "${packet.senderId}:${packet.timestamp}"
        val lastSeen = seenPackets[dedupeKey]

        // Clean up old seen entries (> 3 minutes)
        seenPackets.entries.removeIf { now - it.value > 180_000L }

        // Dedupe identical alert if received within 10 seconds to avoid spamming alerts,
        // but still update flow
        if (lastSeen != null && (now - lastSeen < 10_000L)) {
            return
        }

        seenPackets[dedupeKey] = now
        Log.i(TAG, "Received SOS packet from sender ${packet.senderId}, RSSI: ${result.rssi} dBm, flags: ${packet.flags}")

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

        val filters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(Constants.SOS_SERVICE_UUID))
                .build()
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
            Log.i(TAG, "BLE Scanner started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Exception starting BLE scan", e)
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
