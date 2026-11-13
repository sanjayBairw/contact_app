package com.snj.contactly.ui.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.snj.contactly.data.local.ContactEntity
import com.snj.contactly.databinding.ItemFavoritesHorizontalContainerBinding

class FavoritesContainerAdapter(
    private val onContactClick: (ContactEntity) -> Unit,
    private val onFavoriteClick: (ContactEntity) -> Unit,
    private val onCallClick: (ContactEntity) -> Unit
) : RecyclerView.Adapter<FavoritesContainerAdapter.FavoritesContainerViewHolder>() {

    private val innerAdapter = FavoriteCardAdapter(onContactClick, onFavoriteClick, onCallClick)
    private var currentFavorites: List<ContactEntity> = emptyList()

    fun submitList(list: List<ContactEntity>) {
        val hadItems = currentFavorites.isNotEmpty()
        val hasItems = list.isNotEmpty()
        currentFavorites = list
        innerAdapter.submitList(list)

        when {
            !hadItems && hasItems -> notifyItemInserted(0)
            hadItems && !hasItems -> notifyItemRemoved(0)
            hadItems && hasItems -> notifyItemChanged(0)
        }
    }

    override fun getItemCount(): Int = if (currentFavorites.isNotEmpty()) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesContainerViewHolder {
        val binding = ItemFavoritesHorizontalContainerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        binding.rvHorizontalFavorites.apply {
            layoutManager = LinearLayoutManager(parent.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = innerAdapter
            setHasFixedSize(false)
        }
        return FavoritesContainerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoritesContainerViewHolder, position: Int) {
        // The inner adapter already handles the items through submitList
    }

    class FavoritesContainerViewHolder(
        binding: ItemFavoritesHorizontalContainerBinding
    ) : RecyclerView.ViewHolder(binding.root)
}

