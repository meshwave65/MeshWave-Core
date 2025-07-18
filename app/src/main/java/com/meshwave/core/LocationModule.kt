package com.meshwave.core

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.util.Log
import androidx.core.app.ActivityCompat
import ch.hsr.geohash.GeoHash
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationModule(private val context: Context, private val uiHandler: Handler) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val TAG = "LocationModule"

    fun start() {
        Log.d(TAG, "Iniciando módulo de localização...")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        requestLocation()
    }

    private fun requestLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Permissão de localização não concedida.")
            sendGeohashUpdate(MainActivity.LOG_UPDATE, "[Loc] Falha: Permissão negada.")
            sendGeohashUpdate(MainActivity.LOCATION_UPDATE, LocationData("g9fail_permission", "g9fail_permission"))
            return
        }

        Log.d(TAG, "Permissão OK. Solicitando localização...")
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    val nodeGeohash = GeoHash.withCharacterPrecision(location.latitude, location.longitude, 9).toBase32()
                    val areaGeohash = GeoHash.withCharacterPrecision(location.latitude, location.longitude, 6).toBase32()
                    Log.i(TAG, "Localização obtida. Geohash Nó (9): $nodeGeohash, Geohash Área (6): $areaGeohash")
                    sendGeohashUpdate(MainActivity.LOG_UPDATE, "[Loc] Sucesso: Localização obtida.")
                    sendGeohashUpdate(MainActivity.LOCATION_UPDATE, LocationData(nodeGeohash, areaGeohash))
                } else {
                    Log.e(TAG, "Falha ao obter localização: objeto location é nulo.")
                    sendGeohashUpdate(MainActivity.LOG_UPDATE, "[Loc] Falha: Localização nula.")
                    sendGeohashUpdate(MainActivity.LOCATION_UPDATE, LocationData("g9fail_null", "g9fail_null"))
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erro na API de localização: ", e)
                sendGeohashUpdate(MainActivity.LOG_UPDATE, "[Loc] Falha: API error - ${e.message}")
                sendGeohashUpdate(MainActivity.LOCATION_UPDATE, LocationData("g9fail_api", "g9fail_api"))
            }
    }

    private fun sendGeohashUpdate(what: Int, data: Any) {
        val msg = uiHandler.obtainMessage(what, data)
        uiHandler.sendMessage(msg)
    }
}
