package com.snj.contactly

import com.snj.contactly.data.local.ContactDao
import com.snj.contactly.data.local.ContactEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeContactDao : ContactDao {

    private val contactsFlow = MutableStateFlow<List<ContactEntity>>(emptyList())
    private var nextId = 1

    override suspend fun insertContact(contact: ContactEntity): Long {
        val id = if (contact.id > 0) contact.id else nextId++
        val stored = contact.copy(id = id)
        val current = contactsFlow.value.filterNot { it.id == id }
        contactsFlow.value = (current + stored).sortedBy { it.name.lowercase() }
        return id.toLong()
    }

    override suspend fun insertContacts(contacts: List<ContactEntity>) {
        val current = contactsFlow.value.toMutableList()
        for (contact in contacts) {
            val id = if (contact.id > 0) contact.id else nextId++
            if (current.none { it.id == id }) {
                current.add(contact.copy(id = id))
            }
        }
        contactsFlow.value = current.sortedBy { it.name.lowercase() }
    }

    override suspend fun updateContact(contact: ContactEntity) {
        val current = contactsFlow.value.map { existing ->
            if (existing.id == contact.id) contact else existing
        }
        contactsFlow.value = current.sortedBy { it.name.lowercase() }
    }

    override suspend fun deleteContact(contact: ContactEntity) {
        contactsFlow.value = contactsFlow.value.filterNot { it.id == contact.id }
    }

    override suspend fun deleteContactById(contactId: Int) {
        contactsFlow.value = contactsFlow.value.filterNot { it.id == contactId }
    }

    override suspend fun deleteSampleContacts() {
        contactsFlow.value = contactsFlow.value.filterNot { it.isSample }
    }

    override fun countSampleContacts(): Flow<Int> {
        return contactsFlow.map { list -> list.count { it.isSample } }
    }

    override suspend fun getSampleContactCountOnce(): Int {
        return contactsFlow.value.count { it.isSample }
    }

    override fun getAllContacts(): Flow<List<ContactEntity>> = contactsFlow

    override fun getContactById(contactId: Int): Flow<ContactEntity?> {
        return contactsFlow.map { list -> list.firstOrNull { it.id == contactId } }
    }

    override suspend fun getContactByIdOnce(contactId: Int): ContactEntity? {
        return contactsFlow.value.firstOrNull { it.id == contactId }
    }

    override fun searchContacts(query: String): Flow<List<ContactEntity>> {
        return contactsFlow.map { list ->
            list.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.phone.contains(query, ignoreCase = true) ||
                    (it.email?.contains(query, ignoreCase = true) == true)
            }
        }
    }

    override fun getFavoriteContacts(): Flow<List<ContactEntity>> {
        return contactsFlow.map { list -> list.filter { it.isFavorite } }
    }

    override suspend fun toggleFavorite(contactId: Int, isFavorite: Boolean, updatedAt: Long) {
        contactsFlow.value = contactsFlow.value.map { contact ->
            if (contact.id == contactId) {
                contact.copy(isFavorite = isFavorite, updatedAt = updatedAt)
            } else {
                contact
            }
        }
    }
}

