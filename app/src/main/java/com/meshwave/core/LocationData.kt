package com.meshwave.core

enum class LocationStatus {
    UPDATED, STALE, FAILED
}

data class LocationData(val geohash: String, val status: LocationStatus)
