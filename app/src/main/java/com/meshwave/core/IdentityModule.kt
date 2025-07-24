package com.meshwave.core

import java.util.UUID

// CORRIGIDO: Adicionado (private val cacheModule: CacheModule) ao construtor da classe.
class IdentityModule(private val cacheModule: CacheModule) {

    private var currentNodeCPA: NodeCPA? = null

    suspend fun initialize() {
        if (currentNodeCPA == null) {
            val cachedGeohash = cacheModule.getLastKnownGeohash()
            val initialCla = cachedGeohash?.first ?: AppConstants.GEOHASH_FAIL_CLA
            val initialPrecise = cachedGeohash?.second ?: AppConstants.GEOHASH_FAIL_PRECISE
            val initialStatus = if (cachedGeohash != null) LocationStatus.STALE else LocationStatus.FAILED

            val did = "did:mesh:" + UUID.randomUUID().toString()
            val username = "User-" + (100..999).random()
            currentNodeCPA = NodeCPA(
                did = did,
                username = username,
                creationTimestamp = System.currentTimeMillis(),
                claGeohash = initialCla,
                preciseGeohash = initialPrecise,
                locationTimestamp = 0L,
                locationStatus = initialStatus,
                networkStatus = 1
            )
        }
    }

    fun getOrCreateCurrentNodeCPA(): NodeCPA {
        if (currentNodeCPA == null) {
            return NodeCPA("temp", "temp", 0, "", "", 0, LocationStatus.FAILED, 0)
        }
        return currentNodeCPA!!
    }

    suspend fun updateCpaWithLocation(locationData: LocationData) {
        currentNodeCPA = getOrCreateCurrentNodeCPA().copy(
            claGeohash = locationData.claGeohash,
            preciseGeohash = locationData.preciseGeohash,
            locationStatus = LocationStatus.UPDATED,
            locationTimestamp = System.currentTimeMillis()
        )
        cacheModule.saveLastKnownGeohash(locationData.claGeohash, locationData.preciseGeohash)
    }

    fun updateCpaStatus(newStatus: LocationStatus) {
        getOrCreateCurrentNodeCPA().locationStatus = newStatus
    }
}
