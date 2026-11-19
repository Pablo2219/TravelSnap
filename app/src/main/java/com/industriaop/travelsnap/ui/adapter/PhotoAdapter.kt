package com.industriaop.travelsnap.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.industriaop.travelsnap.R
import com.industriaop.travelsnap.ui.common.loadPhoto

class PhotoAdapter(
    private val onRemove: ((String) -> Unit)? = null
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {
    private var photos: List<String> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(photos[position])
    }

    override fun getItemCount(): Int = photos.size

    fun updatePhotos(photos: List<String>) {
        this.photos = photos
        notifyDataSetChanged()
    }

    inner class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imgPhoto = view.findViewById<ImageView>(R.id.imgPhoto)
        private val btnRemove = view.findViewById<ImageButton>(R.id.btnRemove)

        fun bind(uri: String) {
            imgPhoto.loadPhoto(uri)
            btnRemove.isVisible = onRemove != null
            btnRemove.setOnClickListener { onRemove?.invoke(uri) }
        }
    }
}
