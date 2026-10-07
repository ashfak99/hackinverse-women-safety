package com.brokencoders.narisuraksha.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SosDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: SosEventEntity): Long

    @Query("SELECT * FROM sos_events ORDER BY createdAt DESC")
    fun getAllEvents(): Flow<List<SosEventEntity>>

    @Query("SELECT * FROM sos_events WHERE eventType = 'SENT' ORDER BY createdAt DESC")
    fun getSentEvents(): Flow<List<SosEventEntity>>

    @Query("SELECT * FROM sos_events WHERE eventType = 'RECEIVED' ORDER BY createdAt DESC")
    fun getReceivedEvents(): Flow<List<SosEventEntity>>

    @Query("SELECT * FROM sos_events WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: Long): SosEventEntity?

    @Delete
    suspend fun deleteEvent(event: SosEventEntity)

    @Query("DELETE FROM sos_events")
    suspend fun clearAll()
}
