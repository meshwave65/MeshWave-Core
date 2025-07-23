package com.meshwave.core

import java.io.Serializable

/**
 * Define o pacote de dados que o LocationModule envia.
 */
data class LocationData(
    val claGeohash: String,
    val preciseGeohash: String
) : Serializable
