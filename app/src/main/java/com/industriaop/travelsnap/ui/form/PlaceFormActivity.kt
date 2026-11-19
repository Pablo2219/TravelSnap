package com.industriaop.travelsnap.ui.form

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.industriaop.travelsnap.domain.model.Place
import com.industriaop.travelsnap.domain.model.PlaceCategory
import com.industriaop.travelsnap.domain.model.Province
import com.industriaop.travelsnap.R
import com.industriaop.travelsnap.ui.adapter.PhotoAdapter
import com.industriaop.travelsnap.ui.common.applySystemBarsPadding
import com.industriaop.travelsnap.util.Constants
import com.industriaop.travelsnap.util.labelRes
import com.industriaop.travelsnap.util.showConfirmation
import com.industriaop.travelsnap.util.Validators
import com.industriaop.travelsnap.viewmodel.PlaceViewModel
import java.io.File

/** Opens a blank form or loads an existing place by ID. */
class PlaceFormActivity : AppCompatActivity() {

    private lateinit var acCategory: MaterialAutoCompleteTextView
    private lateinit var acProvince: MaterialAutoCompleteTextView
    private lateinit var btnAddPhoto: MaterialButton
    private lateinit var btnDeletePlace: MaterialButton
    private lateinit var btnSave: MaterialButton
    private lateinit var btnSearchPlace: AppCompatImageButton
    private lateinit var etDescription: TextInputEditText
    private lateinit var etLatitude: TextInputEditText
    private lateinit var etLongitude: TextInputEditText
    private lateinit var etName: TextInputEditText
    private lateinit var etSearchPlaceId: TextInputEditText
    private lateinit var rvPhotos: RecyclerView
    private lateinit var switchFavorite: MaterialSwitch
    private lateinit var tilCategory: TextInputLayout
    private lateinit var tilDescription: TextInputLayout
    private lateinit var tilLatitude: TextInputLayout
    private lateinit var tilLongitude: TextInputLayout
    private lateinit var tilName: TextInputLayout
    private lateinit var tilProvince: TextInputLayout
    private lateinit var tilSearchPlaceId: TextInputLayout
    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvEditingPlaceId: TextView
    private lateinit var tvNoPhotos: TextView
    private lateinit var photoAdapter: PhotoAdapter
    private val viewModel: PlaceViewModel by viewModels()

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
        setContentView(R.layout.activity_place_form)
        findViewById<View>(R.id.root).applySystemBarsPadding()

        acCategory = findViewById(R.id.acCategory)
        acProvince = findViewById(R.id.acProvince)
        btnAddPhoto = findViewById(R.id.btnAddPhoto)
        btnDeletePlace = findViewById(R.id.btnDeletePlace)
        btnSave = findViewById(R.id.btnSave)
        btnSearchPlace = findViewById(R.id.btnSearchPlace)
        etDescription = findViewById(R.id.etDescription)
        etLatitude = findViewById(R.id.etLatitude)
        etLongitude = findViewById(R.id.etLongitude)
        etName = findViewById(R.id.etName)
        etSearchPlaceId = findViewById(R.id.etSearchPlaceId)
        rvPhotos = findViewById(R.id.rvPhotos)
        switchFavorite = findViewById(R.id.switchFavorite)
        tilCategory = findViewById(R.id.tilCategory)
        tilDescription = findViewById(R.id.tilDescription)
        tilLatitude = findViewById(R.id.tilLatitude)
        tilLongitude = findViewById(R.id.tilLongitude)
        tilName = findViewById(R.id.tilName)
        tilProvince = findViewById(R.id.tilProvince)
        tilSearchPlaceId = findViewById(R.id.tilSearchPlaceId)
        toolbar = findViewById(R.id.toolbar)
        tvEditingPlaceId = findViewById(R.id.tvEditingPlaceId)
        tvNoPhotos = findViewById(R.id.tvNoPhotos)

        savedInstanceState?.getString(KEY_CAMERA_URI)?.let { cameraUri = Uri.parse(it) }
        toolbar.setNavigationOnClickListener { finish() }

        setupPhotos()
        setupFields()
        val placeId = savedInstanceState?.getLong(EXTRA_PLACE_ID, NO_ID)
            ?: intent.getLongExtra(EXTRA_PLACE_ID, NO_ID)
        if (placeId != NO_ID && !loadPlace(placeId)) {
            finish()
            return
        }
        val savedPhotos = savedInstanceState?.getStringArrayList(KEY_PHOTOS)
        if (savedPhotos != null) {
            photoUris.clear()
            photoUris.addAll(savedPhotos)
            showPhotos()
        }
        showMode()
        btnSearchPlace.setOnClickListener { searchPlace() }
        btnDeletePlace.setOnClickListener { confirmDelete() }
        btnSave.setOnClickListener { savePlace() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        cameraUri?.let { outState.putString(KEY_CAMERA_URI, it.toString()) }
        outState.putLong(EXTRA_PLACE_ID, originalPlace?.id ?: NO_ID)
        outState.putStringArrayList(KEY_PHOTOS, ArrayList(photoUris))
    }

