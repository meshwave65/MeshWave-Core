package com.meshwave.core

import android.content.Context
import android.content.SharedPreferences
//import com.meshwave.core.model.identity.LocationStatus
import com.meshwave.core.NodeCPA
import java.util.*

/**
 * Gerencia o armazenamento e a criação da identidade local (NodeCPA) do dispositivo.
 * Atua como o "cartório" local.
 */
class IdentityManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("MeshWaveIdentity", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DID = "did"
        private const val KEY_USERNAME = "username"
        private const val KEY_CREATION_TIMESTAMP = "creation_timestamp"
        // Adicionamos chaves para os novos campos
        private const val KEY_CLA_GEOHASH = "cla_geohash"
        private const val KEY_PRECISE_GEOHASH = "precise_geohash"
        private const val KEY_LOCATION_TIMESTAMP = "location_timestamp"
        private const val KEY_LOCATION_STATUS = "location_status"
        private const val KEY_NETWORK_STATUS = "network_status"
    }

    /**
     * Carrega o NodeCPA salvo no disco. Retorna null se não existir.
     */
    fun loadNodeCPA(): NodeCPA? {
        if (!prefs.contains(KEY_DID)) {
            return null
        }
        return NodeCPA(
            did = prefs.getString(KEY_DID, "")!!,
            username = prefs.getString(KEY_USERNAME, "")!!,
            creationTimestamp = prefs.getLong(KEY_CREATION_TIMESTAMP, 0L),
            claGeohash = prefs.getString(KEY_CLA_GEOHASH, AppConstants.GEOHASH_FAIL_CLA)!!,
            preciseGeohash = prefs.getString(KEY_PRECISE_GEOHASH, AppConstants.GEOHASH_FAIL_PRECISE)!!,
            locationTimestamp = prefs.getLong(KEY_LOCATION_TIMESTAMP, 0L),
            locationStatus = LocationStatus.valueOf(prefs.getString(KEY_LOCATION_STATUS, LocationStatus.FAILED.name)!!),
            networkStatus = prefs.getInt(KEY_NETWORK_STATUS, 1)
        )
    }

    /**
     * Salva o NodeCPA completo no disco.
     */
    fun saveNodeCPA(nodeCPA: NodeCPA) {
        prefs.edit().apply {
            putString(KEY_DID, nodeCPA.did)
            putString(KEY_USERNAME, nodeCPA.username)
            putLong(KEY_CREATION_TIMESTAMP, nodeCPA.creationTimestamp)
            putString(KEY_CLA_GEOHASH, nodeCPA.claGeohash)
            putString(KEY_PRECISE_GEOHASH, nodeCPA.preciseGeohash)
            putLong(KEY_LOCATION_TIMESTAMP, nodeCPA.locationTimestamp)
            putString(KEY_LOCATION_STATUS, nodeCPA.locationStatus.name)
            putInt(KEY_NETWORK_STATUS, nodeCPA.networkStatus)
            apply()
        }
    }

    /**
     * Cria uma identidade completamente nova para o dispositivo.
     * A "Certidão de Nascimento".
     */
    fun createNewIdentity(): NodeCPA {
        val did = "did:mesh:${UUID.randomUUID()}"
        val username = "User-${(100..999).random()}"
        val creationTimestamp = System.currentTimeMillis()

        return NodeCPA(
            did = did,
            username = username,
            creationTimestamp = creationTimestamp,
            claGeohash = AppConstants.GEOHASH_FAIL_CLA, // Começa em estado de falha
            preciseGeohash = AppConstants.GEOHASH_FAIL_PRECISE,
            locationTimestamp = 0L,
            locationStatus = LocationStatus.FAILED,
            networkStatus = 1 // Ativo
        )
    }
}
