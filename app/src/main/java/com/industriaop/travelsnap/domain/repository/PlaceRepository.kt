package com.industriaop.travelsnap.domain.repository

import com.industriaop.travelsnap.domain.model.Place
import com.industriaop.travelsnap.domain.model.PlaceFilter

/**
 * Basic operations for places.
 * The methods are synchronous because this course project stores data in memory.
 */
interface PlaceRepository {
    fun getPlaces(filter: PlaceFilter = PlaceFilter()): List<Place>
    fun getPlaceById(id: Long): Place?
    fun addPlace(place: Place): Long
    fun updatePlace(place: Place)
    fun deletePlace(id: Long)
    fun toggleFavorite(id: Long)
}