package com.meshwave.core

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import ch.hsr.geohash.GeoHash // Importa a biblioteca correta
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.TimeUnit

class LocationModule(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    @Throws(SecurityException::class)
    fun fetchLocationUpdates(): Flow<LocationData> = callbackFlow {
        // Verifica se o app tem permissão para acessar a localização precisa.
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Se não tiver, fecha o fluxo com um erro. O ViewModel que chama esta função
            // deve tratar essa exceção (por exemplo, mostrando uma mensagem ao usuário).
            close(SecurityException("Permissão de localização não concedida."))
            return@callbackFlow
        }

        // Configura o pedido de localização: alta precisão, com intervalo de 10 segundos.
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, TimeUnit.SECONDS.toMillis(10)).build()

        // Cria o "ouvinte" que receberá as atualizações de localização do sistema.
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                // Pega a última localização recebida do GPS.
                locationResult.lastLocation?.let { location ->

                    // ================================================================
                    //      SEU TRECHO DE CÓDIGO É INSERIDO EXATAMENTE AQUI
                    // ================================================================
                    val lat = location.latitude
                    val lon = location.longitude

                    // Para o CLA (nível 6)
                    val claGeohash = GeoHash.withCharacterPrecision(lat, lon, 6).toBase32()

                    // Para o Geohash preciso (nível 9)
                    val preciseGeohash = GeoHash.withCharacterPrecision(lat, lon, 9).toBase32()
                    // ================================================================

                    // Cria o objeto LocationData com os geohashes calculados.
                    val locationData = LocationData(
                        claGeohash = claGeohash,
                        preciseGeohash = preciseGeohash
                    )

                    // Envia o objeto LocationData para quem estiver "escutando" este fluxo.
                    trySend(locationData)
                }
            }
        }

        // Registra o nosso "ouvinte" para começar a receber as atualizações.
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        // Este bloco é executado quando o fluxo é cancelado (ex: o app fecha).
        // Ele garante que o GPS seja desligado para economizar bateria.
        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}
