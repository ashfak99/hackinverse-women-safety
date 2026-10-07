package com.brokencoders.narisuraksha.ble

import com.brokencoders.narisuraksha.core.SosPacket
import kotlinx.coroutines.flow.Flow

data class ReceivedSos(val packet: SosPacket, val rssi: Int, val receivedAt: Long)

interface BleTransport {
    fun startAdvertising(packet: SosPacket)
    fun stopAdvertising()
    fun startScanning()
    fun stopScanning()
    val received: Flow<ReceivedSos>
}
