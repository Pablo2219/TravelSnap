package com.industriaop.travelsnap

import com.industriaop.travelsnap.data.memory.MemoryDataRepository
import com.industriaop.travelsnap.domain.model.Place
import com.industriaop.travelsnap.domain.model.PlaceCategory
import com.industriaop.travelsnap.domain.model.PlaceFilter
import com.industriaop.travelsnap.domain.model.Province
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MemoryDataRepositoryTest {
    @Test
    fun createsUpdatesAndDeletesById() {
        val repository = MemoryDataRepository()
        val place = Place(
            name = "Test beach",
            description = "A place for the CRUD test",
            category = PlaceCategory.BEACH,
            province = Province.PUNTARENAS,
            latitude = 9.5,
            longitude = -84.1
        )

        val id = repository.addPlace(place)
        assertEquals(place.copy(id = id), repository.getPlaceById(id))

        val updatedPlace = place.copy(id = id, name = "Updated beach")
        repository.updatePlace(updatedPlace)
        assertEquals(updatedPlace, repository.getPlaceById(id))

        repository.deletePlace(id)
        assertNull(repository.getPlaceById(id))
    }

    @Test
    fun filtersByNameProvinceAndFavorites() {
        val repository = MemoryDataRepository()
        val filter = PlaceFilter(query = "irazu", province = Province.CARTAGO, onlyFavorites = true)
        assertEquals(emptyList<Place>(), repository.getPlaces(filter))

        repository.toggleFavorite(4L)
        assertEquals(listOf(4L), repository.getPlaces(filter).map { it.id })
    }
}
