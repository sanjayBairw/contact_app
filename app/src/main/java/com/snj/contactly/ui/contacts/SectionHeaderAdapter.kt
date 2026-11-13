package com.snj.contactly.ui.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.snj.contactly.databinding.ItemSectionHeaderBinding

class SectionHeaderAdapter(
    private val title: String
) : RecyclerView.Adapter<SectionHeaderAdapter.HeaderViewHolder>() {

    private var visible: Boolean = false
    private var count: Int = 0

    fun update(isVisible: Boolean, itemCount: Int) {
        val wasVisible = visible
        val oldCount = count
        visible = isVisible
        count = itemCount

        when {
            !wasVisible && isVisible -> notifyItemInserted(0)
            wasVisible && !isVisible -> notifyItemRemoved(0)
            wasVisible && isVisible && oldCount != itemCount -> notifyItemChanged(0)
        }
    }

    override fun getItemCount(): Int = if (visible) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val binding = ItemSectionHeaderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HeaderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.bind(title, count)
    }

    class HeaderViewHolder(
        private val binding: ItemSectionHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String, count: Int) {
            binding.tvSectionTitle.text = title
            binding.tvSectionCount.text = count.toString()
        }
    }
}

