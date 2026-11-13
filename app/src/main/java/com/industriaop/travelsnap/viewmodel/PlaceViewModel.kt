package com.industriaop.travelsnap.viewmodel

import androidx.lifecycle.ViewModel
import com.industriaop.travelsnap.data.memory.MemoryDataRepository
import com.industriaop.travelsnap.domain.model.Place
import com.industriaop.travelsnap.domain.repository.IPlaceRepository

class PlaceViewModel : ViewModel() {
    companion object {
        private val repository: IPlaceRepository = MemoryDataRepository()

        fun getPlaces(): List<Place> = repository.getPlaces()
        fun getPlaceById(id: String): Place? = repository.getById(id)
        fun savePlace(place: Place) = repository.addPlace(place)
        fun updatePlace(place: Place) = repository.updatePlace(place)
        fun removePlace(id: String) = repository.deletePlace(id)
    }
}
