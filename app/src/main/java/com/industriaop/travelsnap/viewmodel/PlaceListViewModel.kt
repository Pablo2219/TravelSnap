package com.industriaop.travelsnap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.industriaop.travelsnap.domain.model.Place
import com.industriaop.travelsnap.domain.model.PlaceCategory
import com.industriaop.travelsnap.domain.model.PlaceFilter
import com.industriaop.travelsnap.domain.model.Province
import com.industriaop.travelsnap.domain.model.SortOrder
import com.industriaop.travelsnap.domain.repository.PlaceRepository
import com.industriaop.travelsnap.util.MessageType
import com.industriaop.travelsnap.util.UiMessage
import com.industriaop.travelsnap.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlaceListUiState(
    val places: List<Place> = emptyList(),
    val filter: PlaceFilter = PlaceFilter(),
    val placePendingDelete: Place? = null,
    val message: UiMessage? = null
)

class PlaceListViewModel(
    private val repository: PlaceRepository
) : ViewModel() {

    private val filter = MutableStateFlow(PlaceFilter())
    private val placePendingDelete = MutableStateFlow<Place?>(null)
    private val message = MutableStateFlow<UiMessage?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val places = filter.flatMapLatest { repository.observePlaces(it) }

    val uiState: StateFlow<PlaceListUiState> = combine(
        places, filter, placePendingDelete, message
    ) { places, filter, pending, message ->
        PlaceListUiState(places, filter, pending, message)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlaceListUiState())

    // ----------------------------------------------------- Search and filters

    fun onQueryChange(query: String) = filter.update { it.copy(query = query) }

    fun onCategorySelected(category: PlaceCategory?) = filter.update { it.copy(category = category) }

    fun onProvinceSelected(province: Province?) = filter.update { it.copy(province = province) }

    fun onFavoritesOnlyChange(onlyFavorites: Boolean) =
        filter.update { it.copy(onlyFavorites = onlyFavorites) }

    fun onSortOrderChange(sortOrder: SortOrder) = filter.update { it.copy(sortOrder = sortOrder) }

    fun onClearFilters() = filter.update { PlaceFilter(sortOrder = it.sortOrder) }

    // -------------------------------------------------------------- Actions

    fun onToggleFavorite(placeId: Long) {
        viewModelScope.launch { repository.toggleFavorite(placeId) }
    }

    /** Step 1 of deleting: the UI shows the confirmation dialog. */
    fun onDeleteRequested(place: Place) {
        placePendingDelete.value = place
    }

    fun onDeleteCancelled() {
        placePendingDelete.value = null
    }

    /** Step 2 of deleting: the user confirmed in the dialog. */
    fun onDeleteConfirmed() {
        val place = placePendingDelete.value ?: return
        viewModelScope.launch {
            try {
                repository.deletePlace(place.id)
                message.value = UiMessage(MessageType.SUCCESS, R.string.msg_place_deleted)
            } catch (e: Exception) {
                message.value = UiMessage(MessageType.ERROR, R.string.msg_generic_error)
            } finally {
                placePendingDelete.value = null
            }
        }
    }

    fun onMessageShown() {
        message.value = null
    }
}