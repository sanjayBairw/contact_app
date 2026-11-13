package com.snj.contactly

import com.snj.contactly.data.local.ContactEntity
import com.snj.contactly.data.repository.ContactRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ContactRepositoryTest {

    private lateinit var dao: FakeContactDao
    private lateinit var repository: ContactRepository

    @Before
    fun setup() {
        dao = FakeContactDao()
        repository = ContactRepository(dao)
    }

    @Test
    fun `insert update and delete contact lifecycle works`() = runTest {
        val insertedId = repository.insertContact(
            ContactEntity(
                name = "Maya Lin",
                phone = "+1 415 555 0100",
                email = "maya@studio.dev"
            )
        ).toInt()

        val loaded = repository.getContactByIdOnce(insertedId)
        assertNotNull(loaded)
        assertEquals("Maya Lin", loaded!!.name)

        repository.updateContact(loaded.copy(company = "Studio Dev", isFavorite = true))
        val updated = repository.getContactByIdOnce(insertedId)
        assertEquals("Studio Dev", updated?.company)
        assertTrue(updated?.isFavorite == true)

        repository.deleteContact(updated!!)
        assertNull(repository.getContactByIdOnce(insertedId))
    }

    @Test
    fun `searchContacts matches name phone and email`() = runTest {
        repository.insertContact(
            ContactEntity(name = "Aarav Sharma", phone = "9876543210", email = "aarav@pynexia.dev")
        )
        repository.insertContact(
            ContactEntity(name = "Clara Bennett", phone = "4158902341", email = "clara@northstar.io")
        )

        assertEquals(1, repository.searchContacts("aarav").first().size)
        assertEquals(1, repository.searchContacts("8902341").first().size)
        assertEquals(1, repository.searchContacts("northstar.io").first().size)
        assertEquals(2, repository.searchContacts("   ").first().size)
    }

    @Test
    fun `toggleFavorite flips favorite state`() = runTest {
        val id = repository.insertContact(
            ContactEntity(name = "Liam Carter", phone = "9811122233", isFavorite = false)
        ).toInt()

        repository.toggleFavorite(id, currentFavorite = false)
        assertTrue(repository.getContactByIdOnce(id)?.isFavorite == true)

        repository.toggleFavorite(id, currentFavorite = true)
        assertFalse(repository.getContactByIdOnce(id)?.isFavorite == true)
    }

    @Test
    fun `sample contacts insert once without duplicates and remove cleanly`() = runTest {
        repository.insertContact(
            ContactEntity(name = "User Contact", phone = "9998887776", isSample = false)
        )

        assertTrue(repository.insertSampleContactsIfMissing())
        assertEquals(9, repository.allContacts.first().size)
        assertEquals(8, repository.sampleContactCount.first())

        assertFalse(repository.insertSampleContactsIfMissing())
        assertEquals(9, repository.allContacts.first().size)

        repository.removeSampleContacts()
        val remaining = repository.allContacts.first()
        assertEquals(1, remaining.size)
        assertEquals("User Contact", remaining.first().name)
    }
}

