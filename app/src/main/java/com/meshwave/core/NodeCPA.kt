package com.meshwave.core

/**
 * Representa o Cache Primário de Atualização (CPA) de um nó.
 * Contém a identidade fundamental e o estado do nó na rede.
 */
data class NodeCPA(
    val did: String,                // Identificador único do dispositivo (ex: UUID)
    val username: String,           // Nome de usuário gerado (ex: User-XXX)
    val cpaGeohash: String,         // Geohash de nível 6 da CPA de origem. Imutável.
    val creationTimestamp: Long,    // Momento do "nascimento" na rede. Imutável.
    var currentClaGeohash: String,  // Geohash de nível 6 da CLA onde o nó está atualmente. Mutável.
    var status: String              // Status do nó na rede (ex: "1" para Ativo). Mutável.
)
