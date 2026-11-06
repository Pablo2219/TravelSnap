package com.industriaop.travelsnap.domain.repository

import com.industriaop.travelsnap.domain.model.Place
import com.industriaop.travelsnap.domain.model.PlaceFilter
import kotlinx.coroutines.flow.Flow

/**
 * Place operations.
 * Simple synchronous methods are used by the current course-level CRUD.
 * Flow methods remain temporarily for screens that are still being migrated.
 */
interface PlaceRepository {
    fun getPlaces(filter: PlaceFilter = PlaceFilter()): List<Place>
    fun getPlaceById(id: Long): Place?
    fun addPlaceNow(place: Place): Long
    fun updatePlaceNow(place: Place)
    fun deletePlaceNow(id: Long)
    fun toggleFavoriteNow(id: Long)

    fun observePlaces(filter: PlaceFilter = PlaceFilter()): Flow<List<Place>>
    fun observePlace(id: Long): Flow<Place?>
    suspend fun getPlaceByIdAsync(id: Long): Place?
    suspend fun addPlace(place: Place): Long
    suspend fun updatePlace(place: Place)
    suspend fun deletePlace(id: Long)
    suspend fun toggleFavorite(id: Long)
}