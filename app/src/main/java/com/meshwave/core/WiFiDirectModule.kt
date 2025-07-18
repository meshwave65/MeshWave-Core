// Local: app/src/main/java/com/meshwave/core/WiFiDirectModule.kt
package com.meshwave.core

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.AsyncTask
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

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
    private var isConnected = false

    private val connectionInfoListener = WifiP2pManager.ConnectionInfoListener { info ->
        if (info.groupFormed && info.isGroupOwner) {
            Log.d(TAG, "Este dispositivo é o Dono do Grupo (Servidor).")
            sendToUi("Conectado como Servidor")
            ServerTask().execute()
        } else if (info.groupFormed) {
            Log.d(TAG, "Este dispositivo é o Cliente.")
            sendToUi("Conectado como Cliente")
            ClientTask(info.groupOwnerAddress.hostAddress).execute(identityModule.getCurrentCPA())
        }
    }

    private val peerListListener = WifiP2pManager.PeerListListener { peerList ->
        val refreshedPeers = peerList.deviceList
        if (refreshedPeers.isEmpty()) {
            Log.d(TAG, "Nenhum par encontrado.")
            return@PeerListListener
        }

        if (!isConnected) {
            val device = refreshedPeers.first()
            Log.d(TAG, "Par encontrado: ${device.deviceName}. Tentando conectar...")
            connectToDevice(device)
        }
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
        if (wifiP2pManager == null) {
            sendToUi("Serviço P2P indisponível")
            return
        }
        if (channel != null) {
            discoverPeers()
            return
        }
        channel = wifiP2pManager?.initialize(context, Looper.getMainLooper()) {
            Log.e(TAG, "Canal do Wi-Fi P2P foi desconectado.")
            sendToUi("Canal P2P perdido")
            channel = null
            isConnected = false
        }
        if (channel == null) {
            sendToUi("Falha na inicialização")
            return
        }
        context.registerReceiver(receiver, intentFilter)
        sendToUi("Iniciado")
        discoverPeers()
    }

    private fun discoverPeers() {
        if (channel == null) {
            sendToUi("Erro: Canal P2P nulo")
            return
        }
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.NEARBY_WIFI_DEVICES, Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            listOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (requiredPermissions.any { ActivityCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }) {
            sendToUi("Permissões faltando para buscar")
            return
        }
        wifiP2pManager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                sendToUi("Buscando pares...")
            }
            override fun onFailure(reasonCode: Int) {
                sendToUi("Erro ao buscar: $reasonCode")
            }
        })
    }

    private fun connectToDevice(device: WifiP2pDevice) {
        val config = WifiP2pConfig().apply { deviceAddress = device.deviceAddress }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            sendToUi("Permissão negada para conectar")
            return
        }
        channel?.also { ch ->
            wifiP2pManager?.connect(ch, config, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    sendToUi("Convite enviado para ${device.deviceName}")
                }
                override fun onFailure(reason: Int) {
                    sendToUi("Falha ao conectar: $reason")
                }
            })
        }
    }

    fun stop() {
        if (channel != null) {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Receiver não estava registrado.", e)
            }
            channel = null
            isConnected = false
            sendToUi("Parado")
        }
    }

    private fun sendToUi(statusMessage: String) {
        val message = uiHandler.obtainMessage(MainActivity.WIFI_STATUS_UPDATE, statusMessage)
        uiHandler.sendMessage(message)
    }

    inner class WiFiDirectBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    if (intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1) != WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                        sendToUi("Rádio Desabilitado")
                        channel = null
                        isConnected = false
                    } else {
                        sendToUi("Rádio Habilitado")
                    }
                }
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    if (channel != null) wifiP2pManager?.requestPeers(channel, peerListListener)
                }
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    if (wifiP2pManager == null) return
                    isConnected = true
                    wifiP2pManager?.requestConnectionInfo(channel, connectionInfoListener)
                }
            }
        }
    }

    inner class ServerTask : AsyncTask<Void, Void, NodeCPA>() {
        override fun doInBackground(vararg params: Void?): NodeCPA? {
            try {
                ServerSocket(8888).use { serverSocket ->
                    Log.d(TAG, "Servidor: Socket aberto, aguardando cliente...")
                    val client = serverSocket.accept()
                    Log.d(TAG, "Servidor: Cliente conectado!")
                    val clientCPA = ObjectInputStream(client.getInputStream()).readObject() as NodeCPA
                    ObjectOutputStream(client.getOutputStream()).writeObject(identityModule.getCurrentCPA())
                    return clientCPA
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro no servidor: ${e.message}")
                return null
            }
        }

        override fun onPostExecute(result: NodeCPA?) {
            result?.let {
                uiHandler.obtainMessage(MainActivity.PARTNER_CPA_UPDATE, it).sendToTarget()
            }
        }
    }

    inner class ClientTask(private val hostAddress: String) : AsyncTask<NodeCPA, Void, NodeCPA>() {
        override fun doInBackground(vararg params: NodeCPA): NodeCPA? {
            val socket = Socket()
            try {
                Log.d(TAG, "Cliente: Conectando a $hostAddress")
                socket.bind(null)
                socket.connect(InetSocketAddress(hostAddress, 8888), 5000)
                Log.d(TAG, "Cliente: Conectado.")
                ObjectOutputStream(socket.getOutputStream()).writeObject(params[0])
                return ObjectInputStream(socket.getInputStream()).readObject() as NodeCPA
            } catch (e: Exception) {
                Log.e(TAG, "Erro no cliente: ${e.message}")
                return null
            } finally {
                socket.takeIf { it.isConnected }?.close()
            }
        }

        override fun onPostExecute(result: NodeCPA?) {
            result?.let {
                uiHandler.obtainMessage(MainActivity.PARTNER_CPA_UPDATE, it).sendToTarget()
            }
        }
    }
}
