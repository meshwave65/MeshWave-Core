package com.meshwave.core

// Correto: Apenas a definição dos dados.
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
