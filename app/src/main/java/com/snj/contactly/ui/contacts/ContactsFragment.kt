package com.snj.contactly.ui.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.snj.contactly.ContactlyApplication
import com.snj.contactly.R
import com.snj.contactly.data.local.ContactEntity
import com.snj.contactly.databinding.FragmentContactsBinding
import com.snj.contactly.ui.details.ContactDetailsFragment
import com.snj.contactly.utils.IntentUtils
import kotlinx.coroutines.launch

class ContactsFragment : Fragment() {

    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactsViewModel by viewModels {
        val app = requireActivity().application as ContactlyApplication
        ContactsViewModel.Factory(app.repository)
    }

    private lateinit var favoritesHeaderAdapter: SectionHeaderAdapter
    private lateinit var favoritesAdapter: ContactAdapter
    private lateinit var allHeaderAdapter: SectionHeaderAdapter
    private lateinit var allContactsAdapter: ContactAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupToolbarMenu()
        setupSearchAndFilters()
        setupFab()
        observeUiState()
        observeNavResultMessages()
    }

    private fun setupRecyclerView() {
        val onContactClick: (ContactEntity) -> Unit = { contact ->
            navigateToDetails(contact.id)
        }
        val onFavoriteClick: (ContactEntity) -> Unit = { contact ->
            viewModel.toggleFavorite(contact)
        }
        val onCallClick: (ContactEntity) -> Unit = { contact ->
            when (val result = IntentUtils.launchDialer(requireContext(), contact.phone)) {
                is IntentUtils.IntentLaunchResult.Success -> Unit
                is IntentUtils.IntentLaunchResult.Failure -> showSnackbar(result.reason)
            }
        }

        favoritesHeaderAdapter = SectionHeaderAdapter(getString(R.string.section_favorites))
        favoritesAdapter = ContactAdapter(onContactClick, onFavoriteClick, onCallClick)
        allHeaderAdapter = SectionHeaderAdapter(getString(R.string.section_all_contacts))
        allContactsAdapter = ContactAdapter(onContactClick, onFavoriteClick, onCallClick)

        val concatAdapter = ConcatAdapter(
            favoritesHeaderAdapter,
            favoritesAdapter,
            allHeaderAdapter,
            allContactsAdapter
        )

        binding.rvContacts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = concatAdapter
            setHasFixedSize(false)
        }
    }

    private fun setupToolbarMenu() {
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_load_samples -> {
                    viewModel.loadSampleContacts()
                    true
                }
                R.id.action_remove_samples -> {
                    viewModel.removeSampleContacts()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupSearchAndFilters() {
        binding.etSearch.doAfterTextChanged { editable ->
            val text = editable?.toString().orEmpty()
            binding.btnClearSearch.visibility = if (text.isNotEmpty()) View.VISIBLE else View.GONE
            viewModel.updateSearchQuery(text)
        }

        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.setText("")
            binding.etSearch.clearFocus()
        }

        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            val mode = if (checkedIds.contains(R.id.chipFavorites)) {
                ContactFilterMode.FAVORITES
            } else {
                ContactFilterMode.ALL
            }
            viewModel.setFilterMode(mode)
        }
    }

    private fun setupFab() {
        binding.fabAddContact.setOnClickListener {
            navigateToAdd()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        renderState(state)
                    }
                }
                launch {
                    viewModel.uiMessages.collect { message ->
                        showSnackbar(message)
                    }
                }
            }
        }
    }

    private fun observeNavResultMessages() {
        val navBackStackEntry = findNavController().currentBackStackEntry ?: return
        navBackStackEntry.savedStateHandle
            .getLiveData<String>(KEY_SNACKBAR_MESSAGE)
            .observe(viewLifecycleOwner) { message ->
                if (!message.isNullOrBlank()) {
                    showSnackbar(message)
                    navBackStackEntry.savedStateHandle.remove<String>(KEY_SNACKBAR_MESSAGE)
                }
            }
    }

    private fun renderState(state: ContactsUiState) {
        binding.tvContactCount.text = when (state.totalContactsCount) {
            0 -> getString(R.string.home_subtitle_empty)
            1 -> getString(R.string.home_subtitle_single_contact, state.totalFavoritesCount)
            else -> getString(
                R.string.home_subtitle_count,
                state.totalContactsCount,
                state.totalFavoritesCount
            )
        }

        val menu = binding.toolbar.menu
        menu.findItem(R.id.action_load_samples)?.isVisible = !state.hasSampleContacts
        menu.findItem(R.id.action_remove_samples)?.isVisible = state.hasSampleContacts

        if (state.showFavoritesSection) {
            favoritesHeaderAdapter.update(true, state.favoriteContacts.size)
            favoritesAdapter.submitList(state.favoriteContacts)
        } else {
            favoritesHeaderAdapter.update(false, 0)
            favoritesAdapter.submitList(emptyList())
        }

        allHeaderAdapter.update(state.showAllContactsHeader, state.regularOrAllContacts.size)
        allContactsAdapter.submitList(state.regularOrAllContacts)

        renderEmptyState(state)
    }

    private fun renderEmptyState(state: ContactsUiState) {
        when (state.emptyState) {
            EmptyStateType.NONE -> {
                binding.layoutEmptyState.visibility = View.GONE
                binding.rvContacts.visibility = View.VISIBLE
            }
            EmptyStateType.NO_CONTACTS -> {
                binding.rvContacts.visibility = View.GONE
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.ivEmptyIcon.setImageResource(R.drawable.ic_contacts_empty)
                binding.tvEmptyTitle.setText(R.string.empty_contacts_title)
                binding.tvEmptySubtitle.setText(R.string.empty_contacts_subtitle)
                binding.btnEmptyPrimary.visibility = View.VISIBLE
                binding.btnEmptyPrimary.setText(R.string.action_add_contact)
                binding.btnEmptyPrimary.setIconResource(R.drawable.ic_add)
                binding.btnEmptyPrimary.setOnClickListener { navigateToAdd() }

                binding.btnEmptySecondary.visibility = View.VISIBLE
                binding.btnEmptySecondary.setText(R.string.action_load_samples)
                binding.btnEmptySecondary.setOnClickListener { viewModel.loadSampleContacts() }
            }
            EmptyStateType.NO_SEARCH_RESULTS -> {
                binding.rvContacts.visibility = View.GONE
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.ivEmptyIcon.setImageResource(R.drawable.ic_search)
                binding.tvEmptyTitle.setText(R.string.empty_search_title)
                binding.tvEmptySubtitle.text = getString(
                    R.string.empty_search_subtitle,
                    state.searchQuery.trim()
                )
                binding.btnEmptyPrimary.visibility = View.VISIBLE
                binding.btnEmptyPrimary.setText(R.string.clear_search)
                binding.btnEmptyPrimary.setIconResource(R.drawable.ic_close)
                binding.btnEmptyPrimary.setOnClickListener {
                    binding.etSearch.setText("")
                }
                binding.btnEmptySecondary.visibility = View.GONE
            }
            EmptyStateType.NO_FAVORITES -> {
                binding.rvContacts.visibility = View.GONE
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.ivEmptyIcon.setImageResource(R.drawable.ic_star_outline)
                binding.tvEmptyTitle.setText(R.string.empty_favorites_title)
                binding.tvEmptySubtitle.setText(R.string.empty_favorites_subtitle)
                binding.btnEmptyPrimary.visibility = View.VISIBLE
                binding.btnEmptyPrimary.setText(R.string.filter_all)
                binding.btnEmptyPrimary.icon = null
                binding.btnEmptyPrimary.setOnClickListener {
                    binding.chipAll.isChecked = true
                }
                binding.btnEmptySecondary.visibility = View.GONE
            }
        }
    }

    private fun navigateToAdd() {
        val navController = findNavController()
        if (navController.currentDestination?.id == R.id.contactsFragment) {
            navController.navigate(R.id.action_contacts_to_add)
        }
    }

    private fun navigateToDetails(contactId: Int) {
        val navController = findNavController()
        if (navController.currentDestination?.id == R.id.contactsFragment) {
            val args = Bundle().apply {
                putInt(ContactDetailsFragment.ARG_CONTACT_ID, contactId)
            }
            navController.navigate(R.id.action_contacts_to_details, args)
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.contactsRoot, message, Snackbar.LENGTH_SHORT)
            .setAnchorView(binding.fabAddContact)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val KEY_SNACKBAR_MESSAGE = "snackbar_message"
    }
}

