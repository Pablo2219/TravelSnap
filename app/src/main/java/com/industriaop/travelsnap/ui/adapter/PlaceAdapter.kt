package com.industriaop.travelsnap.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.industriaop.travelsnap.R
import com.industriaop.travelsnap.databinding.ItemPlaceBinding
import com.industriaop.travelsnap.domain.model.Place
import com.industriaop.travelsnap.ui.common.loadPhoto
import com.industriaop.travelsnap.ui.common.toDisplayDate
import com.industriaop.travelsnap.util.labelRes

/** Shows the list of places ("My places" screen). */
class PlaceAdapter(
    private val onClick: (Place) -> Unit,
    private val onFavoriteClick: (Place) -> Unit,
    private val onEditClick: (Place) -> Unit,
    private val onDeleteClick: (Place) -> Unit
) : ListAdapter<Place, PlaceAdapter.PlaceViewHolder>(PlaceDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val binding = ItemPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PlaceViewHolder(
        private val binding: ItemPlaceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(place: Place) {
            val context = binding.root.context
            binding.imgPhoto.loadPhoto(place.photoUris.firstOrNull())
            binding.tvName.text = place.name
            binding.tvLocation.text = context.getString(
                R.string.place_location_format,
                context.getString(place.province.labelRes()),
                context.getString(place.category.labelRes())
            )
            binding.tvDate.text = place.createdAt.toDisplayDate()

            binding.btnFavorite.setImageResource(
                if (place.isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
            )
            binding.btnFavorite.contentDescription = context.getString(
                if (place.isFavorite) R.string.favorite_remove else R.string.favorite_add
            )

            binding.root.setOnClickListener { onClick(place) }
            binding.btnFavorite.setOnClickListener { onFavoriteClick(place) }
            binding.btnMore.setOnClickListener { anchor ->
                val popup = PopupMenu(anchor.context, anchor)
                popup.menuInflater.inflate(R.menu.menu_place_item, popup.menu)
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_edit -> onEditClick(place)
                        R.id.action_delete -> onDeleteClick(place)
                    }
                    true
                }
                popup.show()
            }
        }
    }

    private object PlaceDiffCallback : DiffUtil.ItemCallback<Place>() {
        override fun areItemsTheSame(oldItem: Place, newItem: Place) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Place, newItem: Place) = oldItem == newItem
    }
}
