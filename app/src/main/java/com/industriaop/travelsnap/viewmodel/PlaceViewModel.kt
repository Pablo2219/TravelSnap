package com.industriaop.travelsnap.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.industriaop.travelsnap.TravelSnapApp
import com.industriaop.travelsnap.domain.model.Place
import com.industriaop.travelsnap.domain.model.PlaceFilter
import com.industriaop.travelsnap.domain.repository.PlaceRepository

class PlaceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PlaceRepository = (application as TravelSnapApp).repository

    fun getPlaces(filter: PlaceFilter = PlaceFilter()) = repository.getPlaces(filter)

    fun getPlaceById(id: Long) = repository.getPlaceById(id)

    fun addPlace(place: Place) = repository.addPlace(place)

    fun updatePlace(place: Place) = repository.updatePlace(place)

    fun deletePlace(id: Long) = repository.deletePlace(id)

    fun toggleFavorite(id: Long) = repository.toggleFavorite(id)
}
