package com.meshwave.core

import java.io.Serializable

data class NodeProfile(
    val did: String,
    val username: String,
    var cpaGeohash: String,
    val creationTimestamp: Long = System.currentTimeMillis(),
    var currentClaGeohash: String = cpaGeohash,
    var status: Int = 1
) : Serializable
