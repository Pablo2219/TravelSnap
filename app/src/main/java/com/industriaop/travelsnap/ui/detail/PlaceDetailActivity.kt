package com.industriaop.travelsnap.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.industriaop.travelsnap.domain.model.Place
import com.industriaop.travelsnap.R
import com.industriaop.travelsnap.ui.adapter.PhotoAdapter
import com.industriaop.travelsnap.ui.common.applySystemBarsPadding
import com.industriaop.travelsnap.ui.common.loadPhoto
import com.industriaop.travelsnap.ui.common.toDisplayDate
import com.industriaop.travelsnap.ui.form.PlaceFormActivity
import com.industriaop.travelsnap.util.labelRes
import com.industriaop.travelsnap.util.showConfirmation
import com.industriaop.travelsnap.viewmodel.PlaceViewModel

/** Displays one place and provides edit, favorite and delete actions. */
class PlaceDetailActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnDelete: MaterialButton
    private lateinit var btnEdit: MaterialButton
    private lateinit var btnFavorite: ImageButton
    private lateinit var chipCategory: Chip
    private lateinit var imgCover: ImageView
    private lateinit var rvGallery: RecyclerView
    private lateinit var scrollContent: ScrollView
    private lateinit var tvCoordinates: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvName: TextView
    private lateinit var tvNoPhotos: TextView
    private lateinit var tvPlaceId: TextView
    private lateinit var tvProvince: TextView
    private val galleryAdapter = PhotoAdapter()
    private val viewModel: PlaceViewModel by viewModels()
    private val placeId: Long by lazy { intent.getLongExtra(EXTRA_PLACE_ID, NO_ID) }
    private var hasShownPlace = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (placeId == NO_ID) {
            finish()
            return
        }
        setContentView(R.layout.activity_place_detail)
        findViewById<View>(R.id.root).applySystemBarsPadding()

        btnBack = findViewById(R.id.btnBack)
        btnDelete = findViewById(R.id.btnDelete)
        btnEdit = findViewById(R.id.btnEdit)
        btnFavorite = findViewById(R.id.btnFavorite)
        chipCategory = findViewById(R.id.chipCategory)
        imgCover = findViewById(R.id.imgCover)
        rvGallery = findViewById(R.id.rvGallery)
        scrollContent = findViewById(R.id.scrollContent)
        tvCoordinates = findViewById(R.id.tvCoordinates)
        tvDate = findViewById(R.id.tvDate)
        tvDescription = findViewById(R.id.tvDescription)
        tvName = findViewById(R.id.tvName)
        tvNoPhotos = findViewById(R.id.tvNoPhotos)
        tvPlaceId = findViewById(R.id.tvPlaceId)
        tvProvince = findViewById(R.id.tvProvince)

        rvGallery.adapter = galleryAdapter
        btnBack.setOnClickListener { finish() }
        btnFavorite.setOnClickListener {
            viewModel.toggleFavorite(placeId)
            loadPlace()
        }
        btnEdit.setOnClickListener {
            startActivity(PlaceFormActivity.newIntent(this, placeId))
        }
        btnDelete.setOnClickListener { confirmDelete() }
    }

    override fun onResume() {
        super.onResume()
        if (::btnBack.isInitialized) loadPlace()
    }

    private fun loadPlace() {
        val place = viewModel.getPlaceById(placeId)
        if (place == null) {
            if (!hasShownPlace) {
                Toast.makeText(this, R.string.msg_place_not_found, Toast.LENGTH_SHORT).show()
            }
            finish()
            return
        }
        bindPlace(place)
    }

    private fun bindPlace(place: Place) {
        hasShownPlace = true
        scrollContent.isInvisible = false
        imgCover.loadPhoto(place.photoUris.firstOrNull())
        tvName.text = place.name
        tvProvince.text = getString(place.province.labelRes())
        tvPlaceId.text = getString(R.string.place_id_label, place.id)
        tvDate.text = getString(R.string.detail_added_on, place.createdAt.toDisplayDate())
        chipCategory.text = getString(place.category.labelRes())
        tvDescription.text = place.description
        tvCoordinates.text = getString(
            R.string.detail_coordinates,
            place.latitude,
            place.longitude
        )
        btnFavorite.setImageResource(
            if (place.isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
        )
        btnFavorite.contentDescription = getString(
            if (place.isFavorite) R.string.favorite_remove else R.string.favorite_add
        )
        galleryAdapter.updatePhotos(place.photoUris)
        rvGallery.isVisible = place.photoUris.isNotEmpty()
        tvNoPhotos.isVisible = place.photoUris.isEmpty()
    }

    private fun confirmDelete() {
        val place = viewModel.getPlaceById(placeId) ?: return
        showConfirmation(
            this, R.string.dialog_delete_title,
            getString(R.string.dialog_delete_message, place.name), R.string.button_delete
        ) {
            viewModel.deletePlace(placeId)
            Toast.makeText(this, R.string.msg_place_deleted, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    companion object {
        private const val EXTRA_PLACE_ID = "extra_place_id"
        private const val NO_ID = -1L

        fun newIntent(context: Context, placeId: Long): Intent =
            Intent(context, PlaceDetailActivity::class.java).putExtra(EXTRA_PLACE_ID, placeId)
    }
}
