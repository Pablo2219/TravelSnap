package com.industriaop.travelsnap.domain.repository

import com.industriaop.travelsnap.domain.model.PlaceFilter
import com.industriaop.travelsnap.domain.model.Place
import kotlinx.coroutines.flow.Flow

interface PlaceRepository {

    /** Emits the places matching [filter] every time the data changes. */
    fun observePlaces(filter: PlaceFilter = PlaceFilter()): Flow<List<Place>>

    /** Emits a single place, or null if it does not exist (anymore). */
    fun observePlace(id: Long): Flow<Place?>

    suspend fun getPlaceById(id: Long): Place?

    /** Inserts a new place and returns the generated id. */
    suspend fun addPlace(place: Place): Long

    suspend fun updatePlace(place: Place)

    suspend fun deletePlace(id: Long)

    suspend fun toggleFavorite(id: Long)
}