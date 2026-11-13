package com.snj.contactly.ui.form

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.widget.doAfterTextChanged
import com.snj.contactly.data.local.ContactEntity
import com.snj.contactly.databinding.FragmentContactFormBinding
import com.snj.contactly.utils.AvatarUtils
import com.snj.contactly.utils.ValidationUtils

class ContactFormBinder(
    private val binding: FragmentContactFormBinding,
    private val viewModel: ContactFormViewModel,
    private val photoPickerLauncher: ActivityResultLauncher<Array<String>>,
    private val onCancel: () -> Unit
) {
    var selectedPhotoUri: String? = null
        private set

    var existingContact: ContactEntity? = null
        private set

    fun setupCommon(
        title: String,
        saveLabel: String
    ) {
        binding.toolbar.title = title
        binding.btnSave.text = saveLabel

        binding.toolbar.setNavigationOnClickListener {
            hideKeyboard()
            onCancel()
        }
        binding.btnCancel.setOnClickListener {
            hideKeyboard()
            onCancel()
        }

        binding.btnChoosePhoto.setOnClickListener {
            photoPickerLauncher.launch(arrayOf("image/*"))
        }

        binding.btnRemovePhoto.setOnClickListener {
            selectedPhotoUri = null
            refreshAvatarPreview()
        }

        binding.etFullName.doAfterTextChanged {
            viewModel.clearNameError()
            if (selectedPhotoUri == null) {
                refreshAvatarPreview()
            }
        }
        binding.etPhone.doAfterTextChanged { viewModel.clearPhoneError() }
        binding.etEmail.doAfterTextChanged { viewModel.clearEmailError() }

        binding.btnSave.setOnClickListener {
            hideKeyboard()
            submitForm()
        }

        refreshAvatarPreview()
    }

    fun populateContact(contact: ContactEntity) {
        existingContact = contact
        selectedPhotoUri = contact.photoUri
        binding.etFullName.setText(contact.name)
        binding.etPhone.setText(contact.phone)
        binding.etEmail.setText(contact.email.orEmpty())
        binding.etCompany.setText(contact.company.orEmpty())
        binding.etJobTitle.setText(contact.jobTitle.orEmpty())
        binding.etNotes.setText(contact.notes.orEmpty())
        binding.switchFavorite.isChecked = contact.isFavorite
        refreshAvatarPreview()
    }

    fun onPhotoSelected(context: Context, uri: Uri?) {
        if (uri == null) return
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // Some document providers do not grant persistable permissions; still bind URI
        }
        selectedPhotoUri = uri.toString()
        refreshAvatarPreview()
    }

    fun renderValidationErrors(errors: ValidationUtils.ContactFormValidation) {
        binding.tilFullName.error = errors.nameError
        binding.tilPhone.error = errors.phoneError
        binding.tilEmail.error = errors.emailError
    }

    private fun submitForm() {
        viewModel.saveContact(
            existingContact = existingContact,
            name = binding.etFullName.text?.toString().orEmpty(),
            phone = binding.etPhone.text?.toString().orEmpty(),
            email = binding.etEmail.text?.toString(),
            company = binding.etCompany.text?.toString(),
            jobTitle = binding.etJobTitle.text?.toString(),
            notes = binding.etNotes.text?.toString(),
            photoUri = selectedPhotoUri,
            isFavorite = binding.switchFavorite.isChecked
        )
    }

    private fun refreshAvatarPreview() {
        val currentName = binding.etFullName.text?.toString().orEmpty().ifBlank { "?" }
        AvatarUtils.bindAvatar(
            context = binding.root.context,
            name = currentName,
            photoUri = selectedPhotoUri,
            imageView = binding.ivFormPhoto,
            initialsView = binding.tvFormInitials
        )
        binding.btnRemovePhoto.visibility =
            if (!selectedPhotoUri.isNullOrBlank()) View.VISIBLE else View.GONE
    }

    private fun hideKeyboard() {
        val imm = binding.root.context
            .getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }
}

