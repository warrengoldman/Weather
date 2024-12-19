package com.example.weather.weather

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Rain(@SerialName("3h") val rainAmount: Double? = null)
