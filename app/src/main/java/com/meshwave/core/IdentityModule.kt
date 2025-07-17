package com.meshwave.core

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import java.util.UUID

class IdentityModule(private val context: Context, private val uiHandler: Handler) {

    private val TAG = "IdentityModule"

    // Constantes para comunicação com a MainActivity
    companion object {
        const val IDENTITY_UPDATE = 20
        const val LOG_UPDATE = 99 // Usamos o mesmo código de log
    }

    private var did: String = "N/A"
    private var username: String = "N/A"

    fun start() {
        Log.d(TAG, "Iniciando módulo de identidade...")
        logToUi("[Id] Iniciando...")
        generateIdentity()
        sendIdentityUpdate()
    }

    private fun generateIdentity() {
        // Gerar DID (Identificador Único do Dispositivo)
        // A forma mais robusta e que não requer permissões perigosas é usar o ANDROID_ID.
        // Ele é único para cada combinação de app-usuário-dispositivo.
        // O @SuppressLint é necessário porque o lint pode reclamar, mas este é o uso correto.
        logToUi("[Id] Gerando DID...")
        did = try {
            @SuppressLint("HardwareIds")
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            // Usamos o androidId como base para um UUID para garantir um formato consistente.
            "did:mesh:${UUID.nameUUIDFromBytes(androidId.toByteArray())}"
        } catch (e: Exception) {
            Log.e(TAG, "Não foi possível gerar DID a partir do ANDROID_ID", e)
            "did:mesh:g9fail_did" // Nosso default fail
        }
        logToUi("[Id] DID gerado: $did")


        // Gerar Username
        // Por enquanto, vamos criar um username baseado no modelo do dispositivo.
        // No futuro, isso virá de uma configuração do usuário.
        logToUi("[Id] Gerando Username...")
        username = try {
            val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
            val model = Build.MODEL
            "User-$manufacturer-$model"
        } catch (e: Exception) {
            Log.e(TAG, "Não foi possível gerar username", e)
            "User-g9fail_username"
        }
        logToUi("[Id] Username gerado: $username")
    }

    // Envia uma mensagem de log para a MainActivity
    private fun logToUi(message: String) {
        val msg = uiHandler.obtainMessage(LOG_UPDATE, message)
        uiHandler.sendMessage(msg)
    }

    // Envia a identidade (DID e Username) para a MainActivity
    private fun sendIdentityUpdate() {
        val identityData = arrayOf(did, username)
        val msg = uiHandler.obtainMessage(IDENTITY_UPDATE, identityData)
        uiHandler.sendMessage(msg)
    }
}