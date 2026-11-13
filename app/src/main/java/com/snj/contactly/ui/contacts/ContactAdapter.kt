package com.snj.contactly.ui.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.snj.contactly.data.local.ContactEntity
import com.snj.contactly.databinding.ItemContactBinding

class ContactAdapter(
    private val onContactClick: (ContactEntity) -> Unit,
    private val onFavoriteClick: (ContactEntity) -> Unit,
    private val onCallClick: (ContactEntity) -> Unit
) : ListAdapter<ContactEntity, ContactViewHolder>(ContactDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemContactBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ContactViewHolder(
            binding = binding,
            onContactClick = onContactClick,
            onFavoriteClick = onFavoriteClick,
            onCallClick = onCallClick
        )
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class ContactDiffCallback : DiffUtil.ItemCallback<ContactEntity>() {
        override fun areItemsTheSame(oldItem: ContactEntity, newItem: ContactEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ContactEntity, newItem: ContactEntity): Boolean {
            return oldItem == newItem
        }
    }
}

