package com.brokencoders.narisuraksha.ble

import com.brokencoders.narisuraksha.core.SosPacket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Fake BLE Transport for testing and development without Bluetooth hardware.
 */
class FakeBleTransport : BleTransport {

    private val _received = MutableSharedFlow<ReceivedSos>(replay = 1)
    override val received: Flow<ReceivedSos> = _received.asSharedFlow()

    var isAdvertising = false
        private set

    var isScanning = false
        private set

    var lastAdvertisedPacket: SosPacket? = null
        private set

    override fun startAdvertising(packet: SosPacket) {
        isAdvertising = true
        lastAdvertisedPacket = packet
    }

    override fun stopAdvertising() {
        isAdvertising = false
    }

    override fun startScanning() {
        isScanning = true
    }

    override fun stopScanning() {
        isScanning = false
    }

    fun simulateIncomingSos(
        senderId: Short = 9999.toShort(),
        lat: Double = 28.6139,
        lon: Double = 77.2090,
        rssi: Int = -58
    ) {
        val packet = SosPacket.create(
            senderId = senderId,
            lat = lat,
            lon = lon
        )
        val receivedSos = ReceivedSos(
            packet = packet,
            rssi = rssi,
            receivedAt = System.currentTimeMillis()
        )
        _received.tryEmit(receivedSos)
    }
}
