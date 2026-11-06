package com.industriaop.travelsnap.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.industriaop.travelsnap.R
import com.industriaop.travelsnap.TravelSnapApp
import com.industriaop.travelsnap.databinding.ActivityPlaceDetailBinding
import com.industriaop.travelsnap.domain.model.Place
import com.industriaop.travelsnap.ui.adapter.PhotoAdapter
import com.industriaop.travelsnap.ui.common.applySystemBarsPadding
import com.industriaop.travelsnap.ui.common.loadPhoto
import com.industriaop.travelsnap.ui.common.toDisplayDate
import com.industriaop.travelsnap.ui.form.PlaceFormActivity
import com.industriaop.travelsnap.util.labelRes

/** Displays one place and provides edit, favorite and delete actions. */
class PlaceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaceDetailBinding
    private val galleryAdapter = PhotoAdapter()
    private val repository get() = (application as TravelSnapApp).repository
    private val placeId: Long by lazy { intent.getLongExtra(EXTRA_PLACE_ID, NO_ID) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (placeId == NO_ID) {
            finish()
            return
        }
        binding = ActivityPlaceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()

        binding.rvGallery.adapter = galleryAdapter
        binding.btnBack.setOnClickListener { finish() }
        binding.btnFavorite.setOnClickListener {
            repository.toggleFavorite(placeId)
            loadPlace()
        }
        binding.btnEdit.setOnClickListener {
            startActivity(PlaceFormActivity.newIntent(this, placeId))
        }
        binding.btnDelete.setOnClickListener { confirmDelete() }
        loadPlace()
    }

    override fun onResume() {
        super.onResume()
        if (::binding.isInitialized) loadPlace()
    }

    private fun loadPlace() {
        val place = repository.getPlaceById(placeId)
        if (place == null) {
            Toast.makeText(this, R.string.msg_place_not_found, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        bindPlace(place)
    }

    private fun bindPlace(place: Place) {
        binding.scrollContent.isInvisible = false
        binding.imgCover.loadPhoto(place.photoUris.firstOrNull())
        binding.tvName.text = place.name
        binding.tvProvince.text = getString(place.province.labelRes())
        binding.tvDate.text = getString(R.string.detail_added_on, place.createdAt.toDisplayDate())
        binding.chipCategory.text = getString(place.category.labelRes())
        binding.tvDescription.text = place.description
        binding.tvCoordinates.text = getString(
            R.string.detail_coordinates,
            place.latitude,
            place.longitude
        )
        binding.btnFavorite.setImageResource(
            if (place.isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
        )
        binding.btnFavorite.contentDescription = getString(
            if (place.isFavorite) R.string.favorite_remove else R.string.favorite_add
        )
        galleryAdapter.submitList(place.photoUris)
        binding.rvGallery.isVisible = place.photoUris.isNotEmpty()
        binding.tvNoPhotos.isVisible = place.photoUris.isEmpty()
    }

    private fun confirmDelete() {
        val place = repository.getPlaceById(placeId) ?: return
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_delete_title)
            .setMessage(getString(R.string.dialog_delete_message, place.name))
            .setNegativeButton(R.string.button_cancel, null)
            .setPositiveButton(R.string.dialog_confirm) { _, _ ->
                repository.deletePlace(placeId)
                Toast.makeText(this, R.string.msg_place_deleted, Toast.LENGTH_SHORT).show()
                finish()
            }
            .show()
    }

    companion object {
        private const val EXTRA_PLACE_ID = "extra_place_id"
        private const val NO_ID = -1L

        fun newIntent(context: Context, placeId: Long): Intent =
            Intent(context, PlaceDetailActivity::class.java).putExtra(EXTRA_PLACE_ID, placeId)
    }
}
