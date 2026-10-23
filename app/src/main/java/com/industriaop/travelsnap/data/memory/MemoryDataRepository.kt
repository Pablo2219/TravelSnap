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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class MemoryDataRepository : PlaceRepository, UserRepository {

    private val places = MutableStateFlow(seedPlaces())
    private var nextId: Long = (places.value.maxOfOrNull { it.id } ?: 0L) + 1L

    private val currentUser = MutableStateFlow<User?>(null)
    private val settings = MutableStateFlow(UserSettings())

    // ---------------------------------------------------------------- Places

    override fun observePlaces(filter: PlaceFilter): Flow<List<Place>> =
        places.map { list -> applyFilter(list, filter) }

    override fun observePlace(id: Long): Flow<Place?> =
        places.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun getPlaceById(id: Long): Place? =
        places.value.firstOrNull { it.id == id }

    override suspend fun addPlace(place: Place): Long {
        val id = synchronized(this) { nextId++ }
        places.update { it + place.copy(id = id) }
        return id
    }

    override suspend fun updatePlace(place: Place) {
        places.update { list -> list.map { if (it.id == place.id) place else it } }
    }

    override suspend fun deletePlace(id: Long) {
        places.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun toggleFavorite(id: Long) {
        places.update { list ->
            list.map { if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it }
        }
    }

    private fun applyFilter(list: List<Place>, filter: PlaceFilter): List<Place> {
        val query = TextUtils.normalize(filter.query)
        val filtered = list.filter { place ->
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

    // ----------------------------------------------------------------- Users

    override fun observeCurrentUser(): Flow<User?> = currentUser

    override fun observeSettings(): Flow<UserSettings> = settings

    override suspend fun login(email: String, password: String): User? {
        val valid = email.trim().equals(Constants.DEMO_EMAIL, ignoreCase = true) &&
                password == Constants.DEMO_PASSWORD
        return if (valid) {
            User(id = 1L, name = Constants.DEMO_NAME, email = Constants.DEMO_EMAIL)
                .also { currentUser.value = it }
        } else {
            null
        }
    }

    override suspend fun logout() {
        currentUser.value = null
    }

    override suspend fun setLanguage(language: AppLanguage) {
        settings.update { it.copy(language = language) }
    }

    override suspend fun setDarkTheme(enabled: Boolean) {
        settings.update { it.copy(darkTheme = enabled) }
    }

    // ------------------------------------------------------------- Seed data

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