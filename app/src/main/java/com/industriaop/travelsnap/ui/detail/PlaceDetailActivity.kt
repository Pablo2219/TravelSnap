package com.industriaop.travelsnap.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.industriaop.travelsnap.R
import com.industriaop.travelsnap.TravelSnapApp
import com.industriaop.travelsnap.databinding.ActivityPlaceDetailBinding
import com.industriaop.travelsnap.domain.model.Place
import com.industriaop.travelsnap.ui.adapter.PhotoAdapter
import com.industriaop.travelsnap.ui.common.applySystemBarsPadding
import com.industriaop.travelsnap.ui.common.loadPhoto
import com.industriaop.travelsnap.ui.common.show
import com.industriaop.travelsnap.ui.common.toDisplayDate
import com.industriaop.travelsnap.ui.dialog.DeleteConfirmDialog
import com.industriaop.travelsnap.ui.form.PlaceFormActivity
import com.industriaop.travelsnap.util.MessageType
import com.industriaop.travelsnap.util.UiMessage
import com.industriaop.travelsnap.util.labelRes
import com.industriaop.travelsnap.viewmodel.PlaceDetailUiState
import com.industriaop.travelsnap.viewmodel.PlaceDetailViewModel
import com.industriaop.travelsnap.viewmodel.ViewModelFactories
import kotlinx.coroutines.launch

/** Place detail screen (mockup 4). Needs a place id, see [newIntent]. */
class PlaceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaceDetailBinding
    private val galleryAdapter = PhotoAdapter()
    private var deleteDialog: DeleteConfirmDialog? = null

    /** True once the place was shown, so a later "null" means it was deleted. */
    private var hasShownPlace = false

    private val placeId: Long by lazy { intent.getLongExtra(EXTRA_PLACE_ID, NO_ID) }

    private val viewModel: PlaceDetailViewModel by viewModels {
        ViewModelFactories.placeDetail((application as TravelSnapApp).repository, placeId)
    }

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
        binding.btnFavorite.setOnClickListener { viewModel.onToggleFavorite() }
        binding.btnEdit.setOnClickListener {
            startActivity(PlaceFormActivity.newIntent(this, placeId))
        }
        binding.btnDelete.setOnClickListener { viewModel.onDeleteRequested() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { render(it) }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        deleteDialog?.dismiss()
        deleteDialog = null
    }

    private fun render(state: PlaceDetailUiState) {
        val place = state.place
        if (place != null) {
            hasShownPlace = true
            bindPlace(place)
        } else if (!state.isLoading && !hasShownPlace) {
            // The id does not exist.
            UiMessage(MessageType.ERROR, R.string.msg_place_not_found).show(this)
            finish()
            return
        }

        renderDeleteDialog(state, place)

        state.message?.let {
            it.show(this)
            viewModel.onMessageShown()
        }
        if (state.isDeleted) finish()
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

    private fun renderDeleteDialog(state: PlaceDetailUiState, place: Place?) {
        if (!state.showDeleteDialog || place == null) {
            deleteDialog?.dismiss()
            deleteDialog = null
        } else if (deleteDialog == null) {
            deleteDialog = DeleteConfirmDialog(
                context = this,
                placeName = place.name,
                onConfirm = { viewModel.onDeleteConfirmed() },
                onCancel = { viewModel.onDeleteCancelled() }
            ).also { it.show() }
        }
    }

    companion object {
        private const val EXTRA_PLACE_ID = "extra_place_id"
        private const val NO_ID = -1L

        fun newIntent(context: Context, placeId: Long): Intent =
            Intent(context, PlaceDetailActivity::class.java).putExtra(EXTRA_PLACE_ID, placeId)
    }
}
