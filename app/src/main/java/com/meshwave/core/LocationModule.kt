// LocationModule.kt
// VERSÃO FINAL COM COMUNICAÇÃO VIA HANDLER

package com.meshwave.core

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import ch.hsr.geohash.GeoHash
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationModule(private val context: Context, private val uiHandler: Handler) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val TAG = "LocationModule"

    // Novos códigos de mensagem para o Handler
    companion object {
        const val GEOHASH_UPDATE = 10
        const val LOG_UPDATE = 99
    }

    fun start() {
        Log.d(TAG, "Iniciando módulo de localização...")
        logToUi("[Loc] Iniciando verificação de permissão...")
        fetchLocation()
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Permissão de localização não concedida.")
            logToUi("[Loc] Falha: Permissão negada.")
            sendGeohashUpdate("g9fail_permission")
            return
        }

        logToUi("[Loc] Permissão OK. Solicitando localização...")
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    val geohash = GeoHash.withCharacterPrecision(location.latitude, location.longitude, 9)
                    Log.i(TAG, "Localização obtida com sucesso. Geohash: ${geohash.toBase32()}")
                    logToUi("[Loc] Sucesso: Localização obtida.")
                    sendGeohashUpdate(geohash.toBase32())
                } else {
                    Log.e(TAG, "Falha ao obter localização: objeto de localização é nulo.")
                    logToUi("[Loc] Falha: API retornou localização nula.")
                    sendGeohashUpdate("g9fail_null")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Falha ao obter localização via API.", e)
                logToUi("[Loc] Falha: Erro na API do Google.")
                sendGeohashUpdate("g9fail_api")
            }
    }

    // Envia uma mensagem de log para a MainActivity
    private fun logToUi(message: String) {
        val msg = uiHandler.obtainMessage(LOG_UPDATE, message)
        uiHandler.sendMessage(msg)
    }

    // Envia o resultado (Geohash ou falha) para a MainActivity
    private fun sendGeohashUpdate(data: String) {
        val msg = uiHandler.obtainMessage(GEOHASH_UPDATE, data)
        uiHandler.sendMessage(msg)
    }
}
