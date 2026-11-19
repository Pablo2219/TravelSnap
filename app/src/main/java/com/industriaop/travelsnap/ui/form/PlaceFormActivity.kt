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
import androidx.appcompat.app.AlertDialog
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
import com.industriaop.travelsnap.ui.dialog.DeleteConfirmDialog
import com.industriaop.travelsnap.util.MessageType
import com.industriaop.travelsnap.util.UiMessage
import com.industriaop.travelsnap.util.labelRes
import com.industriaop.travelsnap.viewmodel.FormField
import com.industriaop.travelsnap.viewmodel.PlaceConfirmation
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
    private var updateDialog: AlertDialog? = null
    private var deleteDialog: DeleteConfirmDialog? = null

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
        binding.btnSearchPlace.setOnClickListener { searchPlace() }
        binding.etSearchPlaceId.doOnTextChanged { _, _, _, _ -> binding.tilSearchPlaceId.error = null }
        binding.btnSave.setOnClickListener { viewModel.onSaveRequested() }
        binding.btnDeletePlace.setOnClickListener { viewModel.onDeleteRequested() }

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
        dismissConfirmationDialogs()
    }

    private fun searchPlace() {
        val id = binding.etSearchPlaceId.text.toString().trim().toLongOrNull()
        if (id == null || id <= 0L) {
            binding.tilSearchPlaceId.error = getString(R.string.error_place_id_invalid)
            return
        }
        viewModel.onSearch(id)
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
        val place = state.editingPlace
        binding.toolbar.setTitle(if (place == null) R.string.place_new else R.string.place_edit)
        binding.tvEditingPlaceId.isVisible = place != null
        if (place != null) {
            binding.tvEditingPlaceId.text = getString(R.string.place_id_label, place.id)
        }
        binding.btnSearchPlace.isEnabled = !state.isBusy
        binding.btnSave.isEnabled = !state.isBusy
        binding.btnDeletePlace.isVisible = place != null
        binding.btnDeletePlace.isEnabled = !state.isBusy

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

        renderConfirmation(state)
        state.message?.let {
            it.show(this)
            viewModel.onMessageShown()
        }
        if (state.isFinished) finish()
    }

    private fun renderConfirmation(state: PlaceFormUiState) {
        val place = state.editingPlace ?: return
        when (state.confirmation) {
            PlaceConfirmation.UPDATE -> if (updateDialog == null) {
                updateDialog = AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_update_title)
                    .setMessage(getString(R.string.dialog_update_message, place.name))
                    .setPositiveButton(R.string.button_save) { _, _ -> viewModel.onUpdateConfirmed() }
                    .setNegativeButton(R.string.button_cancel) { _, _ -> viewModel.onConfirmationCancelled() }
                    .setOnCancelListener { viewModel.onConfirmationCancelled() }
                    .show()
            }
            PlaceConfirmation.DELETE -> if (deleteDialog == null) {
                deleteDialog = DeleteConfirmDialog(
                    context = this,
                    placeName = place.name,
                    onConfirm = { viewModel.onDeleteConfirmed() },
                    onCancel = { viewModel.onConfirmationCancelled() }
                ).also { it.show() }
            }
            null -> dismissConfirmationDialogs()
        }
    }

    private fun dismissConfirmationDialogs() {
        updateDialog?.dismiss()
        updateDialog = null
        deleteDialog?.dismiss()
        deleteDialog = null
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
