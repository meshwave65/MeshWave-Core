// Caminho: app/src/main/java/com/meshwave/core/DiscoveredPeer.kt
package com.meshwave.core

import java.io.Serializable

data class DiscoveredPeer(
    val macAddress: String,
    var deviceName: String,
    val did: String,
    var status: Int = 1,
    var lastSeenTimestamp: Long = System.currentTimeMillis()
) : Serializable
