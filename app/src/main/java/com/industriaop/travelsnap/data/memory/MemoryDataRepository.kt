package com.industriaop.travelsnap.data.memory

import com.industriaop.travelsnap.domain.model.AppLanguage
import com.industriaop.travelsnap.domain.model.Place
import com.industriaop.travelsnap.domain.model.PlaceCategory
import com.industriaop.travelsnap.domain.model.PlaceFilter
import com.industriaop.travelsnap.domain.model.Province
import com.industriaop.travelsnap.domain.model.SortOrder
import com.industriaop.travelsnap.domain.model.User
import com.industriaop.travelsnap.domain.model.UserSettings
import com.industriaop.travelsnap.domain.repository.PlaceRepository
import com.industriaop.travelsnap.domain.repository.UserRepository
import com.industriaop.travelsnap.util.Constants
import com.industriaop.travelsnap.util.TextUtils

/**
 * Stores the sample places in a mutable list, following the memory manager
 * pattern practiced in class. User settings keep their existing observer for now.
 */
class MemoryDataRepository : PlaceRepository, UserRepository {

    private val places = mutableListOf<Place>().apply { addAll(seedPlaces()) }
    private var nextId: Long = (places.maxOfOrNull { it.id } ?: 0L) + 1L

    private var currentUser: User? = null
    private var settings = UserSettings()

    // Places: simple in-memory CRUD operations

    override fun getPlaces(filter: PlaceFilter): List<Place> {
        val query = TextUtils.normalize(filter.query)
        val filtered = places.filter { place ->
            (query.isEmpty() || TextUtils.normalize(place.name).contains(query)) &&
                (filter.category == null || place.category == filter.category) &&
                (filter.province == null || place.province == filter.province) &&
                (!filter.onlyFavorites || place.isFavorite)
        }
        val byName = compareBy<Place> { TextUtils.normalize(it.name) }
        return when (filter.sortOrder) {
            SortOrder.NAME_ASC -> filtered.sortedWith(byName)
            SortOrder.NAME_DESC -> filtered.sortedWith(byName.reversed())
        }
    }

    override fun getPlaceById(id: Long): Place? =
        places.firstOrNull { it.id == id }

    override fun addPlace(place: Place): Long {
        val id = nextId++
        places.add(place.copy(id = id))
        return id
    }

    override fun updatePlace(place: Place) {
        val index = places.indexOfFirst { it.id == place.id }
        if (index >= 0) places[index] = place
    }

    override fun deletePlace(id: Long) {
        places.removeAll { it.id == id }
    }

    override fun toggleFavorite(id: Long) {
        val index = places.indexOfFirst { it.id == id }
        if (index >= 0) {
            val place = places[index]
            places[index] = place.copy(isFavorite = !place.isFavorite)
        }
    }

    // User settings

    override fun getCurrentUser(): User? = currentUser

    override fun getSettings(): UserSettings = settings

    override fun login(email: String, password: String): User? {
        val valid = email.trim().equals(Constants.DEMO_EMAIL, ignoreCase = true) &&
            password == Constants.DEMO_PASSWORD
        return if (valid) {
            User(id = 1L, name = Constants.DEMO_NAME, email = Constants.DEMO_EMAIL)
                .also { currentUser = it }
        } else {
            null
        }
    }

    override fun logout() {
        currentUser = null
    }

    override fun setLanguage(language: AppLanguage) {
        settings = settings.copy(language = language)
    }

    override fun setDarkTheme(enabled: Boolean) {
        settings = settings.copy(darkTheme = enabled)
    }

    // Initial sample data

    private fun seedPlaces(): List<Place> = listOf(
        Place(
            id = 1L,
            name = "Manuel Antonio Beach",
            description = "Calm beach inside the national park, with monkeys and sloths nearby.",
            category = PlaceCategory.BEACH,
            province = Province.PUNTARENAS,
            latitude = 9.3922,
            longitude = -84.1367,
            isFavorite = true
        ),
        Place(
            id = 2L,
            name = "Arenal Volcano",
            description = "Iconic cone-shaped volcano with hiking trails and hot springs.",
            category = PlaceCategory.VOLCANO,
            province = Province.ALAJUELA,
            latitude = 10.4626,
            longitude = -84.7031
        ),
        Place(
            id = 3L,
            name = "La Fortuna Waterfall",
            description = "70 meter waterfall reached by a stairway of about 500 steps.",
            category = PlaceCategory.WATERFALL,
            province = Province.ALAJUELA,
            latitude = 10.4417,
            longitude = -84.6700
        ),
        Place(
            id = 4L,
            name = "Irazú Volcano National Park",
            description = "Highest active volcano in the country, with a green crater lake.",
            category = PlaceCategory.NATURAL_PARK,
            province = Province.CARTAGO,
            latitude = 9.9793,
            longitude = -83.8523
        ),
        Place(
            id = 5L,
            name = "Cerro Chirripó",
            description = "Highest mountain in Costa Rica, a multi-day hike.",
            category = PlaceCategory.MOUNTAIN,
            province = Province.SAN_JOSE,
            latitude = 9.4844,
            longitude = -83.4885
        )
    )
}