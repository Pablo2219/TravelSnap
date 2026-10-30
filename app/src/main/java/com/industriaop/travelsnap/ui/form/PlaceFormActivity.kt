package com.industriaop.travelsnap.ui.form

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.textfield.TextInputEditText
import com.industriaop.travelsnap.R
import com.industriaop.travelsnap.TravelSnapApp
import com.industriaop.travelsnap.databinding.ActivityPlaceFormBinding
import com.industriaop.travelsnap.domain.model.PlaceCategory
import com.industriaop.travelsnap.domain.model.Province
import com.industriaop.travelsnap.ui.adapter.PhotoAdapter
import com.industriaop.travelsnap.ui.common.applySystemBarsPadding
import com.industriaop.travelsnap.ui.common.show
import com.industriaop.travelsnap.ui.dialog.AddPhotoBottomSheet
import com.industriaop.travelsnap.util.MessageType
import com.industriaop.travelsnap.util.UiMessage
import com.industriaop.travelsnap.util.labelRes
import com.industriaop.travelsnap.viewmodel.FormField
import com.industriaop.travelsnap.viewmodel.PlaceFormUiState
import com.industriaop.travelsnap.viewmodel.PlaceFormViewModel
import com.industriaop.travelsnap.viewmodel.ViewModelFactories
import kotlinx.coroutines.launch
import java.io.File

/** Create / edit place screen (mockup 3). Pass a place id to edit, nothing to create. */
class PlaceFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaceFormBinding
    private lateinit var photoAdapter: PhotoAdapter
    private var addPhotoSheet: AddPhotoBottomSheet? = null

    private val placeId: Long? by lazy {
        intent.getLongExtra(EXTRA_PLACE_ID, NO_ID).takeIf { it != NO_ID }
    }

    private val viewModel: PlaceFormViewModel by viewModels {
        ViewModelFactories.placeForm((application as TravelSnapApp).repository, placeId)
    }

    private val categoryOptions = PlaceCategory.entries
    private val provinceOptions = Province.entries

    /** Uri of the file the camera app is writing to. */
    private var cameraUri: Uri? = null

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        val uri = cameraUri
        if (saved && uri != null) viewModel.onPhotoAdded(uri.toString())
    }

    private val pickPhoto = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onPhotoAdded(uri.toString())
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
        binding.btnSave.setOnClickListener { viewModel.onSave() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { render(it) }
            }
        }
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

    // ------------------------------------------------------------------ Setup

    private fun setupPhotos() {
        photoAdapter = PhotoAdapter(onRemove = { viewModel.onPhotoRemoved(it) })
        binding.rvPhotos.adapter = photoAdapter
        binding.btnAddPhoto.setOnClickListener {
            addPhotoSheet = AddPhotoBottomSheet(
                context = this,
                onTakePhoto = ::launchCamera,
                onPickFromGallery = {
                    pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            ).also { it.show() }
        }
    }

    private fun setupFields() {
        binding.etName.doOnTextChanged { text, _, _, _ ->
            viewModel.onNameChange(text?.toString().orEmpty())
        }
        binding.etDescription.doOnTextChanged { text, _, _, _ ->
            viewModel.onDescriptionChange(text?.toString().orEmpty())
        }
        binding.etLatitude.doOnTextChanged { text, _, _, _ ->
            viewModel.onLatitudeChange(text?.toString().orEmpty())
        }
        binding.etLongitude.doOnTextChanged { text, _, _, _ ->
            viewModel.onLongitudeChange(text?.toString().orEmpty())
        }

        binding.acCategory.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categoryOptions.map { getString(it.labelRes()) }
            )
        )
        binding.acCategory.setOnItemClickListener { _, _, position, _ ->
            viewModel.onCategoryChange(categoryOptions[position])
        }

        binding.acProvince.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                provinceOptions.map { getString(it.labelRes()) }
            )
        )
        binding.acProvince.setOnItemClickListener { _, _, position, _ ->
            viewModel.onProvinceChange(provinceOptions[position])
        }

        binding.switchFavorite.setOnCheckedChangeListener { _, checked ->
            viewModel.onFavoriteChange(checked)
        }
    }

    // ----------------------------------------------------------------- Camera

    private fun launchCamera() {
        val photosDir = File(cacheDir, "photos").apply { mkdirs() }
        val file = File.createTempFile("travelsnap_", ".jpg", photosDir)
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        cameraUri = uri
        try {
            takePicture.launch(uri)
        } catch (e: ActivityNotFoundException) {
            UiMessage(MessageType.ERROR, R.string.msg_camera_unavailable).show(this)
        }
    }

    // ----------------------------------------------------------------- Render

    private fun render(state: PlaceFormUiState) {
        binding.toolbar.setTitle(if (state.isEditing) R.string.place_edit else R.string.place_new)

        // Fill the fields only when the text is different (e.g. when the place finishes loading).
        binding.etName.setTextIfDifferent(state.name)
        binding.etDescription.setTextIfDifferent(state.description)
        binding.etLatitude.setTextIfDifferent(state.latitude)
        binding.etLongitude.setTextIfDifferent(state.longitude)
        binding.acCategory.setText(state.category?.let { getString(it.labelRes()) }.orEmpty(), false)
        binding.acProvince.setText(state.province?.let { getString(it.labelRes()) }.orEmpty(), false)
        if (binding.switchFavorite.isChecked != state.isFavorite) {
            binding.switchFavorite.isChecked = state.isFavorite
        }

        binding.tilName.error = errorText(state, FormField.NAME)
        binding.tilDescription.error = errorText(state, FormField.DESCRIPTION)
        binding.tilCategory.error = errorText(state, FormField.CATEGORY)
        binding.tilProvince.error = errorText(state, FormField.PROVINCE)
        binding.tilLatitude.error = errorText(state, FormField.LATITUDE)
        binding.tilLongitude.error = errorText(state, FormField.LONGITUDE)

        photoAdapter.submitList(state.photoUris)
        binding.rvPhotos.isVisible = state.photoUris.isNotEmpty()
        binding.tvNoPhotos.isVisible = state.photoUris.isEmpty()

        state.message?.let {
            it.show(this)
            viewModel.onMessageShown()
        }
        if (state.isSaved) finish()
    }

    private fun errorText(state: PlaceFormUiState, field: FormField): String? =
        state.errors[field]?.let { getString(it.messageRes) }

    private fun TextInputEditText.setTextIfDifferent(value: String) {
        if (text?.toString() != value) {
            setText(value)
            setSelection(value.length)
        }
    }

    companion object {
        private const val EXTRA_PLACE_ID = "extra_place_id"
        private const val NO_ID = -1L
        private const val KEY_CAMERA_URI = "key_camera_uri"

        /** Intent to create a place (no id) or edit an existing one. */
        fun newIntent(context: Context, placeId: Long? = null): Intent =
            Intent(context, PlaceFormActivity::class.java).apply {
                if (placeId != null) putExtra(EXTRA_PLACE_ID, placeId)
            }
    }
}
