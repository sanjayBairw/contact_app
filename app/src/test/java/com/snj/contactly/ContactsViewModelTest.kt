package com.snj.contactly

import com.snj.contactly.data.local.ContactEntity
import com.snj.contactly.data.repository.ContactRepository
import com.snj.contactly.ui.contacts.ContactFilterMode
import com.snj.contactly.ui.contacts.ContactsViewModel
import com.snj.contactly.ui.contacts.EmptyStateType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ContactsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: ContactRepository
    private lateinit var viewModel: ContactsViewModel

    @Before
    fun setup() {
        repository = ContactRepository(FakeContactDao())
        viewModel = ContactsViewModel(repository)
    }

    @Test
    fun `empty database emits NO_CONTACTS state`() = runTest {
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        assertEquals(EmptyStateType.NO_CONTACTS, viewModel.uiState.value.emptyState)
        assertEquals(0, viewModel.uiState.value.totalContactsCount)
        job.cancel()
    }

    @Test
    fun `contacts are sorted alphabetically and support search and favorites filtering`() = runTest {
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        repository.insertContact(
            ContactEntity(name = "Zara Khan", phone = "1112223333", isFavorite = false)
        )
        repository.insertContact(
            ContactEntity(name = "Alice Cooper", phone = "4445556666", email = "alice@work.io", isFavorite = true)
        )
        repository.insertContact(
            ContactEntity(name = "Brian Adams", phone = "7778889999", isFavorite = false)
        )

        val initialState = viewModel.uiState.value
        assertEquals(3, initialState.totalContactsCount)
        assertEquals(1, initialState.totalFavoritesCount)
        assertEquals(
            listOf("Alice Cooper", "Brian Adams", "Zara Khan"),
            initialState.regularOrAllContacts.map { it.name }
        )
        assertTrue(initialState.showFavoritesSection)

        viewModel.updateSearchQuery("alice@work.io")
        assertEquals(1, viewModel.uiState.value.regularOrAllContacts.size)
        assertEquals("Alice Cooper", viewModel.uiState.value.regularOrAllContacts.first().name)

        viewModel.updateSearchQuery("nonexistent")
        assertEquals(EmptyStateType.NO_SEARCH_RESULTS, viewModel.uiState.value.emptyState)

        viewModel.updateSearchQuery("")
        viewModel.setFilterMode(ContactFilterMode.FAVORITES)
        assertEquals(1, viewModel.uiState.value.regularOrAllContacts.size)
        assertFalse(viewModel.uiState.value.showFavoritesSection)

        job.cancel()
    }

    @Test
    fun `loadSampleContacts and removeSampleContacts update state`() = runTest {
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.loadSampleContacts()
        assertEquals(8, viewModel.uiState.value.totalContactsCount)
        assertTrue(viewModel.uiState.value.hasSampleContacts)

        viewModel.removeSampleContacts()
        assertEquals(0, viewModel.uiState.value.totalContactsCount)
        assertFalse(viewModel.uiState.value.hasSampleContacts)

        job.cancel()
    }
}

