// LocationModule.kt
// VERSÃO COMPLETA E REATORADA PARA MÚLTIPLOS NÍVEIS DE GEOHASH

package com.meshwave.core

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.util.Log
import androidx.core.app.ActivityCompat
import ch.hsr.geohash.GeoHash
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationModule(private val context: Context, private val uiHandler: Handler) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val TAG = "LocationModule"

    companion object {
        const val GEOHASH_UPDATE = 10
        const val LOG_UPDATE = 99
    }

    fun start() {
        Log.d(TAG, "Iniciando módulo de localização...")
        logToUi("[Loc] Iniciando verificação de permissão...")
        fetchLocation()
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Permissão de localização não concedida.")
            logToUi("[Loc] Falha: Permissão negada.")
            val failData = LocationData("g9fail_permission", "g9fail_permission_area")
            sendGeohashUpdate(failData)
            return
        }

        logToUi("[Loc] Permissão OK. Solicitando localização...")
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    val nodeGeohash = GeoHash.withCharacterPrecision(location.latitude, location.longitude, 9)
                    val areaGeohash = GeoHash.withCharacterPrecision(location.latitude, location.longitude, 6)

                    Log.i(TAG, "Localização obtida. Geohash Nó (9): ${nodeGeohash.toBase32()}, Geohash Área (6): ${areaGeohash.toBase32()}")
                    logToUi("[Loc] Sucesso: Localização obtida.")

                    val successData = LocationData(nodeGeohash.toBase32(), areaGeohash.toBase32())
                    sendGeohashUpdate(successData)
                } else {
                    Log.e(TAG, "Falha ao obter localização: objeto de localização é nulo.")
                    logToUi("[Loc] Falha: API retornou localização nula.")
                    val failData = LocationData("g9fail_null", "g9fail_null_area")
                    sendGeohashUpdate(failData)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Falha ao obter localização via API.", e)
                logToUi("[Loc] Falha: Erro na API do Google.")
                val failData = LocationData("g9fail_api", "g9fail_api_area")
                sendGeohashUpdate(failData)
            }
    }

    private fun logToUi(message: String) {
        val msg = uiHandler.obtainMessage(LOG_UPDATE, message)
        uiHandler.sendMessage(msg)
    }

    private fun sendGeohashUpdate(data: LocationData) {
        val msg = uiHandler.obtainMessage(GEOHASH_UPDATE, data)
        uiHandler.sendMessage(msg)
    }
}

/**
 * Uma estrutura de dados para guardar os diferentes níveis de precisão do Geohash.
 * @param nodeGeohash O Geohash de alta precisão (nível 9) para a localização exata do nó.
 * @param areaGeohash O Geohash de média precisão (nível 6) para identificar a CLA/CPA.
 */
data class LocationData(val nodeGeohash: String, val areaGeohash: String)
