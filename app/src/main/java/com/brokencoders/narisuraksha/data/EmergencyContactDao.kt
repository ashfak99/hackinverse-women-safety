package com.brokencoders.narisuraksha.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyContactDao {

    @Query("SELECT * FROM emergency_contacts ORDER BY isPrimary DESC, name ASC")
    fun getAllContacts(): Flow<List<EmergencyContactEntity>>

    @Query("SELECT * FROM emergency_contacts WHERE isPrimary = 1 LIMIT 1")
    fun getPrimaryContact(): Flow<EmergencyContactEntity?>

    @Query("SELECT * FROM emergency_contacts WHERE id = :id LIMIT 1")
    suspend fun getContactById(id: Long): EmergencyContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContactEntity): Long

    @Update
    suspend fun updateContact(contact: EmergencyContactEntity)

    @Delete
    suspend fun deleteContact(contact: EmergencyContactEntity)

    @Query("DELETE FROM emergency_contacts WHERE id = :id")
    suspend fun deleteContactById(id: Long)

    @Query("UPDATE emergency_contacts SET isPrimary = 0")
    suspend fun clearPrimaryStatus()

    @Query("UPDATE emergency_contacts SET isPrimary = 1 WHERE id = :contactId")
    suspend fun setPrimaryFlag(contactId: Long)

    @Transaction
    suspend fun setAsPrimary(contactId: Long) {
        clearPrimaryStatus()
        setPrimaryFlag(contactId)
    }
}
