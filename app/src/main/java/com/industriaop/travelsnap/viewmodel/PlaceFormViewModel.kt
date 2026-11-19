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

enum class PlaceConfirmation { UPDATE, DELETE }

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
    val editingPlace: Place? = null,
    val confirmation: PlaceConfirmation? = null,
    val isBusy: Boolean = false,
    val isFinished: Boolean = false,
    val message: UiMessage? = null
)

/**
 * Handles both "create" (placeId == null) and "edit" (placeId != null).
 */
class PlaceFormViewModel(
    private val repository: PlaceRepository,
    placeId: Long? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaceFormUiState())
    val uiState: StateFlow<PlaceFormUiState> = _uiState.asStateFlow()

    init {
        if (placeId != null) loadPlace(placeId, closeIfMissing = true)
    }

    fun onSearch(id: Long) {
        if (canAct()) loadPlace(id)
    }

    private fun loadPlace(id: Long, closeIfMissing: Boolean = false) {
        _uiState.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            try {
                val place = repository.getPlaceById(id)
                if (place == null) {
                    _uiState.update {
                        it.copy(
                            isFinished = closeIfMissing,
                            message = UiMessage(MessageType.ERROR, R.string.msg_place_not_found)
                        )
                    }
                } else {
                    _uiState.value = PlaceFormUiState(
                        name = place.name,
                        description = place.description,
                        category = place.category,
                        province = place.province,
                        latitude = place.latitude.toString(),
                        longitude = place.longitude.toString(),
                        photoUris = place.photoUris,
                        isFavorite = place.isFavorite,
                        editingPlace = place
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isFinished = closeIfMissing,
                        message = UiMessage(MessageType.ERROR, R.string.msg_generic_error)
                    )
                }
            } finally {
                _uiState.update { it.copy(isBusy = false) }
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

    fun onSaveRequested() {
        if (!canAct()) return

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

        if (state.editingPlace == null) {
            savePlace(state)
        } else {
            _uiState.update { it.copy(confirmation = PlaceConfirmation.UPDATE) }
        }
    }

    fun onUpdateConfirmed() {
        val state = _uiState.value
        if (state.confirmation == PlaceConfirmation.UPDATE) savePlace(state)
    }

    private fun savePlace(state: PlaceFormUiState) {
        val editingPlace = state.editingPlace
        val place = Place(
            id = editingPlace?.id ?: 0L,
            name = state.name.trim(),
            description = state.description.trim(),
            category = state.category!!,
            province = state.province!!,
            latitude = state.latitude.trim().toDouble(),
            longitude = state.longitude.trim().toDouble(),
            photoUris = state.photoUris,
            isFavorite = state.isFavorite,
            createdAt = editingPlace?.createdAt ?: System.currentTimeMillis()
        )

        _uiState.update { it.copy(confirmation = null, isBusy = true) }
        viewModelScope.launch {
            try {
                val successRes = if (editingPlace == null) {
                    repository.addPlace(place)
                    R.string.msg_place_created
                } else {
                    repository.updatePlace(place)
                    R.string.msg_place_updated
                }
                _uiState.update {
                    it.copy(isFinished = true, message = UiMessage(MessageType.SUCCESS, successRes))
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(message = UiMessage(MessageType.ERROR, R.string.msg_generic_error))
                }
            } finally {
                _uiState.update { it.copy(isBusy = false) }
            }
        }
    }

    fun onDeleteRequested() {
        if (canAct() && _uiState.value.editingPlace != null) {
            _uiState.update { it.copy(confirmation = PlaceConfirmation.DELETE) }
        }
    }

    fun onConfirmationCancelled() = _uiState.update { it.copy(confirmation = null) }

    fun onDeleteConfirmed() {
        val state = _uiState.value
        val place = state.editingPlace ?: return
        if (state.confirmation != PlaceConfirmation.DELETE) return

        _uiState.update { it.copy(confirmation = null, isBusy = true) }
        viewModelScope.launch {
            try {
                repository.deletePlace(place.id)
                _uiState.update {
                    it.copy(
                        isFinished = true,
                        message = UiMessage(MessageType.SUCCESS, R.string.msg_place_deleted)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(message = UiMessage(MessageType.ERROR, R.string.msg_generic_error))
                }
            } finally {
                _uiState.update { it.copy(isBusy = false) }
            }
        }
    }

    private fun canAct(): Boolean {
        val state = _uiState.value
        return !state.isBusy && !state.isFinished && state.confirmation == null
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
