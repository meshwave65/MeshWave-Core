package com.meshwave.core

import java.io.Serializable

/**
 * A estrutura de dados UNIFICADA que representa a identidade e o estado de um nó na rede.
 * Esta é a única fonte da verdade para esta estrutura no projeto.
 */
data class NodeCPA(
    // --- Identidade Fundamental (O "RG") ---
    val did: String,                 // Identificador Descentralizado Único (did:mesh:uuid)
    var username: String,            // Nome de usuário mutável
    val creationTimestamp: Long,     // Timestamp de quando o nó foi criado

    // --- Estado de Localização (O "GPS") ---
    var claGeohash: String,          // Geohash Nível 6 (a "vizinhança")
    var preciseGeohash: String,      // Geohash Nível 9 (a "localização exata")
    var locationTimestamp: Long,     // Timestamp da última atualização de localização
    var locationStatus: LocationStatus, // Saúde da informação de localização (VEM DO ARQUIVO LocationStatus.kt)

    // --- Metadados Operacionais ---
    var networkStatus: Int           // Status geral na rede (ativo, inativo, etc.)
) : Serializable
