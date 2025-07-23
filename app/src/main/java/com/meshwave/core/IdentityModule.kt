package com.meshwave.core

import java.util.UUID

/**
 * Módulo singleton para gerenciar a identidade do nó (NodeCPA).
 */
object IdentityModule {
    private var currentNodeCPA: NodeCPA? = null

    fun getOrCreateCurrentNodeCPA(): NodeCPA {
        if (currentNodeCPA == null) {
            val did = "did:mesh:" + UUID.randomUUID().toString()
            val username = "User-" + (100..999).random()
            currentNodeCPA = NodeCPA(
                did = did,
                username = username,
                claGeohash = AppConstants.GEOHASH_FAIL_CLA,
                preciseGeohash = AppConstants.GEOHASH_FAIL_PRECISE,
                locationTimestamp = 0L,
                locationStatus = LocationStatus.FAILED,
                creationTimestamp = System.currentTimeMillis(),
                networkStatus = 1
            )
        }
        return currentNodeCPA!!
    }

    fun updateCpaWithLocation(locationData: LocationData) {
        getOrCreateCurrentNodeCPA().apply {
            this.claGeohash = locationData.claGeohash
            this.preciseGeohash = locationData.preciseGeohash
//            this.locationStatus = determineLocationStatus(locationData.claGeohash)
            this.locationTimestamp = System.currentTimeMillis()
        }
    }

    fun markCpaLocationAsFailed() {
        getOrCreateCurrentNodeCPA().apply {
            this.claGeohash = AppConstants.GEOHASH_FAIL_CLA
            this.preciseGeohash = AppConstants.GEOHASH_FAIL_PRECISE
            this.locationStatus = LocationStatus.FAILED
        }
    }

    fun getCurrentNodeCPA(): NodeCPA? {
        return currentNodeCPA
    }
}