    private fun setupPhotos() {
        photoAdapter = PhotoAdapter(onRemove = { uri ->
            photoUris.remove(uri)
            showPhotos()
        })
        rvPhotos.adapter = photoAdapter
        btnAddPhoto.setOnClickListener {
            val options = arrayOf(getString(R.string.photo_take), getString(R.string.photo_pick_gallery))
            AlertDialog.Builder(this)
                .setTitle(R.string.photo_add)
                .setItems(options) { _, option ->
                    if (option == 0) {
                        launchCamera()
                    } else {
                        pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                }
                .setNegativeButton(R.string.button_cancel, null)
                .show()
        }
    }

    private fun setupFields() {
        acCategory.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categoryOptions.map { getString(it.labelRes()) }
            )
        )
        acProvince.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                provinceOptions.map { getString(it.labelRes()) }
            )
        )
    }

    private fun searchPlace() {
        val id = etSearchPlaceId.text.toString().trim().toLongOrNull()
        if (id == null || id <= 0L) {
            tilSearchPlaceId.error = getString(R.string.error_place_id_invalid)
            return
        }
        tilSearchPlaceId.error = null
        loadPlace(id)
    }

    private fun loadPlace(id: Long): Boolean {
        val place = viewModel.getPlaceById(id)
        if (place == null) {
            Toast.makeText(this, R.string.msg_place_not_found, Toast.LENGTH_SHORT).show()
            return false
        }

        originalPlace = place
        etName.setText(place.name)
        etDescription.setText(place.description)
        acCategory.setText(getString(place.category.labelRes()), false)
        acProvince.setText(getString(place.province.labelRes()), false)
        etLatitude.setText(place.latitude.toString())
        etLongitude.setText(place.longitude.toString())
        switchFavorite.isChecked = place.isFavorite
        photoUris.clear()
        photoUris.addAll(place.photoUris)
        clearErrors()
        showMode()
        showPhotos()
        return true
    }

    private fun showMode() {
        val place = originalPlace
        toolbar.setTitle(if (place == null) R.string.place_new else R.string.place_edit)
        tvEditingPlaceId.isVisible = place != null
        btnDeletePlace.isVisible = place != null
        if (place != null) {
            tvEditingPlaceId.text = getString(R.string.place_id_label, place.id)
        }
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
        photoAdapter.updatePhotos(photoUris.toList())
        rvPhotos.isVisible = photoUris.isNotEmpty()
        tvNoPhotos.isVisible = photoUris.isEmpty()
    }

    private fun launchCamera() {
        try {
            val photosDir = File(cacheDir, "photos").apply { mkdirs() }
            val file = File.createTempFile("travelsnap_", ".jpg", photosDir)
            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
            cameraUri = uri
            takePicture.launch(uri)
        } catch (exception: Exception) {
            Toast.makeText(this, R.string.msg_camera_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    private fun savePlace() {
        clearErrors()

        val name = etName.text?.toString().orEmpty().trim()
        val description = etDescription.text?.toString().orEmpty().trim()
        val categoryIndex = categoryOptions.indexOfFirst {
            getString(it.labelRes()) == acCategory.text?.toString()
        }
        val provinceIndex = provinceOptions.indexOfFirst {
            getString(it.labelRes()) == acProvince.text?.toString()
        }
        val category = categoryOptions.getOrNull(categoryIndex)
        val province = provinceOptions.getOrNull(provinceIndex)
        val latitudeText = etLatitude.text.toString().trim()
        val longitudeText = etLongitude.text.toString().trim()

        tilName.error = Validators.validateName(name)?.let { getString(it.messageRes) }
        tilDescription.error =
            Validators.validateDescription(description)?.let { getString(it.messageRes) }
        tilCategory.error =
            Validators.validateCategory(category)?.let { getString(it.messageRes) }
        tilProvince.error =
            Validators.validateProvince(province)?.let { getString(it.messageRes) }
        tilLatitude.error =
            Validators.validateLatitude(latitudeText)?.let { getString(it.messageRes) }
        tilLongitude.error =
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
            isFavorite = switchFavorite.isChecked,
            createdAt = previous?.createdAt ?: System.currentTimeMillis()
        )

        if (previous == null) {
            viewModel.addPlace(place)
            Toast.makeText(this, R.string.msg_place_created, Toast.LENGTH_SHORT).show()
            finish()
        } else {
            showConfirmation(
                this, R.string.dialog_update_title,
                getString(R.string.dialog_update_message, previous.name), R.string.button_save
            ) {
                viewModel.updatePlace(place)
                Toast.makeText(this, R.string.msg_place_updated, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun confirmDelete() {
        val place = originalPlace ?: return
        showConfirmation(
            this, R.string.dialog_delete_title,
            getString(R.string.dialog_delete_message, place.name), R.string.button_delete
        ) {
            viewModel.deletePlace(place.id)
            Toast.makeText(this, R.string.msg_place_deleted, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun clearErrors() {
        tilName.error = null
        tilDescription.error = null
        tilCategory.error = null
        tilProvince.error = null
        tilLatitude.error = null
        tilLongitude.error = null
    }

    private fun hasErrors(): Boolean =
        tilName.error != null ||
            tilDescription.error != null ||
            tilCategory.error != null ||
            tilProvince.error != null ||
            tilLatitude.error != null ||
            tilLongitude.error != null

    companion object {
        private const val EXTRA_PLACE_ID = "extra_place_id"
        private const val NO_ID = -1L
        private const val KEY_CAMERA_URI = "key_camera_uri"
        private const val KEY_PHOTOS = "key_photos"

        fun newIntent(context: Context, placeId: Long? = null): Intent =
            Intent(context, PlaceFormActivity::class.java).apply {
                if (placeId != null) putExtra(EXTRA_PLACE_ID, placeId)
            }
    }
}
