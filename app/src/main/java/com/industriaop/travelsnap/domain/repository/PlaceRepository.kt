package com.industriaop.travelsnap.domain.repository

import com.industriaop.travelsnap.domain.model.Place
import com.industriaop.travelsnap.domain.model.PlaceFilter

/**
 * Basic place operations used by the TravelSnap activities.
 * Data is stored in memory while the project practices CRUD.
 */
interface PlaceRepository {
    fun getPlaces(filter: PlaceFilter = PlaceFilter()): List<Place>
    fun getPlaceById(id: Long): Place?
    fun addPlace(place: Place): Long
    fun updatePlace(place: Place)
    fun deletePlace(id: Long)
    fun toggleFavorite(id: Long)
}
