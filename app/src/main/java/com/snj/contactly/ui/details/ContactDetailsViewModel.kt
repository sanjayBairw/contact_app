package com.snj.contactly.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.snj.contactly.data.local.ContactEntity
import com.snj.contactly.data.repository.ContactRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContactDetailsViewModel(
    private val contactId: Int,
    private val repository: ContactRepository
) : ViewModel() {

    val contact: StateFlow<ContactEntity?> = repository.getContactById(contactId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    private val _events = MutableSharedFlow<DetailsEvent>(extraBufferCapacity = 2)
    val events: SharedFlow<DetailsEvent> = _events.asSharedFlow()

    sealed class DetailsEvent {
        data class ShowMessage(val message: String) : DetailsEvent()
        data object ContactDeleted : DetailsEvent()
    }

    fun toggleFavorite() {
        val current = contact.value ?: return
        viewModelScope.launch {
            repository.toggleFavorite(current.id, current.isFavorite)
            val message = if (current.isFavorite) {
                "Removed from favorites"
            } else {
                "Added to favorites"
            }
            _events.tryEmit(DetailsEvent.ShowMessage(message))
        }
    }

    fun deleteContact() {
        val current = contact.value
        viewModelScope.launch {
            if (current != null) {
                repository.deleteContact(current)
            } else {
                repository.deleteContactById(contactId)
            }
            _events.tryEmit(DetailsEvent.ContactDeleted)
        }
    }

    class Factory(
        private val contactId: Int,
        private val repository: ContactRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ContactDetailsViewModel::class.java)) {
                return ContactDetailsViewModel(contactId, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

