package com.snj.contactly.ui.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.snj.contactly.data.local.ContactEntity
import com.snj.contactly.data.repository.ContactRepository
import com.snj.contactly.utils.ValidationUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContactFormViewModel(
    private val repository: ContactRepository
) : ViewModel() {

    private val _loadedContact = MutableStateFlow<ContactEntity?>(null)
    val loadedContact: StateFlow<ContactEntity?> = _loadedContact.asStateFlow()

    private val _validationErrors = MutableStateFlow(ValidationUtils.ContactFormValidation())
    val validationErrors: StateFlow<ValidationUtils.ContactFormValidation> =
        _validationErrors.asStateFlow()

    private val _events = MutableSharedFlow<FormEvent>(extraBufferCapacity = 2)
    val events: SharedFlow<FormEvent> = _events.asSharedFlow()

    private var hasLoadedExisting = false

    sealed class FormEvent {
        data object Saved : FormEvent()
        data object ContactMissing : FormEvent()
    }

    fun loadContactIfNeeded(contactId: Int) {
        if (hasLoadedExisting || contactId <= 0) return
        hasLoadedExisting = true
        viewModelScope.launch {
            val contact = repository.getContactByIdOnce(contactId)
            if (contact != null) {
                _loadedContact.value = contact
            } else {
                _events.tryEmit(FormEvent.ContactMissing)
            }
        }
    }

    fun clearNameError() {
        if (_validationErrors.value.nameError != null) {
            _validationErrors.value = _validationErrors.value.copy(nameError = null)
        }
    }

    fun clearPhoneError() {
        if (_validationErrors.value.phoneError != null) {
            _validationErrors.value = _validationErrors.value.copy(phoneError = null)
        }
    }

    fun clearEmailError() {
        if (_validationErrors.value.emailError != null) {
            _validationErrors.value = _validationErrors.value.copy(emailError = null)
        }
    }

    fun saveContact(
        existingContact: ContactEntity?,
        name: String,
        phone: String,
        email: String?,
        company: String?,
        jobTitle: String?,
        notes: String?,
        photoUri: String?,
        isFavorite: Boolean
    ) {
        val validation = ValidationUtils.validateContactForm(
            name = name,
            phone = phone,
            email = email
        )
        _validationErrors.value = validation
        if (!validation.isValid) {
            return
        }

        val now = System.currentTimeMillis()
        val trimmedName = name.trim()
        val trimmedPhone = phone.trim()
        val trimmedEmail = email?.trim()?.takeIf { it.isNotEmpty() }
        val trimmedCompany = company?.trim()?.takeIf { it.isNotEmpty() }
        val trimmedJobTitle = jobTitle?.trim()?.takeIf { it.isNotEmpty() }
        val trimmedNotes = notes?.trim()?.takeIf { it.isNotEmpty() }
        val cleanPhotoUri = photoUri?.trim()?.takeIf { it.isNotEmpty() }

        viewModelScope.launch {
            if (existingContact == null) {
                val entity = ContactEntity(
                    name = trimmedName,
                    phone = trimmedPhone,
                    email = trimmedEmail,
                    company = trimmedCompany,
                    jobTitle = trimmedJobTitle,
                    notes = trimmedNotes,
                    photoUri = cleanPhotoUri,
                    isFavorite = isFavorite,
                    isSample = false,
                    createdAt = now,
                    updatedAt = now
                )
                repository.insertContact(entity)
            } else {
                val updated = existingContact.copy(
                    name = trimmedName,
                    phone = trimmedPhone,
                    email = trimmedEmail,
                    company = trimmedCompany,
                    jobTitle = trimmedJobTitle,
                    notes = trimmedNotes,
                    photoUri = cleanPhotoUri,
                    isFavorite = isFavorite,
                    updatedAt = now
                )
                repository.updateContact(updated)
            }
            _events.tryEmit(FormEvent.Saved)
        }
    }

    class Factory(private val repository: ContactRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ContactFormViewModel::class.java)) {
                return ContactFormViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

