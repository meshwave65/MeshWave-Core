package com.meshwave.core

import kotlinx.serialization.Serializable

@Serializable // Habilita a conversão para JSON
data class NodeCPA(
    val did: String,
    val username: String,
    val creationTimestamp: Long,
    var claGeohash: String,
    var preciseGeohash: String,
    var locationTimestamp: Long,
    var locationStatus: LocationStatus,
    var networkStatus: Int
)
