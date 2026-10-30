package com.industriaop.travelsnap.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.chip.Chip
import com.industriaop.travelsnap.R
import com.industriaop.travelsnap.TravelSnapApp
import com.industriaop.travelsnap.databinding.ActivityMapBinding
import com.industriaop.travelsnap.domain.model.Place
import com.industriaop.travelsnap.ui.common.BottomNav
import com.industriaop.travelsnap.ui.common.applySystemBarsPadding
import com.industriaop.travelsnap.ui.common.loadPhoto
import com.industriaop.travelsnap.ui.detail.PlaceDetailActivity
import com.industriaop.travelsnap.util.labelRes
import com.industriaop.travelsnap.viewmodel.MapUiState
import com.industriaop.travelsnap.viewmodel.MapViewModel
import com.industriaop.travelsnap.viewmodel.ViewModelFactories
import kotlinx.coroutines.launch

/**
 * Map screen (mockup 6).
 *
 * The real Google Map is not integrated yet: the map area is a placeholder and every place is
 * shown as a chip that works like a marker (tap it to select the place and see its card).
 * Later, replace `mapContainer` with the map fragment and call
 * `viewModel.onMarkerSelected(placeId)` from the marker click listener.
 */
class MapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapBinding

    private val viewModel: MapViewModel by viewModels {
        ViewModelFactories.map((application as TravelSnapApp).repository)
    }

    /** Ids of the places currently drawn as chips, to rebuild them only when the list changes. */
    private var shownPlaceIds: List<Long> = emptyList()
    private val chipsByPlaceId = mutableMapOf<Long, Chip>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()

        BottomNav.setup(this, binding.bottomNav, R.id.nav_map)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { render(it) }
            }
        }
    }

    private fun render(state: MapUiState) {
        binding.tvNoPlaces.isVisible = state.places.isEmpty()
        renderMarkers(state.places)

        // Check the chip of the selected place (or none).
        chipsByPlaceId.forEach { (id, chip) -> chip.isChecked = id == state.selectedPlace?.id }
        renderSelectedCard(state.selectedPlace)
    }

    private fun renderMarkers(places: List<Place>) {
        val ids = places.map { it.id }
        if (ids == shownPlaceIds) return
        shownPlaceIds = ids

        binding.chipGroupMarkers.removeAllViews()
        chipsByPlaceId.clear()
        val inflater = LayoutInflater.from(this)
        places.forEach { place ->
            val chip = inflater.inflate(R.layout.item_filter_chip, binding.chipGroupMarkers, false) as Chip
            chip.id = View.generateViewId()
            chip.text = place.name
            chip.setChipIconResource(R.drawable.ic_place)
            chip.isChipIconVisible = true
            chip.setOnClickListener {
                // The chip toggled itself: checked means "select", unchecked means "clear".
                if (chip.isChecked) viewModel.onMarkerSelected(place.id) else viewModel.onSelectionCleared()
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
