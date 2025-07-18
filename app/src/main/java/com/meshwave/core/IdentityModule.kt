package com.meshwave.core

import android.content.Context
import android.os.Handler
import android.util.Log
import java.util.UUID
import kotlin.random.Random

class IdentityModule(private val context: Context, private val uiHandler: Handler) {

    private val TAG = "IdentityModule"
    private var nodeCPA: NodeCPA? = null

    fun start() {
        Log.d(TAG, "Iniciando módulo de identidade...")
        // A geração do CPA agora depende do Geohash, então esperamos por ele.
        sendToLog("[Id] Aguardando Geohash da Área para gerar CPA...")
    }

    fun generateCpa(areaGeohash: String) {
        if (nodeCPA != null) {
            Log.d(TAG, "CPA já gerado. Ignorando.")
            return
        }

        Log.d(TAG, "Geohash da Área recebido: $areaGeohash. Gerando CPA...")

        val did = "did:mesh:${UUID.randomUUID()}"
        sendToLog("[Id] DID gerado.")

        val username = "User-${Random.nextInt(100, 999)}"
        sendToLog("[Id] Username gerado: $username")

        nodeCPA = NodeCPA(
            did = did,
            username = username,
            cpaGeohash = areaGeohash,
            creationTimestamp = System.currentTimeMillis(),
            currentClaGeohash = areaGeohash, // No nascimento, a CLA atual é a CPA de origem
            status = "1" // 1 = Ativo
        )

        sendToLog("[Id] CPA montado com sucesso.")
        sendCpaUpdate()
    }

    private fun sendCpaUpdate() {
        nodeCPA?.let {
            val msg = uiHandler.obtainMessage(MainActivity.IDENTITY_UPDATE, it)
            uiHandler.sendMessage(msg)
        }
    }

    private fun sendToLog(message: String) {
        val msg = uiHandler.obtainMessage(MainActivity.LOG_UPDATE, message)
        uiHandler.sendMessage(msg)
    }
}
