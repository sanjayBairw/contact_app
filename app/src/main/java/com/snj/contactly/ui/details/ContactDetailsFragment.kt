package com.snj.contactly.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.snj.contactly.ContactlyApplication
import com.snj.contactly.R
import com.snj.contactly.data.local.ContactEntity
import com.snj.contactly.databinding.FragmentContactDetailsBinding
import com.snj.contactly.ui.contacts.ContactsFragment
import com.snj.contactly.ui.edit.EditContactFragment
import com.snj.contactly.utils.AvatarUtils
import com.snj.contactly.utils.IntentUtils
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

class ContactDetailsFragment : Fragment() {

    private var _binding: FragmentContactDetailsBinding? = null
    private val binding get() = _binding!!

    private val contactId: Int by lazy {
        arguments?.getInt(ARG_CONTACT_ID, INVALID_CONTACT_ID) ?: INVALID_CONTACT_ID
    }

    private val viewModel: ContactDetailsViewModel by viewModels {
        val app = requireActivity().application as ContactlyApplication
        ContactDetailsViewModel.Factory(contactId, app.repository)
    }

    private var currentContact: ContactEntity? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (contactId == INVALID_CONTACT_ID) {
            findNavController().popBackStack()
            return
        }

        setupToolbar()
        setupActionButtons()
        observeContactAndEvents()
        observeEditResultMessage()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_toggle_favorite -> {
                    viewModel.toggleFavorite()
                    true
                }
                R.id.action_edit_contact -> {
                    navigateToEdit()
                    true
                }
                R.id.action_delete_contact -> {
                    confirmDelete()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupActionButtons() {
        binding.btnActionCall.setOnClickListener {
            val phone = currentContact?.phone.orEmpty()
            handleIntentResult(IntentUtils.launchDialer(requireContext(), phone))
        }

        binding.rowPhone.setOnClickListener {
            val phone = currentContact?.phone.orEmpty()
            handleIntentResult(IntentUtils.launchDialer(requireContext(), phone))
        }

        binding.btnActionMessage.setOnClickListener {
            val phone = currentContact?.phone.orEmpty()
            handleIntentResult(IntentUtils.launchSms(requireContext(), phone))
        }

        binding.btnActionEmail.setOnClickListener {
            handleIntentResult(IntentUtils.launchEmail(requireContext(), currentContact?.email))
        }

        binding.rowEmail.setOnClickListener {
            handleIntentResult(IntentUtils.launchEmail(requireContext(), currentContact?.email))
        }

        binding.btnEditContact.setOnClickListener {
            navigateToEdit()
        }

        binding.btnDeleteContact.setOnClickListener {
            confirmDelete()
        }
    }

    private fun observeContactAndEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.contact.collect { contact ->
                        if (contact != null) {
                            currentContact = contact
                            bindContact(contact)
                        }
                    }
                }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is ContactDetailsViewModel.DetailsEvent.ShowMessage -> {
                                showSnackbar(event.message)
                            }
                            is ContactDetailsViewModel.DetailsEvent.ContactDeleted -> {
                                val navController = findNavController()
                                navController.previousBackStackEntry?.savedStateHandle?.set(
                                    ContactsFragment.KEY_SNACKBAR_MESSAGE,
                                    getString(R.string.msg_contact_deleted)
                                )
                                navController.popBackStack(R.id.contactsFragment, false)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun observeEditResultMessage() {
        val currentEntry = findNavController().currentBackStackEntry ?: return
        currentEntry.savedStateHandle
            .getLiveData<String>(ContactsFragment.KEY_SNACKBAR_MESSAGE)
            .observe(viewLifecycleOwner) { message ->
                if (!message.isNullOrBlank()) {
                    showSnackbar(message)
                    currentEntry.savedStateHandle.remove<String>(ContactsFragment.KEY_SNACKBAR_MESSAGE)
                }
            }
    }

    private fun bindContact(contact: ContactEntity) {
        val context = requireContext()

        AvatarUtils.bindAvatar(
            context = context,
            name = contact.name,
            photoUri = contact.photoUri,
            imageView = binding.ivDetailPhoto,
            initialsView = binding.tvDetailInitials
        )

        binding.tvDetailName.text = contact.name

        val roleParts = listOfNotNull(
            contact.jobTitle?.takeIf { it.isNotBlank() },
            contact.company?.takeIf { it.isNotBlank() }
        )
        if (roleParts.isNotEmpty()) {
            binding.tvDetailRole.visibility = View.VISIBLE
            binding.tvDetailRole.text = roleParts.joinToString(" • ")
        } else {
            binding.tvDetailRole.visibility = View.GONE
        }

        binding.chipDetailFavorite.visibility = if (contact.isFavorite) View.VISIBLE else View.GONE
        binding.chipDetailSample.visibility = if (contact.isSample) View.VISIBLE else View.GONE
        binding.layoutDetailBadges.visibility =
            if (contact.isFavorite || contact.isSample) View.VISIBLE else View.GONE

        val favoriteMenuItem = binding.toolbar.menu.findItem(R.id.action_toggle_favorite)
        if (contact.isFavorite) {
            favoriteMenuItem?.setIcon(R.drawable.ic_star_filled)
            favoriteMenuItem?.icon?.setTint(
                ContextCompat.getColor(context, R.color.contactly_favorite_active)
            )
        } else {
            favoriteMenuItem?.setIcon(R.drawable.ic_star_outline)
            favoriteMenuItem?.icon?.setTintList(null)
        }

        binding.tvDetailPhone.text = contact.phone

        val email = contact.email?.trim().orEmpty()
        if (email.isNotEmpty()) {
            binding.rowEmail.visibility = View.VISIBLE
            binding.tvDetailEmail.text = email
            binding.btnActionEmail.alpha = 1.0f
        } else {
            binding.rowEmail.visibility = View.GONE
            binding.btnActionEmail.alpha = 0.65f
        }

        val company = contact.company?.trim().orEmpty()
        val jobTitle = contact.jobTitle?.trim().orEmpty()
        if (company.isNotEmpty() || jobTitle.isNotEmpty()) {
            binding.cardWork.visibility = View.VISIBLE
            binding.rowCompany.visibility = if (company.isNotEmpty()) View.VISIBLE else View.GONE
            binding.tvDetailCompany.text = company
            binding.rowJobTitle.visibility = if (jobTitle.isNotEmpty()) View.VISIBLE else View.GONE
            binding.tvDetailJobTitle.text = jobTitle
        } else {
            binding.cardWork.visibility = View.GONE
        }

        val notes = contact.notes?.trim().orEmpty()
        if (notes.isNotEmpty()) {
            binding.cardNotes.visibility = View.VISIBLE
            binding.tvDetailNotes.text = notes
        } else {
            binding.cardNotes.visibility = View.GONE
        }

        val formattedDate = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
            .format(Date(contact.updatedAt))
        binding.tvUpdatedAt.text = getString(R.string.label_updated_at, formattedDate)
    }

    private fun navigateToEdit() {
        val navController = findNavController()
        if (navController.currentDestination?.id == R.id.contactDetailsFragment) {
            val args = Bundle().apply {
                putInt(EditContactFragment.ARG_CONTACT_ID, contactId)
            }
            navController.navigate(R.id.action_details_to_edit, args)
        }
    }

    private fun confirmDelete() {
        val name = currentContact?.name ?: getString(R.string.app_name)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_delete_title)
            .setMessage(getString(R.string.dialog_delete_message, name))
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                viewModel.deleteContact()
            }
            .show()
    }

    private fun handleIntentResult(result: IntentUtils.IntentLaunchResult) {
        if (result is IntentUtils.IntentLaunchResult.Failure) {
            showSnackbar(result.reason)
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.detailsRoot, message, Snackbar.LENGTH_SHORT).show()
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

