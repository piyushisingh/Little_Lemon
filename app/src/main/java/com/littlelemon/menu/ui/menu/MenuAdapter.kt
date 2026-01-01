package com.littlelemon.menu.ui.menu

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.littlelemon.menu.R
import com.littlelemon.menu.data.model.MenuItem
import com.littlelemon.menu.databinding.ItemMenuGridBinding

/**
 * ListAdapter (backed by DiffUtil) for the 2-column menu grid.
 *
 * Uses ViewBinding so there is no `findViewById` anywhere.
 * Item click is forwarded to the caller via [onItemClick] lambda,
 * keeping the adapter free of navigation concerns.
 */
class MenuAdapter(
    private val onItemClick: (MenuItem) -> Unit
) : ListAdapter<MenuItem, MenuAdapter.MenuViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = ItemMenuGridBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MenuViewHolder(
        private val binding: ItemMenuGridBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(item: MenuItem) {
            binding.tvItemName.text = item.name

            // Glide handles image loading, caching, and placeholder gracefully
            Glide.with(binding.ivItemImage.context)
                .load(item.imageRes)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .centerCrop()
                .into(binding.ivItemImage)
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MenuItem>() {
            override fun areItemsTheSame(old: MenuItem, new: MenuItem) = old.id == new.id
            override fun areContentsTheSame(old: MenuItem, new: MenuItem) = old == new
        }
    }
}
