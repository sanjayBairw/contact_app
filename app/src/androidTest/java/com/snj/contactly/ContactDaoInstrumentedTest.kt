package com.snj.contactly

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.snj.contactly.data.local.ContactDao
import com.snj.contactly.data.local.ContactDatabase
import com.snj.contactly.data.local.ContactEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContactDaoInstrumentedTest {

    private lateinit var database: ContactDatabase
    private lateinit var contactDao: ContactDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, ContactDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        contactDao = database.contactDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertSearchFavoriteAndDeleteContact() = runBlocking {
        val id = contactDao.insertContact(
            ContactEntity(
                name = "Aarav Sharma",
                phone = "+91 98765 43210",
                email = "aarav@pynexia.dev"
            )
        ).toInt()

        val searchHits = contactDao.searchContacts("pynexia").first()
        assertEquals(1, searchHits.size)
        assertEquals("Aarav Sharma", searchHits.first().name)

        contactDao.toggleFavorite(id, true, System.currentTimeMillis())
        val favorites = contactDao.getFavoriteContacts().first()
        assertEquals(1, favorites.size)
        assertTrue(favorites.first().isFavorite)

        contactDao.deleteContactById(id)
        assertNull(contactDao.getContactByIdOnce(id))
    }
}

