package com.meshwave.core

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.provider.Settings
import android.util.Log
import java.util.UUID
import kotlin.random.Random

class IdentityModule(private val context: Context, private val uiHandler: Handler) {

    private val TAG = "IdentityModule"

    companion object {
        const val CPA_UPDATE = 20
        const val LOG_UPDATE = 99
    }

    private var nodeCPA: NodeCPA? = null

    fun start() {
        Log.d(TAG, "Iniciando módulo de identidade...")
        logToUi("[Id] Aguardando Geohash da Área para gerar CPA...")
    }

    fun receiveAreaGeohash(areaGeohash: String) {
        if (nodeCPA != null) return
        logToUi("[Id] Geohash da Área recebido: $areaGeohash. Gerando CPA...")
        generateCPA(areaGeohash)
        sendCpaUpdate()
    }

    private fun generateCPA(areaGeohash: String) {
        val did = try {
            @SuppressLint("HardwareIds")
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            "did:mesh:${UUID.nameUUIDFromBytes(androidId.toByteArray())}"
        } catch (e: Exception) {
            Log.e(TAG, "Não foi possível gerar DID", e)
            "did:mesh:g9fail_did"
        }
        logToUi("[Id] DID gerado.")

        val username = "User-${Random.nextInt(100, 999)}"
        logToUi("[Id] Username gerado: $username")

        nodeCPA = NodeCPA(
            did = did,
            username = username,
            cpaGeohash = areaGeohash,
            creationTimestamp = System.currentTimeMillis(),
            currentClaGeohash = areaGeohash,
            status = "1"
        )
        logToUi("[Id] CPA montado com sucesso.")
    }

    private fun logToUi(message: String) {
        val msg = uiHandler.obtainMessage(LOG_UPDATE, message)
        uiHandler.sendMessage(msg)
    }

    private fun sendCpaUpdate() {
        nodeCPA?.let {
            val msg = uiHandler.obtainMessage(CPA_UPDATE, it)
            uiHandler.sendMessage(msg)
        }
    }
}

data class NodeCPA(
    val did: String,
    val username: String,
    val cpaGeohash: String,
    val creationTimestamp: Long,
    var currentClaGeohash: String,
    var status: String
)
