package com.industriaop.travelsnap

import com.industriaop.travelsnap.data.memory.MemoryDataRepository
import com.industriaop.travelsnap.domain.model.Place
import com.industriaop.travelsnap.domain.model.PlaceCategory
import com.industriaop.travelsnap.domain.model.Province
import org.junit.Assert.*
import org.junit.Test

class MemoryDataRepositoryTest {
    @Test
    fun createSearchUpdateAndDelete() {
        val repository = MemoryDataRepository()
        val place = Place("1", "Arenal", "Volcano", PlaceCategory.VOLCANO,
            Province.ALAJUELA, 10.46, -84.7)
        repository.addPlace(place)
        assertEquals(place, repository.getById("1"))
        repository.updatePlace(place.copy(name = "Volcan Arenal"))
        assertEquals(1, repository.getPlaces().size)
        assertEquals("Volcan Arenal", repository.getById("1")!!.name)
        repository.deletePlace("1")
        assertTrue(repository.getPlaces().isEmpty())
        assertNull(repository.getById("1"))
    }
}
