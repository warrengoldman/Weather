package com.example.weather.weather

import kotlinx.serialization.Serializable

@Serializable
data class GeoInstance(
    val name: String,
    val lat: Double,
    val lon: Double
)
