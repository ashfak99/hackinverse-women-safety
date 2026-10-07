package com.brokencoders.narisuraksha.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sos_events")
data class SosEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventType: String, // "SENT" or "RECEIVED"
    val senderId: Short,
    val timestamp: Long,
    val lat: Double?,
    val lon: Double?,
    val audioPath: String? = null,
    val rssi: Int? = null,
    val isLocationUnavailable: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_SENT = "SENT"
        const val TYPE_RECEIVED = "RECEIVED"
    }
}
