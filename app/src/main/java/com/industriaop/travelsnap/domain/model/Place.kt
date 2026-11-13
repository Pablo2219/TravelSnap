package com.industriaop.travelsnap.domain.model

data class Place(
    val id: String,
    val name: String,
    val description: String,
    val category: PlaceCategory,
    val province: Province,
    val latitude: Double,
    val longitude: Double
)
