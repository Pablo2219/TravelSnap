package com.industriaop.travelsnap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.industriaop.travelsnap.domain.model.Place
import com.industriaop.travelsnap.domain.model.PlaceCategory
import com.industriaop.travelsnap.domain.model.Province
import com.industriaop.travelsnap.domain.repository.PlaceRepository
import com.industriaop.travelsnap.util.Constants
import com.industriaop.travelsnap.util.MessageType
import com.industriaop.travelsnap.util.UiMessage
import com.industriaop.travelsnap.R
import com.industriaop.travelsnap.util.ValidationError
import com.industriaop.travelsnap.util.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FormField { NAME, DESCRIPTION, CATEGORY, PROVINCE, LATITUDE, LONGITUDE }

data class PlaceFormUiState(
    val name: String = "",
    val description: String = "",
    val category: PlaceCategory? = null,
    val province: Province? = null,
    val latitude: String = "",
    val longitude: String = "",
    val photoUris: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val errors: Map<FormField, ValidationError> = emptyMap(),
    val isEditing: Boolean = false,
    val isSaved: Boolean = false,
    val message: UiMessage? = null
)

/**
 * Handles both "create" (placeId == null) and "edit" (placeId != null).
 */
class PlaceFormViewModel(
    private val repository: PlaceRepository,
    private val placeId: Long? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaceFormUiState(isEditing = placeId != null))
    val uiState: StateFlow<PlaceFormUiState> = _uiState.asStateFlow()

    private var createdAt: Long = System.currentTimeMillis()

    init {
        if (placeId != null) loadPlace(placeId)
    }

    private fun loadPlace(id: Long) {
        viewModelScope.launch {
            val place = repository.getPlaceById(id)
            if (place == null) {
                _uiState.update {
                    it.copy(message = UiMessage(MessageType.ERROR, R.string.msg_place_not_found))
                }
                return@launch
            }
            createdAt = place.createdAt
            _uiState.update {
                it.copy(
                    name = place.name,
                    description = place.description,
                    category = place.category,
                    province = place.province,
                    latitude = place.latitude.toString(),
                    longitude = place.longitude.toString(),
                    photoUris = place.photoUris,
                    isFavorite = place.isFavorite
                )
            }
        }
    }

    // ------------------------------------------------------------ Field input

    fun onNameChange(value: String) = updateField(FormField.NAME) { it.copy(name = value) }

    fun onDescriptionChange(value: String) =
        updateField(FormField.DESCRIPTION) { it.copy(description = value) }

    fun onCategoryChange(value: PlaceCategory) =
        updateField(FormField.CATEGORY) { it.copy(category = value) }

    fun onProvinceChange(value: Province) =
        updateField(FormField.PROVINCE) { it.copy(province = value) }

    fun onLatitudeChange(value: String) =
        updateField(FormField.LATITUDE) { it.copy(latitude = value) }

    fun onLongitudeChange(value: String) =
        updateField(FormField.LONGITUDE) { it.copy(longitude = value) }

    fun onFavoriteChange(value: Boolean) = _uiState.update { it.copy(isFavorite = value) }

    private fun updateField(field: FormField, change: (PlaceFormUiState) -> PlaceFormUiState) {
        _uiState.update { change(it).copy(errors = it.errors - field) }
    }

    // ----------------------------------------------------------------- Photos

    /** Called with the URI returned by the Photo Picker or the camera. */
    fun onPhotoAdded(uri: String) {
        _uiState.update { state ->
            when {
                uri in state.photoUris -> state
                state.photoUris.size >= Constants.MAX_PHOTOS_PER_PLACE -> state.copy(
                    message = UiMessage(
                        MessageType.ERROR,
                        R.string.msg_max_photos_reached,
                        listOf(Constants.MAX_PHOTOS_PER_PLACE)
                    )
                )
                else -> state.copy(photoUris = state.photoUris + uri)
            }
        }
    }

    fun onPhotoRemoved(uri: String) =
        _uiState.update { it.copy(photoUris = it.photoUris - uri) }

    fun onCameraPermissionDenied() = _uiState.update {
        it.copy(message = UiMessage(MessageType.ERROR, R.string.msg_camera_permission_denied))
    }

    // ------------------------------------------------------------------- Save

    fun onSave() {
        val state = _uiState.value
        val errors = validate(state)
        if (errors.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    errors = errors,
                    message = UiMessage(MessageType.ERROR, R.string.msg_fix_form_errors)
                )
            }
            return
        }

        val place = Place(
            id = placeId ?: 0L,
            name = state.name.trim(),
            description = state.description.trim(),
            category = state.category!!,
            province = state.province!!,
            latitude = state.latitude.trim().toDouble(),
            longitude = state.longitude.trim().toDouble(),
            photoUris = state.photoUris,
            isFavorite = state.isFavorite,
            createdAt = createdAt
        )

        viewModelScope.launch {
            try {
                if (placeId == null) repository.addPlace(place) else repository.updatePlace(place)
                val successRes =
                    if (placeId == null) R.string.msg_place_created else R.string.msg_place_updated
                _uiState.update {
                    it.copy(isSaved = true, message = UiMessage(MessageType.SUCCESS, successRes))
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(message = UiMessage(MessageType.ERROR, R.string.msg_generic_error))
                }
            }
        }
    }

    private fun validate(state: PlaceFormUiState): Map<FormField, ValidationError> = buildMap {
        Validators.validateName(state.name)?.let { put(FormField.NAME, it) }
        Validators.validateDescription(state.description)?.let { put(FormField.DESCRIPTION, it) }
        Validators.validateCategory(state.category)?.let { put(FormField.CATEGORY, it) }
        Validators.validateProvince(state.province)?.let { put(FormField.PROVINCE, it) }
        Validators.validateLatitude(state.latitude)?.let { put(FormField.LATITUDE, it) }
        Validators.validateLongitude(state.longitude)?.let { put(FormField.LONGITUDE, it) }
    }

    fun onMessageShown() = _uiState.update { it.copy(message = null) }
}