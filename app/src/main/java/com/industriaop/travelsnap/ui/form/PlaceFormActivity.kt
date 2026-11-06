package com.industriaop.travelsnap.ui.form

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.industriaop.travelsnap.R
import com.industriaop.travelsnap.TravelSnapApp
import com.industriaop.travelsnap.databinding.ActivityPlaceFormBinding
import com.industriaop.travelsnap.domain.model.Place
import com.industriaop.travelsnap.domain.model.PlaceCategory
import com.industriaop.travelsnap.domain.model.Province
import com.industriaop.travelsnap.ui.adapter.PhotoAdapter
import com.industriaop.travelsnap.ui.common.applySystemBarsPadding
import com.industriaop.travelsnap.ui.dialog.AddPhotoBottomSheet
import com.industriaop.travelsnap.util.Constants
import com.industriaop.travelsnap.util.Validators
import com.industriaop.travelsnap.util.labelRes
import java.io.File

/** Creates or edits a place using the repository directly. */
class PlaceFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaceFormBinding
    private lateinit var photoAdapter: PhotoAdapter
    private var addPhotoSheet: AddPhotoBottomSheet? = null
    private val repository get() = (application as TravelSnapApp).repository

    private val placeId: Long? by lazy {
        intent.getLongExtra(EXTRA_PLACE_ID, NO_ID).takeIf { it != NO_ID }
    }

    private val categoryOptions = PlaceCategory.entries
    private val provinceOptions = Province.entries
    private val photoUris = mutableListOf<String>()
    private var originalPlace: Place? = null
    private var cameraUri: Uri? = null

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
            val uri = cameraUri
            if (saved && uri != null) addPhoto(uri.toString())
        }

    private val pickPhoto =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) addPhoto(uri.toString())
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPlaceFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()

        savedInstanceState?.getString(KEY_CAMERA_URI)?.let { cameraUri = Uri.parse(it) }
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupPhotos()
        setupFields()
        loadPlaceForEditing()
        binding.btnSave.setOnClickListener { savePlace() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        cameraUri?.let { outState.putString(KEY_CAMERA_URI, it.toString()) }
    }

    override fun onStop() {
        super.onStop()
        addPhotoSheet?.dismiss()
        addPhotoSheet = null
    }

    private fun setupPhotos() {
        photoAdapter = PhotoAdapter(onRemove = { uri ->
            photoUris.remove(uri)
            showPhotos()
        })
        binding.rvPhotos.adapter = photoAdapter
        binding.btnAddPhoto.setOnClickListener {
            addPhotoSheet = AddPhotoBottomSheet(
                context = this,
                onTakePhoto = ::launchCamera,
                onPickFromGallery = {
                    pickPhoto.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            ).also { it.show() }
        }
    }

    private fun setupFields() {
        binding.acCategory.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categoryOptions.map { getString(it.labelRes()) }
            )
        )
        binding.acProvince.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                provinceOptions.map { getString(it.labelRes()) }
            )
        )
    }

    private fun loadPlaceForEditing() {
        val id = placeId
        if (id == null) {
            binding.toolbar.setTitle(R.string.place_new)
            return
        }

        val place = repository.getPlaceById(id)
        if (place == null) {
            Toast.makeText(this, R.string.msg_place_not_found, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        originalPlace = place
        binding.toolbar.setTitle(R.string.place_edit)
        binding.etName.setText(place.name)
        binding.etDescription.setText(place.description)
        binding.acCategory.setText(getString(place.category.labelRes()), false)
        binding.acProvince.setText(getString(place.province.labelRes()), false)
        binding.etLatitude.setText(place.latitude.toString())
        binding.etLongitude.setText(place.longitude.toString())
        binding.switchFavorite.isChecked = place.isFavorite
        photoUris.addAll(place.photoUris)
        showPhotos()
    }

    private fun addPhoto(uri: String) {
        if (uri in photoUris) return
        if (photoUris.size >= Constants.MAX_PHOTOS_PER_PLACE) {
            Toast.makeText(
                this,
                getString(R.string.msg_max_photos_reached, Constants.MAX_PHOTOS_PER_PLACE),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        photoUris.add(uri)
        showPhotos()
    }

    private fun showPhotos() {
        photoAdapter.submitList(photoUris.toList())
        binding.rvPhotos.isVisible = photoUris.isNotEmpty()
        binding.tvNoPhotos.isVisible = photoUris.isEmpty()
    }

    private fun launchCamera() {
        val photosDir = File(cacheDir, "photos").apply { mkdirs() }
        val file = File.createTempFile("travelsnap_", ".jpg", photosDir)
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        cameraUri = uri
        try {
            takePicture.launch(uri)
        } catch (exception: ActivityNotFoundException) {
            Toast.makeText(this, R.string.msg_camera_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    private fun savePlace() {
        clearErrors()

        val name = binding.etName.text?.toString().orEmpty().trim()
        val description = binding.etDescription.text?.toString().orEmpty().trim()
        val categoryIndex = categoryOptions.indexOfFirst {
            getString(it.labelRes()) == binding.acCategory.text?.toString()
        }
        val provinceIndex = provinceOptions.indexOfFirst {
            getString(it.labelRes()) == binding.acProvince.text?.toString()
        }
        val category = categoryOptions.getOrNull(categoryIndex)
        val province = provinceOptions.getOrNull(provinceIndex)
        val latitudeText = binding.etLatitude.text?.toString().orEmpty()
        val longitudeText = binding.etLongitude.text?.toString()

        binding.tilName.error = Validators.validateName(name)?.let { getString(it.messageRes) }
        binding.tilDescription.error =
            Validators.validateDescription(description)?.let { getString(it.messageRes) }
        binding.tilCategory.error =
            Validators.validateCategory(category)?.let { getString(it.messageRes) }
        binding.tilProvince.error =
            Validators.validateProvince(province)?.let { getString(it.messageRes) }
        binding.tilLatitude.error =
            Validators.validateLatitude(latitudeText)?.let { getString(it.messageRes) }
        binding.tilLongitude.error =
            Validators.validateLongitude(longitudeText)?.let { getString(it.messageRes) }

        if (hasErrors()) {
            Toast.makeText(this, R.string.msg_fix_form_errors, Toast.LENGTH_SHORT).show()
            return
        }

        val previous = originalPlace
        val place = Place(
            id = previous?.id ?: 0L,
            name = name,
            description = description,
            category = category!!,
            province = province!!,
            latitude = latitudeText.toDouble(),
            longitude = longitudeText.toDouble(),
            photoUris = photoUris.toList(),
            isFavorite = binding.switchFavorite.isChecked,
            createdAt = previous?.createdAt ?: System.currentTimeMillis()
        )

        if (previous == null) {
            repository.addPlace(place)
            Toast.makeText(this, R.string.msg_place_created, Toast.LENGTH_SHORT).show()
            finish()
        } else {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_update_title)
                .setMessage(getString(R.string.dialog_update_message, place.name))
                .setNegativeButton(R.string.button_cancel, null)
                .setPositiveButton(R.string.dialog_confirm) { _, _ ->
                    repository.updatePlace(place)
                    Toast.makeText(this, R.string.msg_place_updated, Toast.LENGTH_SHORT).show()
                    finish()
                }
                .show()
        }
    }

    private fun clearErrors() {
        binding.tilName.error = null
        binding.tilDescription.error = null
        binding.tilCategory.error = null
        binding.tilProvince.error = null
        binding.tilLatitude.error = null
        binding.tilLongitude.error = null
    }

    private fun hasErrors(): Boolean =
        binding.tilName.error != null ||
            binding.tilDescription.error != null ||
            binding.tilCategory.error != null ||
            binding.tilProvince.error != null ||
            binding.tilLatitude.error != null ||
            binding.tilLongitude.error != null

    companion object {
        private const val EXTRA_PLACE_ID = "extra_place_id"
        private const val NO_ID = -1L
        private const val KEY_CAMERA_URI = "key_camera_uri"

        fun newIntent(context: Context, placeId: Long? = null): Intent =
            Intent(context, PlaceFormActivity::class.java).apply {
                if (placeId != null) putExtra(EXTRA_PLACE_ID, placeId)
            }
    }
}
