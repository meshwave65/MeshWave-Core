// Arquivo: app/src/main/java/com/meshwave/core/NodeCPA.kt
package com.meshwave.core

import java.io.Serializable

/**
 * Representa o Cache Primário de Atualização (CPA) de um nó.
 * Contém a identidade fundamental e o estado do nó na rede.
 *
 * Implementa Serializable para permitir que o objeto seja enviado pela rede.
 */
data class NodeCPA(
    val did: String,                // Identificador único do dispositivo (ex: UUID)
    val username: String,           // Nome de usuário gerado (ex: User-XXX)
    val cpaGeohash: String,         // Geohash de nível 6 da CPA de origem. Imutável.
    val creationTimestamp: Long,    // Momento do "nascimento" na rede. Imutável.
    var currentClaGeohash: String,  // Geohash de nível 6 da CLA onde o nó está atualmente. Mutável.
    var status: Int                 // Status do nó na rede (ex: 1 para Ativo). Mutável.
) : Serializable // A única adição necessária ao seu código original.
