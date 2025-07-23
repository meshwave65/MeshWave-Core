package com.meshwave.core

import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap

data class SituationalCache(
    val nodes: ConcurrentHashMap<String, NodeProfile> = ConcurrentHashMap(),
    val discoveredPeers: ConcurrentHashMap<String, DiscoveredPeer> = ConcurrentHashMap()
) : Serializable