package com.industriaop.travelsnap.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.chip.Chip
import com.industriaop.travelsnap.R
import com.industriaop.travelsnap.TravelSnapApp
import com.industriaop.travelsnap.databinding.ActivityMapBinding
import com.industriaop.travelsnap.domain.model.Place
import com.industriaop.travelsnap.domain.model.PlaceFilter
import com.industriaop.travelsnap.ui.common.BottomNav
import com.industriaop.travelsnap.ui.common.applySystemBarsPadding
import com.industriaop.travelsnap.ui.common.loadPhoto
import com.industriaop.travelsnap.ui.detail.PlaceDetailActivity
import com.industriaop.travelsnap.util.labelRes

/**
 * Map screen placeholder. Places are represented by selectable chips until
 * the Google Maps feature is covered in class.
 */
class MapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapBinding
    private val repository get() = (application as TravelSnapApp).repository
    private var selectedPlaceId: Long? = null
    private var shownPlaceIds: List<Long> = emptyList()
    private val chipsByPlaceId = mutableMapOf<Long, Chip>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()
        BottomNav.setup(this, binding.bottomNav, R.id.nav_map)
    }

    override fun onResume() {
        super.onResume()
        if (::binding.isInitialized) refreshPlaces()
    }

    private fun refreshPlaces() {
        val places = repository.getPlaces(PlaceFilter())
        binding.tvNoPlaces.isVisible = places.isEmpty()
        renderMarkers(places)
        val selectedPlace = places.firstOrNull { it.id == selectedPlaceId }
        if (selectedPlace == null) selectedPlaceId = null
        chipsByPlaceId.forEach { (id, chip) -> chip.isChecked = id == selectedPlaceId }
        renderSelectedCard(selectedPlace)
    }

    private fun renderMarkers(places: List<Place>) {
        val ids = places.map { it.id }
        if (ids == shownPlaceIds) return
        shownPlaceIds = ids
        binding.chipGroupMarkers.removeAllViews()
        chipsByPlaceId.clear()

        val inflater = LayoutInflater.from(this)
        places.forEach { place ->
            val chip = inflater.inflate(
                R.layout.item_filter_chip,
                binding.chipGroupMarkers,
                false
            ) as Chip
            chip.id = View.generateViewId()
            chip.text = place.name
            chip.setChipIconResource(R.drawable.ic_place)
            chip.isChipIconVisible = true
            chip.setOnClickListener {
                selectedPlaceId = if (chip.isChecked) place.id else null
                refreshPlaces()
            }
            binding.chipGroupMarkers.addView(chip)
            chipsByPlaceId[place.id] = chip
        }
    }

    private fun renderSelectedCard(place: Place?) {
        binding.cardSelected.isVisible = place != null
        if (place == null) return

        binding.imgSelected.loadPhoto(place.photoUris.firstOrNull())
        binding.tvSelectedName.text = place.name
        binding.tvSelectedLocation.text = getString(
            R.string.place_location_format,
            getString(place.province.labelRes()),
            getString(place.category.labelRes())
        )
        binding.tvSelectedCoordinates.text = getString(
            R.string.detail_coordinates,
            place.latitude,
            place.longitude
        )
        binding.btnViewDetails.setOnClickListener {
            startActivity(PlaceDetailActivity.newIntent(this, place.id))
        }
    }
}
