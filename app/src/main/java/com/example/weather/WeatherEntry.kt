package com.example.weather

data class WeatherEntry(
    val sunsetFormattedTime: String,
    val temp: Int,
    val heading: String,
    val dt: Long,
    val skyTitle: String,
    val skyDescription: String,
    val skyIcon: String,
    val windMph: Double,
    val windGustMph: Double,
    val windDirection: String,
    val city: String,
    val precip: Double?
)