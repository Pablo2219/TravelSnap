package com.industriaop.travelsnap.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.industriaop.travelsnap.R
import com.industriaop.travelsnap.domain.model.Place
import com.industriaop.travelsnap.ui.common.loadPhoto
import com.industriaop.travelsnap.ui.common.toDisplayDate
import com.industriaop.travelsnap.util.labelRes

class PlaceAdapter(
    private val onClick: (Place) -> Unit,
    private val onFavoriteClick: (Place) -> Unit,
    private val onEditClick: (Place) -> Unit,
    private val onDeleteClick: (Place) -> Unit
) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {
    private var places: List<Place> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_place, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.bind(places[position])
    }

    override fun getItemCount(): Int = places.size

    fun updatePlaces(places: List<Place>) {
        this.places = places
        notifyDataSetChanged()
    }

    inner class PlaceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imgPhoto = view.findViewById<ImageView>(R.id.imgPhoto)
        private val tvName = view.findViewById<TextView>(R.id.tvName)
        private val tvLocation = view.findViewById<TextView>(R.id.tvLocation)
        private val tvDate = view.findViewById<TextView>(R.id.tvDate)
        private val btnFavorite = view.findViewById<ImageButton>(R.id.btnFavorite)
        private val btnMore = view.findViewById<ImageButton>(R.id.btnMore)

        fun bind(place: Place) {
            val context = itemView.context
            imgPhoto.loadPhoto(place.photoUris.firstOrNull())
            tvName.text = place.name
            tvLocation.text = context.getString(
                R.string.place_location_format,
                context.getString(place.province.labelRes()),
                context.getString(place.category.labelRes())
            )
            tvDate.text = place.createdAt.toDisplayDate()

            btnFavorite.setImageResource(
                if (place.isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
            )
            btnFavorite.contentDescription = context.getString(
                if (place.isFavorite) R.string.favorite_remove else R.string.favorite_add
            )

            itemView.setOnClickListener { onClick(place) }
            btnFavorite.setOnClickListener { onFavoriteClick(place) }
            btnMore.setOnClickListener { anchor ->
                val popup = PopupMenu(anchor.context, anchor)
                popup.menuInflater.inflate(R.menu.menu_place_item, popup.menu)
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_edit -> onEditClick(place)
                        R.id.action_delete -> onDeleteClick(place)
                        else -> return@setOnMenuItemClickListener false
                    }
                    true
                }
                popup.show()
            }
        }
    }
}
