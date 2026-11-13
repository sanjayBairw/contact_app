package com.snj.contactly.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertContacts(contacts: List<ContactEntity>)

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Delete
    suspend fun deleteContact(contact: ContactEntity)

    @Query("DELETE FROM contacts WHERE id = :contactId")
    suspend fun deleteContactById(contactId: Int)

    @Query("DELETE FROM contacts WHERE isSample = 1")
    suspend fun deleteSampleContacts()

    @Query("SELECT COUNT(*) FROM contacts WHERE isSample = 1")
    fun countSampleContacts(): Flow<Int>

    @Query("SELECT COUNT(*) FROM contacts WHERE isSample = 1")
    suspend fun getSampleContactCountOnce(): Int

    @Query("SELECT * FROM contacts ORDER BY name COLLATE NOCASE ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE id = :contactId")
    fun getContactById(contactId: Int): Flow<ContactEntity?>

    @Query("SELECT * FROM contacts WHERE id = :contactId")
    suspend fun getContactByIdOnce(contactId: Int): ContactEntity?

    @Query(
        """
        SELECT * FROM contacts
        WHERE name LIKE '%' || :query || '%'
           OR phone LIKE '%' || :query || '%'
           OR COALESCE(email, '') LIKE '%' || :query || '%'
        ORDER BY name COLLATE NOCASE ASC
        """
    )
    fun searchContacts(query: String): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE isFavorite = 1 ORDER BY name COLLATE NOCASE ASC")
    fun getFavoriteContacts(): Flow<List<ContactEntity>>

    @Query("UPDATE contacts SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :contactId")
    suspend fun toggleFavorite(contactId: Int, isFavorite: Boolean, updatedAt: Long)
}

