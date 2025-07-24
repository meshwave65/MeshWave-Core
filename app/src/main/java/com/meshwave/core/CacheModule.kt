package com.meshwave.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Cria uma instância do DataStore para o nosso aplicativo.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "meshwave_cache")

/**
 * Módulo Hiper-Especializado para gerenciar o cache persistente do nó local.
 * Usa o Jetpack DataStore para salvar os dados em um arquivo de forma segura.
 */
class CacheModule(private val context: Context) {

    // Define as "chaves" para os dados que vamos salvar.
    companion object {
        val LAST_KNOWN_GEOHASH_CLA = stringPreferencesKey("last_known_geohash_cla")
        val LAST_KNOWN_GEOHASH_PRECISE = stringPreferencesKey("last_known_geohash_precise")
    }

    /**
     * Salva o último geohash conhecido no arquivo de cache.
     */
    suspend fun saveLastKnownGeohash(cla: String, precise: String) {
        context.dataStore.edit { cache ->
            cache[LAST_KNOWN_GEOHASH_CLA] = cla
            cache[LAST_KNOWN_GEOHASH_PRECISE] = precise
        }
    }

    /**
     * Lê o último geohash conhecido do arquivo de cache.
     * Retorna um Par (cla, precise) ou null se não houver nada salvo.
     */
    suspend fun getLastKnownGeohash(): Pair<String, String>? {
        val preferences = context.dataStore.data.first()
        val cla = preferences[LAST_KNOWN_GEOHASH_CLA]
        val precise = preferences[LAST_KNOWN_GEOHASH_PRECISE]

        return if (cla != null && precise != null) {
            Pair(cla, precise)
        } else {
            null
        }
    }
}
