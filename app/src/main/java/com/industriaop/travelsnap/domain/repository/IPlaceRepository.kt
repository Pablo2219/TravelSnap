package com.industriaop.travelsnap.domain.repository

import com.industriaop.travelsnap.domain.model.Place

interface IPlaceRepository {
    fun getPlaces(): List<Place>
    fun getById(id: String): Place?
    fun addPlace(place: Place)
    fun updatePlace(place: Place)
    fun deletePlace(id: String)
}
