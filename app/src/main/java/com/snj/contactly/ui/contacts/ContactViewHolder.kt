package com.snj.contactly.ui.contacts

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.snj.contactly.R
import com.snj.contactly.data.local.ContactEntity
import com.snj.contactly.databinding.ItemContactBinding
import com.snj.contactly.utils.AvatarUtils

class ContactViewHolder(
    private val binding: ItemContactBinding,
    private val onContactClick: (ContactEntity) -> Unit,
    private val onFavoriteClick: (ContactEntity) -> Unit,
    private val onCallClick: (ContactEntity) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(contact: ContactEntity) {
        val context = binding.root.context

        binding.tvContactName.text = contact.name
        binding.tvContactPhone.text = contact.phone
        binding.tvSampleBadge.visibility = if (contact.isSample) View.VISIBLE else View.GONE

        AvatarUtils.bindAvatar(
            context = context,
            name = contact.name,
            photoUri = contact.photoUri,
            imageView = binding.ivAvatarPhoto,
            initialsView = binding.tvAvatarInitials
        )

        if (contact.isFavorite) {
            binding.btnFavorite.setImageResource(R.drawable.ic_star_filled)
            binding.btnFavorite.setColorFilter(
                ContextCompat.getColor(context, R.color.contactly_favorite_active)
            )
            binding.btnFavorite.contentDescription =
                context.getString(R.string.msg_removed_favorite)
        } else {
            binding.btnFavorite.setImageResource(R.drawable.ic_star_outline)
            binding.btnFavorite.setColorFilter(
                ContextCompat.getColor(context, R.color.contactly_favorite_inactive)
            )
            binding.btnFavorite.contentDescription =
                context.getString(R.string.action_toggle_favorite)
        }

        binding.root.setOnClickListener { onContactClick(contact) }
        binding.btnFavorite.setOnClickListener { onFavoriteClick(contact) }
        binding.btnQuickCall.setOnClickListener { onCallClick(contact) }
    }
}

