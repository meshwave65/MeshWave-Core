package com.meshwave.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "meshwave_cache")

class CacheModule(private val context: Context) {

    companion object {
        private val LOCAL_NODE_CPA_KEY = stringPreferencesKey("local_node_cpa")
        private val SYNCED_NODES_CACHE_KEY = stringPreferencesKey("synced_nodes_cache")
    }

    suspend fun saveLocalNode(node: NodeCPA) {
        context.dataStore.edit { preferences ->
            preferences[LOCAL_NODE_CPA_KEY] = Json.encodeToString(node)
        }
    }

    suspend fun getLocalNode(): NodeCPA? {
        val json = context.dataStore.data.first()[LOCAL_NODE_CPA_KEY]
        return if (json != null) Json.decodeFromString<NodeCPA>(json) else null
    }

    suspend fun saveSyncedNodes(nodes: Map<String, NodeCPA>) {
        context.dataStore.edit { preferences ->
            preferences[SYNCED_NODES_CACHE_KEY] = Json.encodeToString(nodes)
        }
    }

    suspend fun getSyncedNodes(): Map<String, NodeCPA> {
        val json = context.dataStore.data.first()[SYNCED_NODES_CACHE_KEY]
        return if (json != null) Json.decodeFromString<Map<String, NodeCPA>>(json) else emptyMap()
    }
}
