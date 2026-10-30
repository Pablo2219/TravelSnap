package com.industriaop.travelsnap.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.industriaop.travelsnap.databinding.ItemPhotoBinding
import com.industriaop.travelsnap.ui.common.loadPhoto

/**
 * Horizontal list of photo URIs.
 * Pass [onRemove] in the form screen to show the "remove" button; leave it null in the detail gallery.
 */
class PhotoAdapter(
    private val onRemove: ((String) -> Unit)? = null
) : ListAdapter<String, PhotoAdapter.PhotoViewHolder>(PhotoDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PhotoViewHolder(
        private val binding: ItemPhotoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(uri: String) {
            binding.imgPhoto.loadPhoto(uri)
            binding.btnRemove.isVisible = onRemove != null
            binding.btnRemove.setOnClickListener { onRemove?.invoke(uri) }
        }
    }

    private object PhotoDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
    }
}
