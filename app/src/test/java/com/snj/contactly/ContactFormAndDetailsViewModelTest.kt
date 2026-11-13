package com.snj.contactly

import com.snj.contactly.data.local.ContactEntity
import com.snj.contactly.data.repository.ContactRepository
import com.snj.contactly.ui.details.ContactDetailsViewModel
import com.snj.contactly.ui.form.ContactFormViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ContactFormAndDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: ContactRepository

    @Before
    fun setup() {
        repository = ContactRepository(FakeContactDao())
    }

    @Test
    fun `saveContact rejects invalid input and saves valid new contact`() = runTest {
        val formViewModel = ContactFormViewModel(repository)
        val savedEvents = mutableListOf<ContactFormViewModel.FormEvent>()
        val eventJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            formViewModel.events.collect { savedEvents.add(it) }
        }

        formViewModel.saveContact(
            existingContact = null,
            name = "",
            phone = "12",
            email = "bad",
            company = null,
            jobTitle = null,
            notes = null,
            photoUri = null,
            isFavorite = false
        )

        assertFalse(formViewModel.validationErrors.value.isValid)
        assertEquals(0, repository.allContacts.first().size)
        assertTrue(savedEvents.isEmpty())

        formViewModel.saveContact(
            existingContact = null,
            name = "  Elena Rostova  ",
            phone = " +44 7700 900412 ",
            email = " elena@atelier.co.uk ",
            company = "Atelier Studio",
            jobTitle = "Creative Director",
            notes = "Design review",
            photoUri = null,
            isFavorite = true
        )

        assertTrue(formViewModel.validationErrors.value.isValid)
        val all = repository.allContacts.first()
        assertEquals(1, all.size)
        assertEquals("Elena Rostova", all.first().name)
        assertEquals("+44 7700 900412", all.first().phone)
        assertTrue(all.first().isFavorite)
        assertTrue(savedEvents.contains(ContactFormViewModel.FormEvent.Saved))

        eventJob.cancel()
    }

    @Test
    fun `editContact preserves id createdAt and updates fields`() = runTest {
        val originalId = repository.insertContact(
            ContactEntity(
                name = "Marcus Vance",
                phone = "3125550148",
                isSample = true,
                createdAt = 1_000L,
                updatedAt = 1_000L
            )
        ).toInt()

        val formViewModel = ContactFormViewModel(repository)
        formViewModel.loadContactIfNeeded(originalId)
        val loaded = formViewModel.loadedContact.value
        assertNotNull(loaded)

        formViewModel.saveContact(
            existingContact = loaded,
            name = "Marcus Vance Jr.",
            phone = "3125550199",
            email = "marcus@finpeak.com",
            company = "FinPeak",
            jobTitle = "Architect",
            notes = "Updated notes",
            photoUri = null,
            isFavorite = true
        )

        val updated = repository.getContactByIdOnce(originalId)!!
        assertEquals(originalId, updated.id)
        assertEquals(1_000L, updated.createdAt)
        assertTrue(updated.isSample)
        assertEquals("Marcus Vance Jr.", updated.name)
        assertEquals("3125550199", updated.phone)
        assertTrue(updated.isFavorite)
    }

    @Test
    fun `detailsViewModel toggles favorite and deletes contact`() = runTest {
        val id = repository.insertContact(
            ContactEntity(name = "Nadia Patel", phone = "9820111223", isFavorite = false)
        ).toInt()

        val detailsViewModel = ContactDetailsViewModel(id, repository)
        val collectorJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            detailsViewModel.contact.collect()
        }
        val events = mutableListOf<ContactDetailsViewModel.DetailsEvent>()
        val eventJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            detailsViewModel.events.collect { events.add(it) }
        }

        assertEquals("Nadia Patel", detailsViewModel.contact.value?.name)

        detailsViewModel.toggleFavorite()
        assertTrue(repository.getContactByIdOnce(id)?.isFavorite == true)

        detailsViewModel.deleteContact()
        assertNull(repository.getContactByIdOnce(id))
        assertTrue(events.contains(ContactDetailsViewModel.DetailsEvent.ContactDeleted))

        collectorJob.cancel()
        eventJob.cancel()
    }
}

