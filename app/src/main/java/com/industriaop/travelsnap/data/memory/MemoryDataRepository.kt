package com.industriaop.travelsnap.data.memory

import com.industriaop.travelsnap.domain.model.Place
import com.industriaop.travelsnap.domain.repository.IPlaceRepository

class MemoryDataRepository : IPlaceRepository {
    private val places = mutableListOf<Place>()

    override fun getPlaces(): List<Place> = places

    override fun getById(id: String): Place? {
        val result = places.filter { it.id == id }
        return if (result.any()) result[0] else null
    }

    override fun addPlace(place: Place) {
        places.add(place)
    }

    override fun updatePlace(place: Place) {
        deletePlace(place.id)
        addPlace(place)
    }

    override fun deletePlace(id: String) {
        places.removeIf { it.id == id }
    }
}
