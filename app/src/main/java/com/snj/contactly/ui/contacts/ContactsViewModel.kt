package com.snj.contactly.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.snj.contactly.data.local.ContactEntity
import com.snj.contactly.data.repository.ContactRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ContactFilterMode {
    ALL,
    FAVORITES
}

enum class EmptyStateType {
    NONE,
    NO_CONTACTS,
    NO_FAVORITES,
    NO_SEARCH_RESULTS
}

data class ContactsUiState(
    val totalContactsCount: Int = 0,
    val totalFavoritesCount: Int = 0,
    val favoriteContacts: List<ContactEntity> = emptyList(),
    val regularOrAllContacts: List<ContactEntity> = emptyList(),
    val searchQuery: String = "",
    val filterMode: ContactFilterMode = ContactFilterMode.ALL,
    val hasSampleContacts: Boolean = false,
    val emptyState: EmptyStateType = EmptyStateType.NONE
) {
    val showFavoritesSection: Boolean
        get() = filterMode == ContactFilterMode.ALL && favoriteContacts.isNotEmpty()

    val showAllContactsHeader: Boolean
        get() = filterMode == ContactFilterMode.ALL &&
            favoriteContacts.isNotEmpty() &&
            regularOrAllContacts.isNotEmpty()
}

class ContactsViewModel(
    private val repository: ContactRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterMode = MutableStateFlow(ContactFilterMode.ALL)
    val filterMode: StateFlow<ContactFilterMode> = _filterMode.asStateFlow()

    private val _uiMessages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val uiMessages: SharedFlow<String> = _uiMessages.asSharedFlow()

    val uiState: StateFlow<ContactsUiState> = combine(
        repository.allContacts,
        repository.sampleContactCount,
        _searchQuery,
        _filterMode
    ) { allContacts, sampleCount, query, filter ->
        val sortedAll = allContacts.sortedBy { it.name.lowercase() }
        val totalCount = sortedAll.size
        val totalFavorites = sortedAll.count { it.isFavorite }

        val trimmedQuery = query.trim()
        val matchingQuery = if (trimmedQuery.isEmpty()) {
            sortedAll
        } else {
            sortedAll.filter { contact ->
                contact.name.contains(trimmedQuery, ignoreCase = true) ||
                    contact.phone.contains(trimmedQuery, ignoreCase = true) ||
                    (contact.email?.contains(trimmedQuery, ignoreCase = true) == true)
            }
        }

        val favoritesList: List<ContactEntity>
        val mainList: List<ContactEntity>

        when (filter) {
            ContactFilterMode.FAVORITES -> {
                favoritesList = emptyList()
                mainList = matchingQuery.filter { it.isFavorite }
            }
            ContactFilterMode.ALL -> {
                favoritesList = matchingQuery.filter { it.isFavorite }
                mainList = matchingQuery
            }
        }

        val emptyState = when {
            totalCount == 0 -> EmptyStateType.NO_CONTACTS
            trimmedQuery.isNotEmpty() && favoritesList.isEmpty() && mainList.isEmpty() ->
                EmptyStateType.NO_SEARCH_RESULTS
            filter == ContactFilterMode.FAVORITES && mainList.isEmpty() ->
                EmptyStateType.NO_FAVORITES
            else -> EmptyStateType.NONE
        }

        ContactsUiState(
            totalContactsCount = totalCount,
            totalFavoritesCount = totalFavorites,
            favoriteContacts = favoritesList,
            regularOrAllContacts = mainList,
            searchQuery = query,
            filterMode = filter,
            hasSampleContacts = sampleCount > 0,
            emptyState = emptyState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ContactsUiState()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterMode(mode: ContactFilterMode) {
        _filterMode.value = mode
    }

    fun toggleFavorite(contact: ContactEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(contact.id, contact.isFavorite)
            val message = if (contact.isFavorite) {
                "Removed ${contact.name} from favorites"
            } else {
                "Added ${contact.name} to favorites"
            }
            _uiMessages.tryEmit(message)
        }
    }

    fun loadSampleContacts() {
        viewModelScope.launch {
            val inserted = repository.insertSampleContactsIfMissing()
            val message = if (inserted) {
                "8 sample contacts added"
            } else {
                "Sample contacts are already loaded"
            }
            _uiMessages.tryEmit(message)
        }
    }

    fun removeSampleContacts() {
        viewModelScope.launch {
            repository.removeSampleContacts()
            _uiMessages.tryEmit("Sample contacts removed")
        }
    }

    class Factory(private val repository: ContactRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ContactsViewModel::class.java)) {
                return ContactsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

