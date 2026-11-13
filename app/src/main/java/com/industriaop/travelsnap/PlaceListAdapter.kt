package com.industriaop.travelsnap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.industriaop.travelsnap.domain.model.Place

class PlaceListAdapter(private val places: List<Place>,
                       private val listener: OnItemClickListener) :
    RecyclerView.Adapter<PlaceViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_place, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.bind(places[position], listener)
    }

    override fun getItemCount(): Int = places.size
}

class PlaceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val name = view.findViewById<TextView>(R.id.tvPlaceName)
    private val province = view.findViewById<TextView>(R.id.tvPlaceProvince)

    fun bind(place: Place, listener: OnItemClickListener) {
        name.text = place.name
        province.text = itemView.resources.getStringArray(R.array.provinces)[place.province.ordinal]
        itemView.setOnClickListener { listener.onItemClick(place) }
    }
}
