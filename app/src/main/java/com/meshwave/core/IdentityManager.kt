// Local: app/src/main/java/com/meshwave/core/IdentityManager.kt
package com.meshwave.core

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

class IdentityManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("MeshWaveIdentity", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DID = "did"
        private const val KEY_USERNAME = "username"
        private const val KEY_CPA_GEOHASH = "cpa_geohash"
        private const val KEY_CREATION_TIMESTAMP = "creation_timestamp"
    }

    fun loadNodeCPA(): NodeCPA? {
        if (!prefs.contains(KEY_DID)) {
            return null // Nenhuma identidade salva
        }
        return NodeCPA(
            did = prefs.getString(KEY_DID, "")!!,
            username = prefs.getString(KEY_USERNAME, "")!!,
            cpaGeohash = prefs.getString(KEY_CPA_GEOHASH, "")!!,
            creationTimestamp = prefs.getLong(KEY_CREATION_TIMESTAMP, 0L),
            currentClaGeohash = "", // Será preenchido pela localização atual
            status = 1 // Sempre inicia como ativo
        )
    }

    fun saveNodeCPA(nodeCPA: NodeCPA) {
        prefs.edit().apply {
            putString(KEY_DID, nodeCPA.did)
            putString(KEY_USERNAME, nodeCPA.username)
            putString(KEY_CPA_GEOHASH, nodeCPA.cpaGeohash)
            putLong(KEY_CREATION_TIMESTAMP, nodeCPA.creationTimestamp)
            apply()
        }
    }

    fun createNewIdentity(activationGeohash: String): NodeCPA {
        val did = "did:mesh:${UUID.randomUUID()}"
        val username = "User-${(100..999).random()}"
        val creationTimestamp = System.currentTimeMillis()

        val newNodeCPA = NodeCPA(
            did = did,
            username = username,
            cpaGeohash = activationGeohash,
            creationTimestamp = creationTimestamp,
            currentClaGeohash = activationGeohash,
            status = 1
        )
        saveNodeCPA(newNodeCPA)
        return newNodeCPA
    }
}
