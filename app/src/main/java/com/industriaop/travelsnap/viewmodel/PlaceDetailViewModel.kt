package com.industriaop.travelsnap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.industriaop.travelsnap.domain.model.Place
import com.industriaop.travelsnap.domain.repository.PlaceRepository
import com.industriaop.travelsnap.util.MessageType
import com.industriaop.travelsnap.util.UiMessage
import com.industriaop.travelsnap.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlaceDetailUiState(
    val place: Place? = null,
    val isLoading: Boolean = true,
    val showDeleteDialog: Boolean = false,
    val isDeleted: Boolean = false,
    val message: UiMessage? = null
)

class PlaceDetailViewModel(
    private val repository: PlaceRepository,
    private val placeId: Long
) : ViewModel() {

    private data class Local(
        val showDeleteDialog: Boolean = false,
        val isDeleted: Boolean = false,
        val message: UiMessage? = null
    )

    private val local = MutableStateFlow(Local())

    val uiState: StateFlow<PlaceDetailUiState> = combine(
        repository.observePlace(placeId), local
    ) { place, local ->
        PlaceDetailUiState(
            place = place,
            isLoading = false,
            showDeleteDialog = local.showDeleteDialog,
            isDeleted = local.isDeleted,
            message = local.message
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlaceDetailUiState())

    fun onToggleFavorite() {
        viewModelScope.launch { repository.toggleFavorite(placeId) }
    }

    fun onDeleteRequested() = local.update { it.copy(showDeleteDialog = true) }

    fun onDeleteCancelled() = local.update { it.copy(showDeleteDialog = false) }

    fun onDeleteConfirmed() {
        viewModelScope.launch {
            try {
                repository.deletePlace(placeId)
                local.update {
                    it.copy(
                        showDeleteDialog = false,
                        isDeleted = true,
                        message = UiMessage(MessageType.SUCCESS, R.string.msg_place_deleted)
                    )
                }
            } catch (e: Exception) {
                local.update {
                    it.copy(
                        showDeleteDialog = false,
                        message = UiMessage(MessageType.ERROR, R.string.msg_generic_error)
                    )
                }
            }
        }
    }

    fun onMessageShown() = local.update { it.copy(message = null) }
}