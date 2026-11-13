package com.snj.contactly.ui.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.snj.contactly.ContactlyApplication
import com.snj.contactly.R
import com.snj.contactly.databinding.FragmentContactFormBinding
import com.snj.contactly.ui.contacts.ContactsFragment
import com.snj.contactly.ui.form.ContactFormBinder
import com.snj.contactly.ui.form.ContactFormViewModel
import kotlinx.coroutines.launch

class EditContactFragment : Fragment() {

    private var _binding: FragmentContactFormBinding? = null
    private val binding get() = _binding!!

    private val contactId: Int by lazy {
        arguments?.getInt(ARG_CONTACT_ID, INVALID_CONTACT_ID) ?: INVALID_CONTACT_ID
    }

    private val viewModel: ContactFormViewModel by viewModels {
        val app = requireActivity().application as ContactlyApplication
        ContactFormViewModel.Factory(app.repository)
    }

    private lateinit var formBinder: ContactFormBinder
    private var hasPopulatedFields = false

    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        formBinder.onPhotoSelected(requireContext(), uri)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (contactId == INVALID_CONTACT_ID) {
            findNavController().popBackStack()
            return
        }

        formBinder = ContactFormBinder(
            binding = binding,
            viewModel = viewModel,
            photoPickerLauncher = photoPickerLauncher,
            onCancel = { findNavController().popBackStack() }
        )

        formBinder.setupCommon(
            title = getString(R.string.title_edit_contact),
            saveLabel = getString(R.string.action_update)
        )
        binding.btnSave.isEnabled = false

        viewModel.loadContactIfNeeded(contactId)
        observeFormState()
    }

    private fun observeFormState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.loadedContact.collect { contact ->
                        if (contact != null && !hasPopulatedFields) {
                            hasPopulatedFields = true
                            formBinder.populateContact(contact)
                            binding.btnSave.isEnabled = true
                        }
                    }
                }
                launch {
                    viewModel.validationErrors.collect { errors ->
                        formBinder.renderValidationErrors(errors)
                    }
                }
                launch {
                    viewModel.events.collect { event ->
                        val navController = findNavController()
                        when (event) {
                            is ContactFormViewModel.FormEvent.Saved -> {
                                navController.previousBackStackEntry?.savedStateHandle?.set(
                                    ContactsFragment.KEY_SNACKBAR_MESSAGE,
                                    getString(R.string.msg_contact_updated)
                                )
                                navController.popBackStack()
                            }
                            is ContactFormViewModel.FormEvent.ContactMissing -> {
                                navController.previousBackStackEntry?.savedStateHandle?.set(
                                    ContactsFragment.KEY_SNACKBAR_MESSAGE,
                                    getString(R.string.contact_not_found)
                                )
                                navController.popBackStack()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_CONTACT_ID = "contactId"
        private const val INVALID_CONTACT_ID = -1
    }
}
