package com.meshwave.core

import java.io.Serializable

data class NodeCPA(
    val did: String,
    val username: String,
    val cpaGeohash: String,
    val creationTimestamp: Long,
    var currentClaGeohash: String,
    var status: Int
) : Serializable
