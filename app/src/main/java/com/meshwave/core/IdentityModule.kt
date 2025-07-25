package com.meshwave.core

import java.util.UUID

class IdentityModule(private val cacheModule: CacheModule) {

    private var currentNodeCPA: NodeCPA? = null

    suspend fun initialize() {
        var node = cacheModule.getLocalNode()
        if (node == null) {
            val did = "did:mesh:" + UUID.randomUUID().toString()
            val username = "User-" + (100..999).random()
            node = NodeCPA(
                did = did,
                username = username,
                creationTimestamp = System.currentTimeMillis(),
                claGeohash = AppConstants.GEOHASH_FAIL_CLA,
                preciseGeohash = AppConstants.GEOHASH_FAIL_PRECISE,
                locationTimestamp = 0L,
                locationStatus = LocationStatus.FAILED,
                networkStatus = 1
            )
            cacheModule.saveLocalNode(node)
        }
        currentNodeCPA = node
    }

    fun getLocalNode(): NodeCPA {
        return currentNodeCPA!!
    }

    suspend fun updateCpaWithLocation(locationData: LocationData) {
        currentNodeCPA?.let {
            it.claGeohash = locationData.claGeohash
            it.preciseGeohash = locationData.preciseGeohash
            it.locationStatus = LocationStatus.UPDATED
            it.locationTimestamp = System.currentTimeMillis()
            cacheModule.saveLocalNode(it)
        }
    }

    suspend fun updateCpaStatus(newStatus: LocationStatus) {
        currentNodeCPA?.let {
            it.locationStatus = newStatus
            cacheModule.saveLocalNode(it)
        }
    }
}
