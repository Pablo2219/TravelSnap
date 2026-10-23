package com.industriaop.travelsnap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.industriaop.travelsnap.domain.model.Place
import com.industriaop.travelsnap.domain.model.PlaceFilter
import com.industriaop.travelsnap.domain.repository.PlaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class MapUiState(
    val places: List<Place> = emptyList(),
    val selectedPlace: Place? = null
)

class MapViewModel(
    repository: PlaceRepository
) : ViewModel() {

    private val selectedPlaceId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<MapUiState> = combine(
        repository.observePlaces(PlaceFilter()), selectedPlaceId
    ) { places, selectedId ->
        MapUiState(
            places = places,
            selectedPlace = places.firstOrNull { it.id == selectedId }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MapUiState())

    /** Called when the user taps a marker. */
    fun onMarkerSelected(placeId: Long) {
        selectedPlaceId.value = placeId
    }

    fun onSelectionCleared() {
        selectedPlaceId.value = null
    }
}