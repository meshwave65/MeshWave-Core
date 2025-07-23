package com.meshwave.core.blockchain

import java.io.Serializable

// A "Certidão de Nascimento" com valores que podemos determinar localmente.
data class BirthCertificate(
    val imeiDid: String,
    val birthCPA: String,
    val birthTimestamp: Long,
    val dnaHash: String,
    val sponsorDid: String, // Será um valor fixo/determinado
    val sponsorSignature: String // Será um valor fixo/determinado
) : Serializable

// O Bloco, como definido antes.
data class Block(
    val index: Long,
    val timestamp: Long,
    val transactions: List<BirthCertificate>,
    val validatorSignature: String, // Assinatura do "validador" (que será o próprio app neste caso)
    val previousHash: String
) : Serializable

// Um contêiner para a cadeia completa, para facilitar a serialização para JSON.
data class BlockchainContainer(
    val chain: List<Block>
) : Serializable

