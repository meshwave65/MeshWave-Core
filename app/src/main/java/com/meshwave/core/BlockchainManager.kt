package com.meshwave.core

import android.content.Context
import com.meshwave.core.blockchain.BirthCertificate
import com.meshwave.core.blockchain.Block
import com.meshwave.core.blockchain.BlockchainContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.security.MessageDigest
import java.util.*

object BlockchainManager {

    private const val BLOCKCHAIN_FILE_NAME = "MeshBlockchain.json"

    /**
     * Ponto de entrada principal. Cria a identidade, gera o bloco gênese e o salva em um arquivo.
     * Retorna true se o arquivo foi criado com sucesso.
     */
    suspend fun createAndStoreIdentity(context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Criar a Certidão de Nascimento com valores determinados
                val certificate = createDeterministicBirthCertificate(context)

                // 2. Criar o Bloco Gênese
                val genesisBlock = createGenesisBlock(certificate)

                // 3. Criar o contêiner da blockchain
                val blockchainContainer = BlockchainContainer(chain = listOf(genesisBlock))

                // 4. Converter para JSON
                val json = Json { prettyPrint = true } // prettyPrint para facilitar a leitura do arquivo
                val jsonString = json.encodeToString(blockchainContainer)

                // 5. Salvar o arquivo no armazenamento interno do app
                val file = File(context.filesDir, BLOCKCHAIN_FILE_NAME)
                file.writeText(jsonString)
                
                return@withContext true

            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext false
            }
        }
    }

    /**
     * Verifica se o arquivo da blockchain já existe no dispositivo.
     */
    fun isBlockchainCreated(context: Context): Boolean {
        val file = File(context.filesDir, BLOCKCHAIN_FILE_NAME)
        return file.exists()
    }

    private fun createDeterministicBirthCertificate(context: Context): BirthCertificate {
        // Lógica para obter o ID do dispositivo (vamos simular por enquanto)
        val imeiDid = "did:mesh:${UUID.randomUUID()}" // Simulação de um ID único
        val birthCPA = "SA.BR.RJ.COP.CPA001" // Valor fixo
        val birthTimestamp = System.currentTimeMillis()

        // O "apadrinhamento" é determinado, não buscado na rede.
        val sponsorDid = "did:mesh:foundation-node-01" // Padrinho determinado
        val sponsorSignature = "signature(${hash(imeiDid + sponsorDid)})" // Assinatura simulada

        val dnaHash = hash("$imeiDid$birthCPA$birthTimestamp$sponsorSignature")

        return BirthCertificate(
            imeiDid = imeiDid,
            birthCPA = birthCPA,
            birthTimestamp = birthTimestamp,
            dnaHash = dnaHash,
            sponsorDid = sponsorDid,
            sponsorSignature = sponsorSignature
        )
    }

    private fun createGenesisBlock(certificate: BirthCertificate): Block {
        val timestamp = System.currentTimeMillis()
        val transactions = listOf(certificate)
        val previousHash = "0" // O Bloco Gênese sempre aponta para "0"
        
        // Em um cenário PoA, o validador assinaria o bloco. Aqui, o próprio app "assina".
        val validatorSignature = "signature(${hash(transactions.toString() + timestamp + previousHash)})"

        return Block(
            index = 1,
            timestamp = timestamp,
            transactions = transactions,
            validatorSignature = validatorSignature,
            previousHash = previousHash
        )
    }

    // Função utilitária para criar hashes SHA-256
    private fun hash(input: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }
}

