package com.example.weather.weather

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Snow(@SerialName("3h") val snowDepth: Double? = null)
