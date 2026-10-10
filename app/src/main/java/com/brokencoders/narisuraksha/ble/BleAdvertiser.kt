
package com.brokencoders.narisuraksha.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.AdvertisingSet
import android.bluetooth.le.AdvertisingSetCallback
import android.bluetooth.le.AdvertisingSetParameters
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

class BleAdvertiser(
    private val context: Context,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager

    private val bluetoothAdapter: BluetoothAdapter?
        get() = bluetoothManager?.adapter

    private var advertiser: BluetoothLeAdvertiser? = null
    private var extendedSet: AdvertisingSet? = null

    var isAdvertising = false
        private set

    private var timeoutJob: Job? = null
    private var lastPacket: SosPacket? = null

    private val stateLock = Any()
    private var currentPacket: SosPacket? = null
    private var packetBeingStarted: SosPacket? = null
    private val pendingAcks = ArrayDeque<SosPacket>()

    private var fallbackTier = 0
    private var activeExtendedAdvertising = false

    val supportsParallelAdvertisingSets: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            bluetoothAdapter?.isLeExtendedAdvertisingSupported == true &&
            bluetoothAdapter?.isMultipleAdvertisementSupported == true

    private companion object {
        const val TAG = "BleAdvertiser"

        const val LEGACY_ADVERTISING_LIMIT = 31
        const val FLAGS_AD_STRUCTURE_SIZE = 3

        const val AD_TYPE_SERVICE_DATA_16_BIT = 0x16
        const val AD_TYPE_MANUFACTURER_DATA = 0xFF

        const val TIER_FULL = 1
        const val TIER_SERVICE_ONLY = 2
        const val TIER_MANUFACTURER_ONLY = 3
        const val TIER_MINIMAL = 4
        const val TIER_EXTENDED = 5
    }

    /**
     * Includes the AD length byte, AD type byte, and field identifier.
     */
    private fun calculateAdStructureSize(
        uuid: ParcelUuid?,
        payload: ByteArray,
        type: Int
    ): Int {
        val identifierSize = when (type) {
            AD_TYPE_SERVICE_DATA_16_BIT -> 2
            AD_TYPE_MANUFACTURER_DATA -> 2
            else -> 0
        }

        return 1 + 1 + identifierSize + payload.size
    }

    private fun recordSize(
        tier: Int,
        packetSize: Int,
        mainSize: Int,
        scanResponseSize: Int = 0
    ) {
        Log.d(
            TAG,
            "tier=$tier packet=$packetSize bytes, " +
                "advertising=$mainSize/$LEGACY_ADVERTISING_LIMIT bytes, " +
                "scanResponse=$scanResponseSize/$LEGACY_ADVERTISING_LIMIT bytes"
        )
    }

    private fun recordFailure(message: String) {
        BleDiagnosticsTracker.recordEvent(
            "NARI_BLE_ADVERTISE_FAILURE",
            message,
            isError = true
        )
        Log.e(TAG, message)
    }

    private fun makeServiceData(payload: ByteArray): AdvertiseData =
        AdvertiseData.Builder()
            .addServiceData(Constants.SOS_PARCEL_UUID, payload)
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .build()

    private fun makeManufacturerData(payload: ByteArray): AdvertiseData =
        AdvertiseData.Builder()
            .addManufacturerData(Constants.MANUFACTURER_ID, payload)
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .build()

    private fun makeSettings(): AdvertiseSettings =
        AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .setTimeout(0)
            .build()

    private fun makeExtendedData(payload: ByteArray): AdvertiseData =
        AdvertiseData.Builder()
            .addServiceData(Constants.SOS_PARCEL_UUID, payload)
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .build()

    // ---------------------------------------------------------------------
    // Legacy advertising callback
    // ---------------------------------------------------------------------

    private val callback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)

            val startedPacket: SosPacket?

            synchronized(stateLock) {
                startedPacket = packetBeingStarted
                currentPacket = packetBeingStarted
                packetBeingStarted = null
            }

            isAdvertising = true
            BleDiagnosticsTracker.setAdvertising(true)
            BleDiagnosticsTracker.incrementPacketsSent()

            val description = when {
                startedPacket?.isAck == true ->
                    "ACK to #${startedPacket.targetSenderId}"

                startedPacket?.isTest == true ->
                    "TEST SOS #${startedPacket.senderId}"

                else ->
                    "DISTRESS SOS #${startedPacket?.senderId}"
            }

            BleDiagnosticsTracker.recordEvent(
                "NARI_BLE_ADVERTISE_SUCCESS",
                "BLE broadcast active: $description, tier=$fallbackTier"
            )

            Log.i(TAG, "Advertising started: $description, tier=$fallbackTier")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)

            val failedPacket: SosPacket?

            synchronized(stateLock) {
                failedPacket = packetBeingStarted ?: currentPacket
                currentPacket = null
                packetBeingStarted = null
            }

            isAdvertising = false
            BleDiagnosticsTracker.setAdvertising(false)

            recordFailure(
                "Legacy advertising failed: error=$errorCode, tier=$fallbackTier"
            )

            if (failedPacket != null) {
                startNextTier(failedPacket)
            }
        }
    }

    // ---------------------------------------------------------------------
    // Extended advertising callback (API 26+)
    // ---------------------------------------------------------------------

    private val extendedCallback =
        object : AdvertisingSetCallback() {

            override fun onAdvertisingSetStarted(
                advertisingSet: AdvertisingSet?,
                txPower: Int,
                status: Int
            ) {
                super.onAdvertisingSetStarted(advertisingSet, txPower, status)

                if (status == ADVERTISE_SUCCESS && advertisingSet != null) {
                    extendedSet = advertisingSet

                    synchronized(stateLock) {
                        currentPacket = packetBeingStarted
                        packetBeingStarted = null
                    }

                    isAdvertising = true
                    activeExtendedAdvertising = true

                    BleDiagnosticsTracker.setAdvertising(true)
                    BleDiagnosticsTracker.incrementPacketsSent()
                    BleDiagnosticsTracker.recordEvent(
                        "NARI_BLE_EXTENDED_ADV_START",
                        "Extended advertising started, txPower=$txPower"
                    )

                    Log.i(TAG, "Extended advertising started")
                } else {
                    val failedPacket: SosPacket?

                    synchronized(stateLock) {
                        failedPacket = packetBeingStarted ?: currentPacket
                        packetBeingStarted = null
                        currentPacket = null
                    }

                    extendedSet = null
                    activeExtendedAdvertising = false
                    isAdvertising = false
                    BleDiagnosticsTracker.setAdvertising(false)

                    recordFailure(
                        "Extended advertising failed: status=$status"
                    )

                    if (failedPacket != null) {
                        finishFailure(failedPacket)
                    }
                }
            }

            override fun onAdvertisingSetStopped(advertisingSet: AdvertisingSet?) {
                super.onAdvertisingSetStopped(advertisingSet)

                extendedSet = null
                activeExtendedAdvertising = false
                isAdvertising = false
                BleDiagnosticsTracker.setAdvertising(false)

                Log.i(TAG, "Extended advertising stopped")
            }
        }

    // ---------------------------------------------------------------------
    // Start advertising
    // ---------------------------------------------------------------------

    @SuppressLint("MissingPermission")
    fun startAdvertising(
        packet: SosPacket,
        timeoutMs: Long = Constants.SOS_ADVERTISE_TIMEOUT_MS
    ) {
        if (!PermissionHelper.hasBluetoothAdvertisePermission(context)) {
            recordFailure("Missing BLUETOOTH_ADVERTISE permission")
            return
        }

        val adapter = bluetoothAdapter

        if (adapter == null || !adapter.isEnabled) {
            recordFailure("Bluetooth is disabled or unavailable")
            return
        }

        synchronized(stateLock) {
            val occupying = currentPacket ?: packetBeingStarted

            if (occupying?.isSos == true && packet.isAck) {
                pendingAcks.addLast(packet)

                BleDiagnosticsTracker.recordEvent(
                    "NARI_BLE_ACK_DEFERRED",
                    "ACK deferred behind active SOS #${occupying.senderId}"
                )
                return
            }
        }

        stopAdvertisingInternal(flushPendingAcks = false)

        advertiser = adapter.bluetoothLeAdvertiser

        if (advertiser == null) {
            recordFailure("BLE advertising is not supported")
            finishFailure(packet)
            return
        }

        val encodedPacket = PacketCodec.encode(packet)

        if (encodedPacket == null) {
            recordFailure("Packet encoding failed: packet exceeds allowed size")
            finishFailure(packet)
            return
        }

        val serviceStructureSize = calculateAdStructureSize(
            Constants.SOS_PARCEL_UUID,
            encodedPacket,
            AD_TYPE_SERVICE_DATA_16_BIT
        )

        val manufacturerStructureSize = calculateAdStructureSize(
            null,
            encodedPacket,
            AD_TYPE_MANUFACTURER_DATA
        )

        val mainSize = FLAGS_AD_STRUCTURE_SIZE + serviceStructureSize

        recordSize(
            tier = TIER_FULL,
            packetSize = encodedPacket.size,
            mainSize = mainSize,
            scanResponseSize = manufacturerStructureSize
        )

        if (
            mainSize > LEGACY_ADVERTISING_LIMIT ||
            manufacturerStructureSize > LEGACY_ADVERTISING_LIMIT
        ) {
            BleDiagnosticsTracker.recordEvent(
                "NARI_BLE_SIZE_OVERFLOW",
                "Legacy advertising capacity exceeded; packet=${encodedPacket.size}, " +
                    "main=$mainSize, scanResponse=$manufacturerStructureSize"
            )
        }

        lastPacket = packet
        synchronized(stateLock) {
            packetBeingStarted = packet
        }

        fallbackTier = TIER_FULL

        val data = makeServiceData(encodedPacket)
        val scanResponse = makeManufacturerData(encodedPacket)

        try {
            if (
                mainSize <= LEGACY_ADVERTISING_LIMIT &&
                manufacturerStructureSize <= LEGACY_ADVERTISING_LIMIT
            ) {
                advertiser?.startAdvertising(
                    makeSettings(),
                    data,
                    scanResponse,
                    callback
                )
            } else {
                startNextTier(packet)
            }

            timeoutJob?.cancel()
            timeoutJob = externalScope.launch {
                delay(timeoutMs)
                BleDiagnosticsTracker.recordEvent(
                    "NARI_BLE_ADVERTISE_START",
                    "Advertising timeout reached; stopping"
                )
                stopAdvertising()
            }
        } catch (e: Exception) {
            recordFailure("Exception starting advertising: ${e.message}")
            startNextTier(packet)
        }
    }

    // ---------------------------------------------------------------------
    // Multi-tier fallback
    // ---------------------------------------------------------------------

    @SuppressLint("MissingPermission")
    private fun startNextTier(packet: SosPacket) {
        val adv = advertiser

        if (adv == null) {
            finishFailure(packet)
            return
        }

        val encoded = PacketCodec.encode(packet)

        if (encoded == null) {
            finishFailure(packet)
            return
        }

        // Each retry advances to the next tier.
        fallbackTier++

        try {
            when (fallbackTier) {
                TIER_SERVICE_ONLY -> {
                    val structureSize = calculateAdStructureSize(
                        Constants.SOS_PARCEL_UUID,
                        encoded,
                        AD_TYPE_SERVICE_DATA_16_BIT
                    )
                    val mainSize = FLAGS_AD_STRUCTURE_SIZE + structureSize

                    recordSize(TIER_SERVICE_ONLY, encoded.size, mainSize)

                    BleDiagnosticsTracker.recordEvent(
                        "NARI_BLE_FALLBACK_TIER_1",
                        "Retrying with Service Data only"
                    )

                    if (mainSize > LEGACY_ADVERTISING_LIMIT) {
                        startNextTier(packet)
                        return
                    }

                    synchronized(stateLock) {
                        packetBeingStarted = packet
                    }

                    adv.startAdvertising(
                        makeSettings(),
                        makeServiceData(encoded),
                        callback
                    )
                }

                TIER_MANUFACTURER_ONLY -> {
                    val structureSize = calculateAdStructureSize(
                        null,
                        encoded,
                        AD_TYPE_MANUFACTURER_DATA
                    )
                    val mainSize = FLAGS_AD_STRUCTURE_SIZE + structureSize

                    recordSize(TIER_MANUFACTURER_ONLY, encoded.size, mainSize)

                    BleDiagnosticsTracker.recordEvent(
                        "NARI_BLE_FALLBACK_TIER_2",
                        "Retrying with Manufacturer Data only"
                    )

                    if (mainSize > LEGACY_ADVERTISING_LIMIT) {
                        startNextTier(packet)
                        return
                    }

                    synchronized(stateLock) {
                        packetBeingStarted = packet
                    }

                    adv.startAdvertising(
                        makeSettings(),
                        makeManufacturerData(encoded),
                        callback
                    )
                }

                TIER_MINIMAL -> {
                    // Use the codec's legacy 15-byte representation.
                    val minimal = PacketCodec.encode(
                        packet,
                        PacketCodec.LEGACY_PACKET_SIZE_BYTES
                    )

                    if (minimal == null) {
                        startNextTier(packet)
                        return
                    }

                    val structureSize = calculateAdStructureSize(
                        Constants.SOS_PARCEL_UUID,
                        minimal,
                        AD_TYPE_SERVICE_DATA_16_BIT
                    )
                    val mainSize = FLAGS_AD_STRUCTURE_SIZE + structureSize

                    recordSize(TIER_MINIMAL, minimal.size, mainSize)

                    BleDiagnosticsTracker.recordEvent(
                        "NARI_BLE_FALLBACK_TIER_3",
                        "Retrying with legacy 15-byte packet"
                    )

                    if (mainSize > LEGACY_ADVERTISING_LIMIT) {
                        startNextTier(packet)
                        return
                    }

                    synchronized(stateLock) {
                        packetBeingStarted = packet
                    }

                    adv.startAdvertising(
                        makeSettings(),
                        makeServiceData(minimal),
                        callback
                    )
                }

                TIER_EXTENDED -> {
                    if (
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                        bluetoothAdapter?.isLeExtendedAdvertisingSupported == true
                    ) {
                        startExtendedAdvertising(packet, encoded)
                    } else {
                        recordFailure(
                            "Extended advertising unsupported; all legacy tiers failed"
                        )
                        finishFailure(packet)
                    }
                }

                else -> finishFailure(packet)
            }
        } catch (e: Exception) {
            recordFailure(
                "Fallback tier $fallbackTier failed: ${e.message}"
            )
            startNextTier(packet)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startExtendedAdvertising(
        packet: SosPacket,
        encoded: ByteArray
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            finishFailure(packet)
            return
        }

        val adapter = bluetoothAdapter
        val adv = advertiser

        if (
            adapter?.isLeExtendedAdvertisingSupported != true ||
            adv == null
        ) {
            finishFailure(packet)
            return
        }

        val maximumLength = adapter.leMaximumAdvertisingDataLength

        if (encoded.size > maximumLength) {
            recordFailure(
                "Extended payload too large: ${encoded.size}/$maximumLength bytes"
            )
            finishFailure(packet)
            return
        }

        val parameters = AdvertisingSetParameters.Builder()
            .setLegacyMode(false)
            .setConnectable(false)
            .setScannable(false)
            .setInterval(AdvertisingSetParameters.INTERVAL_LOW)
            .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_HIGH)
            .build()

        synchronized(stateLock) {
            packetBeingStarted = packet
        }

        BleDiagnosticsTracker.recordEvent(
            "NARI_BLE_EXTENDED_ADV_START",
            "Starting extended advertising: packet=${encoded.size}, capacity=$maximumLength"
        )

        adv.startAdvertisingSet(
            parameters,
            makeExtendedData(encoded),
            null,
            null,
            null,
            extendedCallback
        )
    }

    private fun finishFailure(packet: SosPacket) {
        synchronized(stateLock) {
            if (packetBeingStarted == packet) packetBeingStarted = null
            if (currentPacket == packet) currentPacket = null
        }

        isAdvertising = false
        BleDiagnosticsTracker.setAdvertising(false)

        recordFailure(
            "All advertising tiers failed for sender #${packet.senderId}"
        )

        if (packet.isSos) {
            drainPendingAcks()
        }
    }

    // ---------------------------------------------------------------------
    // Stop advertising
    // ---------------------------------------------------------------------

    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        stopAdvertisingInternal(flushPendingAcks = true)
    }

    @SuppressLint("MissingPermission")
    private fun stopAdvertisingInternal(flushPendingAcks: Boolean) {
        timeoutJob?.cancel()
        timeoutJob = null

        val wasSos: Boolean

        synchronized(stateLock) {
            wasSos = (currentPacket ?: packetBeingStarted)?.isSos == true
            currentPacket = null
            packetBeingStarted = null
        }

        try {
            if (activeExtendedAdvertising && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                advertiser?.stopAdvertisingSet(extendedCallback)
                extendedSet = null
                activeExtendedAdvertising = false
            } else if (isAdvertising || fallbackTier > 0) {
                advertiser?.stopAdvertising(callback)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception stopping BLE advertising", e)
        }

        isAdvertising = false
        BleDiagnosticsTracker.setAdvertising(false)

        if (flushPendingAcks && wasSos) {
            drainPendingAcks()
        }
    }

    // ---------------------------------------------------------------------
    // Deferred ACK handling
    // ---------------------------------------------------------------------

    private fun drainPendingAcks() {
        val next = synchronized(stateLock) {
            pendingAcks.removeFirstOrNull()
        } ?: return

        Log.i(
            TAG,
            "Draining deferred ACK for #${next.targetSenderId}"
        )

        BleDiagnosticsTracker.recordEvent(
            "NARI_BLE_ACK_DRAINED",
            "Flushing deferred ACK for #${next.targetSenderId}"
        )

        startAdvertising(next)
    }
}