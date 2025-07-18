package com.meshwave.core

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import ch.hsr.geohash.GeoHash
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.Timer
import java.util.TimerTask

@SuppressLint("MissingPermission")
class LocationModule(
    private val context: Context,
    private val uiHandler: Handler,
    private val identityModule: IdentityModule
) {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationTimer: Timer? = null
    private var lastKnownGoodGeohash: String? = null

    companion object {
        private const val TAG = "LocationModule"
        private const val LOCATION_UPDATE_INTERVAL_MS = 15000L
        const val GEOHASH_FAIL_NODE = "g9failxxx"
        const val GEOHASH_FAIL_AREA = "g6fail"
    }

    fun start() {
        logToUi("[Loc] Módulo iniciando...")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        startLocationUpdates()
    }

    fun stop() {
        logToUi("[Loc] Módulo parando...")
        locationTimer?.cancel()
        locationTimer = null
    }

    private fun startLocationUpdates() {
        if (locationTimer != null) return
        locationTimer = Timer()
        locationTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                requestLocation()
            }
        }, 0, LOCATION_UPDATE_INTERVAL_MS)
    }

    private fun requestLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            handleLocationError("Permissão negada.")
            return
        }

        logToUi("[Loc] Solicitando localização...")
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    val nodeGeohash = GeoHash.withCharacterPrecision(location.latitude, location.longitude, 9).toBase32()
                    val areaGeohash = GeoHash.withCharacterPrecision(location.latitude, location.longitude, 6).toBase32()
                    logToUi("[Loc] Sucesso: $nodeGeohash")

                    sendToUi(AppConstants.LOCATION_UPDATE, LocationData(nodeGeohash, LocationStatus.UPDATED))
                    lastKnownGoodGeohash = nodeGeohash

                    identityModule.updateCurrentLocation(nodeGeohash)
                    identityModule.updateCpaIfNeeded(areaGeohash)
                } else {
                    handleLocationError("Localização nula.")
                }
            }
            .addOnFailureListener { e ->
                handleLocationError("API error - ${e.message}")
            }
    }

    private fun handleLocationError(reason: String) {
        logToUi("[Loc] Falha: $reason")
        if (lastKnownGoodGeohash != null) {
            sendToUi(AppConstants.LOCATION_UPDATE, LocationData(lastKnownGoodGeohash!!, LocationStatus.STALE))
        } else {
            sendToUi(AppConstants.LOCATION_UPDATE, LocationData(GEOHASH_FAIL_NODE, LocationStatus.FAILED))
        }
    }

    private fun sendToUi(what: Int, data: Any) {
        val msg = uiHandler.obtainMessage(what, data)
        uiHandler.sendMessage(msg)
    }

    private fun logToUi(logMessage: String) {
        Log.d(TAG, logMessage)
        sendToUi(AppConstants.LOG_UPDATE, logMessage)
    }
}
