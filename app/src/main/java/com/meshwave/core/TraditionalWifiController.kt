package com.meshwave.core

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import kotlin.coroutines.resume

@Suppress("DEPRECATION") // Suprime avisos para a classe inteira, pois a API NSD legada é usada de forma consciente
class TraditionalWifiController(context: Context) {

    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val SERVICE_TYPE = "_meshwave._tcp."
    private var serviceName = "MeshWaveCoreService"

    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null

    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null

    suspend fun startServerAndRegisterService(): Socket? {
        return withContext(Dispatchers.IO) {
            try {
                serverSocket = ServerSocket(0).also { socket ->
                    val localPort = socket.localPort
                    println("[Trad-WiFi Server] Socket aberto na porta: $localPort")
                    val serviceInfo = NsdServiceInfo().apply {
                        this.serviceName = this@TraditionalWifiController.serviceName
                        this.serviceType = SERVICE_TYPE
                        this.port = localPort
                    }
                    registrationListener = createRegistrationListener()
                    nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
                }
                val connectedSocket = serverSocket?.accept()
                println("[Trad-WiFi Server] Cliente conectado: ${connectedSocket?.remoteSocketAddress}")
                clientSocket = connectedSocket
                return@withContext clientSocket
            } catch (e: IOException) {
                println("[Trad-WiFi Server] Erro ao iniciar o servidor: ${e.message}")
                closeServer()
                return@withContext null
            }
        }
    }

    fun closeServer() {
        registrationListener?.let { try { nsdManager.unregisterService(it) } catch (e: Exception) { /* Ignorar */ } }
        try {
            serverSocket?.close()
            clientSocket?.close()
        } catch (e: IOException) { /* Ignorar */ }
        println("[Trad-WiFi] Conexões do servidor fechadas.")
    }

    private fun createRegistrationListener(): NsdManager.RegistrationListener {
        return object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(info: NsdServiceInfo) {
                serviceName = info.serviceName
                println("[NSD] Serviço registrado: ${info.serviceName}")
            }
            override fun onRegistrationFailed(s: NsdServiceInfo, e: Int) { println("[NSD] Falha ao registrar: $e") }
            override fun onServiceUnregistered(info: NsdServiceInfo) { println("[NSD] Serviço não registrado: ${info.serviceName}") }
            override fun onUnregistrationFailed(s: NsdServiceInfo, e: Int) {}
        }
    }

    fun discoverServices(onServiceFound: (NsdServiceInfo) -> Unit) {
        stopDiscovery()
        discoveryListener = createDiscoveryListener(onServiceFound)
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    suspend fun connectToService(serviceInfo: NsdServiceInfo): Socket? {
        return withContext(Dispatchers.IO) {
            try {
                val resolvedService = resolveService(serviceInfo)
                resolvedService?.let {
                    val socket = Socket(it.host, it.port)
                    println("[Trad-WiFi Client] Conectado a ${it.host}:${it.port}")
                    clientSocket = socket
                    return@withContext clientSocket
                }
            } catch (e: IOException) {
                println("[Trad-WiFi Client] Erro ao conectar: ${e.message}")
            }
            return@withContext null
        }
    }

    fun stopDiscovery() {
        discoveryListener?.let { try { nsdManager.stopServiceDiscovery(it) } catch (e: Exception) { /* Ignorar */ } }
        println("[NSD] Descoberta parada.")
    }

    private suspend fun resolveService(serviceInfo: NsdServiceInfo): NsdServiceInfo? =
        suspendCancellableCoroutine { continuation ->
            val resolveListener = object : NsdManager.ResolveListener {
                override fun onResolveFailed(s: NsdServiceInfo, e: Int) {
                    if (continuation.isActive) continuation.resume(null)
                }
                override fun onServiceResolved(info: NsdServiceInfo) {
                    if (continuation.isActive) continuation.resume(info)
                }
            }
            nsdManager.resolveService(serviceInfo, resolveListener)
        }

    private fun createDiscoveryListener(onServiceFound: (NsdServiceInfo) -> Unit): NsdManager.DiscoveryListener {
        return object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(r: String) { println("[NSD] Descoberta iniciada.") }
            override fun onServiceFound(s: NsdServiceInfo) { if (s.serviceType == SERVICE_TYPE) onServiceFound(s) }
            override fun onServiceLost(s: NsdServiceInfo) { println("[NSD] Serviço perdido: ${s.serviceName}") }
            override fun onDiscoveryStopped(s: String) { println("[NSD] Descoberta parada.") }
            override fun onStartDiscoveryFailed(s: String, e: Int) { println("[NSD] Falha ao iniciar descoberta: $e") }
            override fun onStopDiscoveryFailed(s: String, e: Int) { println("[NSD] Falha ao parar descoberta: $e") }
        }
    }
}
