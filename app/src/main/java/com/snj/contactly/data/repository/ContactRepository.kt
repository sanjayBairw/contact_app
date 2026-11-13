package com.snj.contactly.data.repository

import com.snj.contactly.data.local.ContactDao
import com.snj.contactly.data.local.ContactEntity
import com.snj.contactly.utils.SampleDataProvider
import kotlinx.coroutines.flow.Flow

class ContactRepository(private val contactDao: ContactDao) {

    val allContacts: Flow<List<ContactEntity>> = contactDao.getAllContacts()

    val favoriteContacts: Flow<List<ContactEntity>> = contactDao.getFavoriteContacts()

    val sampleContactCount: Flow<Int> = contactDao.countSampleContacts()

    fun getContactById(contactId: Int): Flow<ContactEntity?> =
        contactDao.getContactById(contactId)

    suspend fun getContactByIdOnce(contactId: Int): ContactEntity? =
        contactDao.getContactByIdOnce(contactId)

    fun searchContacts(query: String): Flow<List<ContactEntity>> {
        val trimmed = query.trim()
        return if (trimmed.isEmpty()) {
            contactDao.getAllContacts()
        } else {
            contactDao.searchContacts(trimmed)
        }
    }

    suspend fun insertContact(contact: ContactEntity): Long {
        val now = System.currentTimeMillis()
        val normalized = contact.copy(
            createdAt = if (contact.createdAt > 0L) contact.createdAt else now,
            updatedAt = now
        )
        return contactDao.insertContact(normalized)
    }

    suspend fun updateContact(contact: ContactEntity) {
        val updated = contact.copy(updatedAt = System.currentTimeMillis())
        contactDao.updateContact(updated)
    }

    suspend fun deleteContact(contact: ContactEntity) {
        contactDao.deleteContact(contact)
    }

    suspend fun deleteContactById(contactId: Int) {
        contactDao.deleteContactById(contactId)
    }

    suspend fun toggleFavorite(contactId: Int, currentFavorite: Boolean) {
        contactDao.toggleFavorite(
            contactId = contactId,
            isFavorite = !currentFavorite,
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Inserts sample contacts only if no sample contacts are currently stored,
     * preventing duplicate sample entries.
     */
    suspend fun insertSampleContactsIfMissing(): Boolean {
        if (contactDao.getSampleContactCountOnce() > 0) {
            return false
        }
        contactDao.insertContacts(SampleDataProvider.createSampleContacts())
        return true
    }

    suspend fun removeSampleContacts() {
        contactDao.deleteSampleContacts()
    }
}

