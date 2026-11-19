package com.industriaop.travelsnap.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.imageview.ShapeableImageView
import com.industriaop.travelsnap.domain.model.Place
import com.industriaop.travelsnap.R
import com.industriaop.travelsnap.ui.common.applySystemBarsPadding
import com.industriaop.travelsnap.ui.common.BottomNav
import com.industriaop.travelsnap.ui.common.loadPhoto
import com.industriaop.travelsnap.ui.detail.PlaceDetailActivity
import com.industriaop.travelsnap.util.labelRes
import com.industriaop.travelsnap.viewmodel.PlaceViewModel

/**
 * Map screen placeholder. Places are represented by selectable chips until
 * the Google Maps feature is covered in class.
 */
class MapActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var btnViewDetails: MaterialButton
    private lateinit var cardSelected: MaterialCardView
    private lateinit var chipGroupMarkers: ChipGroup
    private lateinit var imgSelected: ShapeableImageView
    private lateinit var tvNoPlaces: TextView
    private lateinit var tvSelectedCoordinates: TextView
    private lateinit var tvSelectedLocation: TextView
    private lateinit var tvSelectedName: TextView
    private val viewModel: PlaceViewModel by viewModels()
    private var selectedPlaceId: Long? = null
    private var shownPlaces: List<Place> = emptyList()
    private val chipsByPlaceId = mutableMapOf<Long, Chip>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_map)
        findViewById<View>(R.id.root).applySystemBarsPadding()

        bottomNav = findViewById(R.id.bottomNav)
        btnViewDetails = findViewById(R.id.btnViewDetails)
        cardSelected = findViewById(R.id.cardSelected)
        chipGroupMarkers = findViewById(R.id.chipGroupMarkers)
        imgSelected = findViewById(R.id.imgSelected)
        tvNoPlaces = findViewById(R.id.tvNoPlaces)
        tvSelectedCoordinates = findViewById(R.id.tvSelectedCoordinates)
        tvSelectedLocation = findViewById(R.id.tvSelectedLocation)
        tvSelectedName = findViewById(R.id.tvSelectedName)
        BottomNav.setup(this, bottomNav, R.id.nav_map)
    }

    override fun onResume() {
        super.onResume()
        if (::bottomNav.isInitialized) refreshPlaces()
    }

    private fun refreshPlaces() {
        val places = viewModel.getPlaces()
        tvNoPlaces.isVisible = places.isEmpty()
        renderMarkers(places)
        val selectedPlace = places.firstOrNull { it.id == selectedPlaceId }
        if (selectedPlace == null) selectedPlaceId = null
        chipsByPlaceId.forEach { (id, chip) -> chip.isChecked = id == selectedPlaceId }
        renderSelectedCard(selectedPlace)
    }

    private fun renderMarkers(places: List<Place>) {
        if (places == shownPlaces) return
        shownPlaces = places
        chipGroupMarkers.removeAllViews()
        chipsByPlaceId.clear()

        val inflater = LayoutInflater.from(this)
        places.forEach { place ->
            val chip = inflater.inflate(
                R.layout.item_filter_chip,
                chipGroupMarkers,
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
            chipGroupMarkers.addView(chip)
            chipsByPlaceId[place.id] = chip
        }
    }

    private fun renderSelectedCard(place: Place?) {
        cardSelected.isVisible = place != null
        if (place == null) return

        imgSelected.loadPhoto(place.photoUris.firstOrNull())
        tvSelectedName.text = place.name
        tvSelectedLocation.text = getString(
            R.string.place_location_format,
            getString(place.province.labelRes()),
            getString(place.category.labelRes())
        )
        tvSelectedCoordinates.text = getString(
            R.string.detail_coordinates,
            place.latitude,
            place.longitude
        )
        btnViewDetails.setOnClickListener {
            startActivity(PlaceDetailActivity.newIntent(this, place.id))
        }
    }
}
