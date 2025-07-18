package com.meshwave.core

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

@SuppressLint("MissingPermission")
class WiFiDirectModule(
    private val context: Context,
    private val uiHandler: Handler,
    private val identityModule: IdentityModule
) {

    private val wifiP2pManager: WifiP2pManager? by lazy(LazyThreadSafetyMode.NONE) {
        context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager?
    }

    private var channel: WifiP2pManager.Channel? = null
    private val receiver: BroadcastReceiver
    private val intentFilter = IntentFilter()
    private var isP2pConnected = false

    private val moduleScope = CoroutineScope(Job() + Dispatchers.IO)

    private val connectionInfoListener = WifiP2pManager.ConnectionInfoListener { info ->
        logToUi("[Debug] ConnectionInfoListener: groupFormed=${info.groupFormed}")
        if (info.groupFormed && info.isGroupOwner) {
            logToUi("Estado: Conectado como Servidor. Abrindo porta...")
            startServerTask()
        } else if (info.groupFormed) {
            logToUi("Estado: Conectado como Cliente. Sincronizando...")
            startClientTask(info.groupOwnerAddress?.hostAddress)
        }
    }

    private val peerListListener = WifiP2pManager.PeerListListener { peerList ->
        val peers = peerList.deviceList
        if (peers.isEmpty()) {
            logToUi("Pares: Nenhum encontrado.")
            return@PeerListListener
        }
        val peerNames = peers.joinToString { it.deviceName }
        logToUi("Pares: $peerNames")

        // Conecta ao primeiro par encontrado
        connectToDevice(peers.first())
    }

    companion object {
        private const val TAG = "WiFiDirectModule"
    }

    init {
        receiver = WiFiDirectBroadcastReceiver()
        setupIntentFilter()
    }

    private fun setupIntentFilter() {
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

    fun start() {
        logToUi("Módulo WiFi iniciado. Escolha um modo.")
        if (wifiP2pManager == null) {
            logToUi("Erro: Serviço P2P indisponível")
            return
        }
        if (channel != null) return

        channel = wifiP2pManager?.initialize(context, Looper.getMainLooper()) {
            logToUi("Erro: Canal P2P perdido.")
            resetConnection()
        }
        context.registerReceiver(receiver, intentFilter)
    }

    fun stop() {
        logToUi("Módulo WiFi parando...")
        moduleScope.cancel()
        removeCurrentGroup {
            if (channel != null) {
                try {
                    context.unregisterReceiver(receiver)
                } catch (e: IllegalArgumentException) {
                    // Ignora
                }
                channel = null
                isP2pConnected = false
                logToUi("Estado: Parado")
            }
        }
    }

    private fun resetConnection() {
        isP2pConnected = false
        uiHandler.obtainMessage(AppConstants.PARTNER_CPA_UPDATE, null).sendToTarget()
    }

    fun becomeServer() {
        logToUi("Ação: Ativando modo Servidor...")
        removeCurrentGroup {
            wifiP2pManager?.createGroup(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    logToUi("Estado: Servidor Ativo. Aguardando clientes.")
                }
                override fun onFailure(reason: Int) {
                    logToUi("Erro ao criar grupo: $reason")
                }
            })
        }
    }

    fun becomeClient() {
        logToUi("Ação: Ativando modo Cliente...")
        removeCurrentGroup {
            wifiP2pManager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    logToUi("Estado: Buscando Servidores...")
                }
                override fun onFailure(reason: Int) {
                    logToUi("Erro ao buscar: $reason")
                }
            })
        }
    }

    private fun connectToDevice(device: WifiP2pDevice) {
        if (isP2pConnected) return
        logToUi("Ação: Tentando conectar a ${device.deviceName}...")
        val config = WifiP2pConfig().apply { deviceAddress = device.deviceAddress }
        wifiP2pManager?.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                logToUi("Ação: Convite enviado.")
            }
            override fun onFailure(reason: Int) {
                logToUi("Erro ao conectar: $reason.")
            }
        })
    }

    private fun removeCurrentGroup(onRemoved: (() -> Unit)? = null) {
        wifiP2pManager?.requestGroupInfo(channel) { group ->
            if (group != null) {
                wifiP2pManager?.removeGroup(channel, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() { onRemoved?.invoke() }
                    override fun onFailure(reason: Int) { onRemoved?.invoke() }
                })
            } else {
                onRemoved?.invoke()
            }
        }
    }

    private fun logToUi(logMessage: String) {
        val message = uiHandler.obtainMessage(AppConstants.LOG_UPDATE, logMessage)
        uiHandler.sendMessage(message)
    }

    inner class WiFiDirectBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    logToUi("[Event] Conexão mudou.")
                    val p2pInfo: WifiP2pInfo? = intent.getParcelableExtraCompat(WifiP2pManager.EXTRA_WIFI_P2P_INFO, WifiP2pInfo::class.java)
                    if (p2pInfo?.groupFormed == true) {
                        if (!isP2pConnected) {
                            isP2pConnected = true
                            wifiP2pManager?.requestConnectionInfo(channel, connectionInfoListener)
                        }
                    } else {
                        if (isP2pConnected) {
                            logToUi("Estado: Desconectado.")
                            resetConnection()
                        }
                    }
                }
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    if (channel != null) wifiP2pManager?.requestPeers(channel, peerListListener)
                }
            }
        }
    }

    private fun startServerTask() = moduleScope.launch {
        logToUi("[Socket] Servidor iniciado.")
        val partnerCpa = runServer()
        withContext(Dispatchers.Main) {
            if (partnerCpa != null) {
                logToUi("[Socket] CPA do Cliente recebido!")
                uiHandler.obtainMessage(AppConstants.PARTNER_CPA_UPDATE, partnerCpa).sendToTarget()
            } else {
                logToUi("[Socket] Erro: Falha ao receber CPA do Cliente.")
            }
        }
    }

    private suspend fun runServer(): NodeCPA? = withContext(Dispatchers.IO) {
        try {
            ServerSocket(8888).use { serverSocket ->
                val client = serverSocket.accept()
                logToUi("[Socket] Cliente conectado ao servidor.")
                val input = ObjectInputStream(client.getInputStream())
                val clientCPA = input.readObject() as? NodeCPA
                val output = ObjectOutputStream(client.getOutputStream())
                output.writeObject(identityModule.getCurrentCPA())
                output.flush()
                return@withContext clientCPA
            }
        } catch (e: Exception) {
            logToUi("[Socket] Erro no servidor: ${e.message}")
            return@withContext null
        }
    }

    private fun startClientTask(hostAddress: String?) = moduleScope.launch {
        logToUi("[Socket] Cliente iniciado.")
        val partnerCpa = runClient(hostAddress, identityModule.getCurrentCPA())
        withContext(Dispatchers.Main) {
            if (partnerCpa != null) {
                logToUi("[Socket] CPA do Servidor recebido!")
                uiHandler.obtainMessage(AppConstants.PARTNER_CPA_UPDATE, partnerCpa).sendToTarget()
            } else {
                logToUi("[Socket] Erro: Falha ao receber CPA do Servidor.")
            }
        }
    }

    private suspend fun runClient(hostAddress: String?, myCpa: NodeCPA?): NodeCPA? = withContext(Dispatchers.IO) {
        if (hostAddress == null || myCpa == null) return@withContext null
        logToUi("[Socket] Cliente tentando conectar a $hostAddress")
        delay(1000)
        val socket = Socket()
        try {
            socket.bind(null)
            socket.connect(InetSocketAddress(hostAddress, 8888), 5000)
            logToUi("[Socket] Cliente conectado ao servidor.")
            val output = ObjectOutputStream(socket.getOutputStream())
            output.writeObject(myCpa)
            output.flush()
            val input = ObjectInputStream(socket.getInputStream())
            return@withContext input.readObject() as? NodeCPA
        } catch (e: Exception) {
            logToUi("[Socket] Erro no cliente: ${e.message}")
            return@withContext null
        } finally {
            if (socket.isConnected) socket.close()
        }
    }
}

fun <T> Intent.getParcelableExtraCompat(key: String, clazz: Class<T>): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getParcelableExtra(key, clazz)
    } else {
        @Suppress("DEPRECATION")
        this.getParcelableExtra(key) as? T
    }
}
