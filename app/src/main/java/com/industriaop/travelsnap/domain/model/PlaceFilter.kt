package com.industriaop.travelsnap.domain.model

enum class SortOrder {
    NAME_ASC,
    NAME_DESC
}

/**
 * Criteria used to search, filter and sort the list of places.
 * A null category/province means "no filter".
 */
data class PlaceFilter(
    val query: String = "",
    val category: PlaceCategory? = null,
    val province: Province? = null,
    val onlyFavorites: Boolean = false,
    val sortOrder: SortOrder = SortOrder.NAME_ASC
) {
    val isActive: Boolean
        get() = query.isNotBlank() || category != null || province != null || onlyFavorites
}