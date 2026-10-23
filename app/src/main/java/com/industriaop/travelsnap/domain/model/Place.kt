package com.industriaop.travelsnap.domain.model

data class Place(
    val id: Long = 0L,
    val name: String,
    val description: String,
    val category: PlaceCategory,
    val province: Province,
    val latitude: Double,
    val longitude: Double,
    val photoUris: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)