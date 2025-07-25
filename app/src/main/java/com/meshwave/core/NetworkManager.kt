package com.meshwave.core

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

class NetworkManager(
    private val context: Context,
    private val viewModel: MainViewModel,
    private val scope: CoroutineScope,
    private val getInitialCpa: () -> NodeCPA
) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private var serverSocket: ServerSocket? = null
    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var serverJob: Job? = null
    private var discoveryJob: Job? = null

    private var currentNodeCpa: NodeCPA = getInitialCpa()

    companion object {
        const val SERVICE_TYPE = "_meshwave._tcp."
        const val SERVICE_NAME_PREFIX = "MeshWave"
    }

    private val resolveListener = object : NsdManager.ResolveListener {
        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            scope.launch { viewModel.log("Falha ao resolver: ${serviceInfo.serviceName}") }
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            scope.launch(Dispatchers.IO) {
                try {
                    viewModel.log("Serviço encontrado: ${serviceInfo.serviceName}. Conectando...")
                    val socket = Socket(serviceInfo.host, serviceInfo.port)
                    handleCommunication(socket)
                } catch (e: Exception) {
                    viewModel.log("Erro ao conectar em ${serviceInfo.serviceName}: ${e.message}")
                }
            }
        }
    }

    fun startAnnouncing() {
        if (serverJob?.isActive == true) {
            scope.launch { viewModel.log("Anúncio já está ativo.") }
            return
        }
        serverJob = scope.launch(Dispatchers.IO) {
            startServer()
        }
    }

    private suspend fun startServer() {
        try {
            serverSocket = ServerSocket(0).also {
                registerService(it.localPort)
            }

            while (scope.isActive && serverSocket?.isClosed == false) {
                try {
                    val clientSocket = serverSocket!!.accept()
                    scope.launch { viewModel.log("Conexão recebida de: ${clientSocket.inetAddress}") }
                    handleCommunication(clientSocket)
                } catch (e: Exception) {
                    if (scope.isActive) scope.launch { viewModel.log("Erro no laço do servidor: ${e.message}") }
                }
            }
        } catch (e: Exception) {
            scope.launch { viewModel.log("Falha ao iniciar servidor de anúncio: ${e.message}") }
        }
    }

    private fun registerService(port: Int) {
        stopAnnouncing()

        val serviceName = "$SERVICE_NAME_PREFIX-${currentNodeCpa.username}"
        val serviceInfo = NsdServiceInfo().apply {
            this.serviceName = serviceName
            this.serviceType = SERVICE_TYPE
            this.port = port
        }

        registrationListener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(p0: NsdServiceInfo?) {
                scope.launch { viewModel.log("Serviço anunciado: $serviceName") }
            }
            override fun onRegistrationFailed(p0: NsdServiceInfo?, p1: Int) {
                scope.launch { viewModel.log("Falha ao registrar serviço.") }
            }
            override fun onServiceUnregistered(p0: NsdServiceInfo?) {
                scope.launch { viewModel.log("Anúncio parado para: ${p0?.serviceName}") }
            }
            override fun onUnregistrationFailed(p0: NsdServiceInfo?, p1: Int) {}
        }

        registrationListener?.let {
            nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, it)
        }
    }

    fun startDiscovery() {
        if (discoveryJob?.isActive == true) return

        stopDiscovery()

        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(p0: String?) {
                scope.launch { viewModel.log("Busca por serviços iniciada.") }
            }
            override fun onServiceFound(service: NsdServiceInfo) {
                val selfUsername = "$SERVICE_NAME_PREFIX-${currentNodeCpa.username}"
                if (service.serviceName != selfUsername) {
                    nsdManager.resolveService(service, resolveListener)
                }
            }
            override fun onServiceLost(service: NsdServiceInfo) {
                scope.launch { viewModel.onNodeLost(service.serviceName) }
            }
            override fun onDiscoveryStopped(p0: String?) {
                scope.launch { viewModel.log("Busca por serviços parada.") }
            }
            override fun onStartDiscoveryFailed(p0: String?, p1: Int) {
                scope.launch { viewModel.log("Falha ao iniciar busca.") }
            }
            override fun onStopDiscoveryFailed(p0: String?, p1: Int) {}
        }

        discoveryListener?.let {
            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, it)
        }
    }

    private suspend fun handleCommunication(socket: Socket) {
        try {
            socket.use { s ->
                val out = PrintWriter(s.getOutputStream(), true)
                val reader = BufferedReader(InputStreamReader(s.getInputStream()))

                val myCpaJson = Json.encodeToString(currentNodeCpa)
                out.println(myCpaJson)

                val theirCpaJson = reader.readLine()
                if (theirCpaJson != null) {
                    val theirCpa = Json.decodeFromString<NodeCPA>(theirCpaJson)
                    viewModel.log("CPA recebido de ${theirCpa.username}")
                    viewModel.onNodeDiscovered(theirCpa)
                }
            }
        } catch (e: Exception) {
            scope.launch { viewModel.log("Erro na comunicação: ${e.message}") }
        }
    }

    fun updateNodeCpa(newNodeCpa: NodeCPA) {
        scope.launch(Dispatchers.IO) {
            if (currentNodeCpa == newNodeCpa) return@launch

            viewModel.log("Atualizando CPA de rede...")
            currentNodeCpa = newNodeCpa

            if (serverJob?.isActive == true) {
                viewModel.log("Reiniciando anúncio com novo CPA...")
                serverSocket?.localPort?.let { registerService(it) }
            }
        }
    }

    private fun stopAnnouncing() {
        registrationListener?.let {
            try {
                nsdManager.unregisterService(it)
            } catch (e: Exception) {
                // Ignorar
            }
            registrationListener = null
        }
    }

    private fun stopDiscovery() {
        discoveryListener?.let {
            try {
                nsdManager.stopServiceDiscovery(it)
            } catch (e: Exception) {
                // Ignorar
            }
            discoveryListener = null
        }
    }

    fun stopAll() {
        scope.launch { viewModel.log("Parando todos os serviços de rede...") }
        stopAnnouncing()
        stopDiscovery()
        serverSocket?.close()
        serverJob?.cancel()
        discoveryJob?.cancel()
    }
}
