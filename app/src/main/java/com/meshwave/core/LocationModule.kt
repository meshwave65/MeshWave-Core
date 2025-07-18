// Local: app/src/main/java/com/meshwave/core/LocationModule.kt
package com.meshwave.core

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import ch.hsr.geohash.GeoHash
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationModule(private val context: Context, private val uiHandler: Handler) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val TAG = "LocationModule"
    private val locationHandler = Handler(Looper.getMainLooper())
    private var isRunning = false

    companion object {
        private const val UPDATE_INTERVAL_MS = 30000L // Atualiza a cada 30 segundos
    }

    private val locationRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                requestLocation()
                locationHandler.postDelayed(this, UPDATE_INTERVAL_MS)
            }
        }
    }

    fun start() {
        if (isRunning) return
        Log.d(TAG, "Iniciando ciclo de atualização de localização...")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        isRunning = true
        locationHandler.post(locationRunnable) // Inicia o ciclo imediatamente
    }

    fun stop() {
        if (!isRunning) return
        Log.d(TAG, "Parando ciclo de atualização de localização.")
        isRunning = false
        locationHandler.removeCallbacks(locationRunnable)
    }

    private fun requestLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            sendToUi(MainActivity.LOG_UPDATE, "[Loc] Falha: Permissão negada.")
            return
        }

        sendToUi(MainActivity.LOG_UPDATE, "[Loc] Solicitando localização...")
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    val nodeGeohash = GeoHash.withCharacterPrecision(location.latitude, location.longitude, 9).toBase32()
                    val areaGeohash = GeoHash.withCharacterPrecision(location.latitude, location.longitude, 6).toBase32()
                    sendToUi(MainActivity.LOG_UPDATE, "[Loc] Sucesso: Localização obtida.")
                    sendToUi(MainActivity.LOCATION_UPDATE, LocationData(nodeGeohash, areaGeohash))
                } else {
                    Log.e(TAG, "Falha ao obter localização: objeto location é nulo.")
                    sendToUi(MainActivity.LOG_UPDATE, "[Loc] Falha: Localização nula.")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erro na API de localização: ", e)
                sendToUi(MainActivity.LOG_UPDATE, "[Loc] Falha: API error - ${e.message}")
            }
    }

    private fun sendToUi(what: Int, data: Any) {
        val msg = uiHandler.obtainMessage(what, data)
        uiHandler.sendMessage(msg)
    }
}
