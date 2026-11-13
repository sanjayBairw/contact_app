package com.snj.contactly.ui.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.snj.contactly.data.local.ContactEntity
import com.snj.contactly.databinding.ItemFavoriteCardBinding
import com.snj.contactly.utils.AvatarUtils

class FavoriteCardAdapter(
    private val onContactClick: (ContactEntity) -> Unit,
    private val onFavoriteClick: (ContactEntity) -> Unit,
    private val onCallClick: (ContactEntity) -> Unit
) : ListAdapter<ContactEntity, FavoriteCardAdapter.FavoriteCardViewHolder>(FavoriteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteCardViewHolder {
        val binding = ItemFavoriteCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FavoriteCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteCardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FavoriteCardViewHolder(
        private val binding: ItemFavoriteCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: ContactEntity) {
            val context = binding.root.context
            binding.tvFavContactName.text = contact.name
            binding.tvFavContactPhone.text = contact.phone

            AvatarUtils.bindAvatar(
                context = context,
                name = contact.name,
                photoUri = contact.photoUri,
                imageView = binding.ivFavAvatarPhoto,
                initialsView = binding.tvFavAvatarInitials
            )

            binding.root.setOnClickListener { onContactClick(contact) }
            binding.btnFavStar.setOnClickListener { onFavoriteClick(contact) }
            binding.btnFavQuickCall.setOnClickListener { onCallClick(contact) }
        }
    }

    private class FavoriteDiffCallback : DiffUtil.ItemCallback<ContactEntity>() {
        override fun areItemsTheSame(oldItem: ContactEntity, newItem: ContactEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ContactEntity, newItem: ContactEntity): Boolean {
            return oldItem == newItem
        }
    }
}

