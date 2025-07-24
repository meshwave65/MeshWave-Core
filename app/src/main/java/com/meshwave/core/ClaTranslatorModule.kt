package com.meshwave.core

import android.content.Context
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Módulo Hiper-Especializado para traduzir Geohash em formato CLA.
 * Responsabilidades:
 * 1. Baixar o mapeamento (CSV) da internet.
 * 2. Armazenar o mapeamento em cache local (arquivo).
 * 3. Carregar o mapeamento em memória para consulta rápida.
 * 4. Traduzir um geohash para o formato "CLAx.y".
 */
object ClaTranslatorModule {

    private const val CLA_MAP_URL = "https://docs.google.com/spreadsheets/d/e/2PACX-1vQqu2GszlcKr8xoG0bB_E0xa7S_FdHJmdvLXSrBIlaXtLkud0w7c7SeLC990wS896feoofUOt4513eQ/pub?gid=768142967&single=true&output=csv"
    private const val CACHE_FILE_NAME = "cla_map.csv"

    // Cache em memória para acesso instantâneo.
    // Mapeia um Geohash (String ) para um Par de Coordenadas (Int, Int).
    private var claMap: Map<String, Pair<Int, Int>>? = null

    /**
     * Ponto de entrada principal. Garante que o mapa esteja carregado e traduz o geohash.
     * Implementa a estratégia de cache que você descreveu.
     */
    suspend fun translate(context: Context, geohash: String): String {
        // Se o mapa não está em memória, carrega-o.
        if (claMap == null) {
            loadMapFromCache(context)
        }

        // Se, mesmo após tentar carregar do cache, o mapa ainda for nulo,
        // tenta baixar da internet.
        if (claMap == null) {
            fetchAndCacheMap(context)
        }

        // Se o mapa finalmente estiver carregado, faz a tradução.
        claMap?.let { map ->
            val coords = map[geohash]
            return if (coords != null) {
                "CLA${coords.first}.${coords.second}"
            } else {
                "CLA?.?" // Geohash não encontrado no mapa
            }
        }

        // Se tudo falhar, retorna o estado de falha.
        return "CLAFail"
    }

    /**
     * Baixa o CSV da internet e o salva no cache local.
     */
    private suspend fun fetchAndCacheMap(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val client = HttpClient(CIO)
                val response: HttpResponse = client.get(CLA_MAP_URL)
                val csvData = response.bodyAsText()
                client.close()

                val cacheFile = File(context.cacheDir, CACHE_FILE_NAME)
                cacheFile.writeText(csvData)

                // Após baixar, processa e carrega em memória.
                claMap = parseCsvToMap(csvData)
            } catch (e: Exception) {
                // Em caso de falha de rede, o claMap permanece nulo.
                e.printStackTrace()
            }
        }
    }

    /**
     * Tenta carregar o mapa a partir do arquivo de cache local.
     */
    private fun loadMapFromCache(context: Context) {
        try {
            val cacheFile = File(context.cacheDir, CACHE_FILE_NAME)
            if (cacheFile.exists()) {
                val csvData = cacheFile.readText()
                claMap = parseCsvToMap(csvData)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Converte o texto bruto do CSV em um mapa para busca rápida.
     */
    private fun parseCsvToMap(csvData: String): Map<String, Pair<Int, Int>> {
        val map = mutableMapOf<String, Pair<Int, Int>>()
        val lines = csvData.lines()

        // Pula a linha do cabeçalho (geohash,coord_x,coord_y)
        for (i in 1 until lines.size) {
            val line = lines[i].trim()
            if (line.isNotEmpty()) {
                val parts = line.split(',')
                if (parts.size == 3) {
                    val geohash = parts[0].trim()
                    val x = parts[1].trim().toIntOrNull()
                    val y = parts[2].trim().toIntOrNull()
                    if (x != null && y != null) {
                        map[geohash] = Pair(x, y)
                    }
                }
            }
        }
        return map
    }
}
