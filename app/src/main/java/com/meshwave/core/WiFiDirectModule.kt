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
    private val discoveredPeers = mutableListOf<WifiP2pDevice>()
    private var myDeviceAddress: String? = null

    private val connectionInfoListener = WifiP2pManager.ConnectionInfoListener { info ->
        if (info.groupFormed && info.isGroupOwner) {
            sendToUi("Conectado como Servidor")
            ServerTask().execute()
        } else if (info.groupFormed) {
            sendToUi("Conectado como Cliente")
            ClientTask(info.groupOwnerAddress?.hostAddress).execute(identityModule.getCurrentCPA())
        }
    }

    private val peerListListener = WifiP2pManager.PeerListListener { peerList ->
        discoveredPeers.clear()
        discoveredPeers.addAll(peerList.deviceList)

        if (discoveredPeers.isEmpty()) {
            Log.d(TAG, "Nenhum par encontrado.")
            return@PeerListListener
        }

        val peerNames = discoveredPeers.joinToString { it.deviceName }
        sendToUi("Pares visíveis: $peerNames")

        if (!isConnected && discoveredPeers.isNotEmpty()) {
            val partner = discoveredPeers.first()
            if (myDeviceAddress != null && partner.deviceAddress != null && myDeviceAddress!! > partner.deviceAddress) {
                Log.d(TAG, "Nosso endereço ($myDeviceAddress) é maior. Iniciando conexão com ${partner.deviceName}")
                connectToDevice(partner)
            } else {
                Log.d(TAG, "Endereço do parceiro (${partner.deviceAddress}) é maior ou igual. Aguardando convite.")
            }
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
        if (channel != null) return

        channel = wifiP2pManager?.initialize(context, Looper.getMainLooper()) {
            channel = null
            isConnected = false
            sendToUi("Canal P2P perdido")
        }

        if (channel == null) {
            sendToUi("Falha na inicialização")
            return
        }

        context.registerReceiver(receiver, intentFilter)
        createGroupAndDiscover()
    }

    fun stop() {
        removeCurrentGroup {
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
    }

    private fun createGroupAndDiscover() {
        removeCurrentGroup {
            createGroup {
                discoverPeers()
            }
        }
    }

    private fun discoverPeers() {
        if (channel == null) return
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return
        wifiP2pManager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                sendToUi("Buscando pares...")
            }

            override fun onFailure(reason: Int) {
                sendToUi("Erro ao buscar: $reason")
                Handler(Looper.getMainLooper()).postDelayed({
                    createGroupAndDiscover()
                }, 2000)
            }
        })
    }

    private fun createGroup(onSuccess: (() -> Unit)? = null) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return
        wifiP2pManager?.createGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Grupo criado, agora visível como Servidor.")
                sendToUi("Visível como Servidor")
                onSuccess?.invoke()
            }

            override fun onFailure(reason: Int) {
                Log.e(TAG, "Falha ao criar grupo: $reason")
            }
        })
    }

    private fun connectToDevice(device: WifiP2pDevice) {
        if (isConnected) {
            Log.d(TAG, "Já conectado, ignorando nova tentativa de conexão.")
            return
        }

        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
            groupOwnerIntent = 0
        }

        removeCurrentGroup {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return@removeCurrentGroup

            wifiP2pManager?.connect(channel, config, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    sendToUi("Convite enviado para ${device.deviceName}")
                }

                override fun onFailure(reason: Int) {
                    sendToUi("Falha ao conectar: $reason")
                    Handler(Looper.getMainLooper()).postDelayed({
                        createGroupAndDiscover()
                    }, 2000)
                }
            })
        }
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

    private fun sendToUi(statusMessage: String) {
        val message = uiHandler.obtainMessage(MainActivity.WIFI_STATUS_UPDATE, statusMessage)
        uiHandler.sendMessage(message)
    }

    inner class WiFiDirectBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (val action = intent.action) {
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    val networkInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO, android.net.NetworkInfo::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO)
                    }
                    if (networkInfo?.isConnected == true) {
                        if (!isConnected) {
                            isConnected = true
                            wifiP2pManager?.requestConnectionInfo(channel, connectionInfoListener)
                        }
                    } else {
                        if (isConnected) {
                            isConnected = false
                            sendToUi("Desconectado.")
                            createGroupAndDiscover()
                        }
                    }
                }
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    if (channel != null) wifiP2pManager?.requestPeers(channel, peerListListener)
                }
                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE, WifiP2pDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)
                    }
                    device?.let { myDeviceAddress = it.deviceAddress }
                }
            }
        }
    }

    inner class ServerTask : AsyncTask<Void, Void, NodeCPA>() {
        override fun doInBackground(vararg params: Void?): NodeCPA? {
            try {
                ServerSocket(8888).use { serverSocket ->
                    val client = serverSocket.accept()
                    val input = ObjectInputStream(client.getInputStream())
                    val clientCPA = input.readObject() as? NodeCPA
                    val output = ObjectOutputStream(client.getOutputStream())
                    output.writeObject(identityModule.getCurrentCPA())
                    output.flush()
                    return clientCPA
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro no servidor: ${e.message}", e)
                return null
            }
        }
        override fun onPostExecute(result: NodeCPA?) {
            result?.let {
                uiHandler.obtainMessage(MainActivity.PARTNER_CPA_UPDATE, it).sendToTarget()
            }
        }
    }

    inner class ClientTask(private val hostAddress: String?) : AsyncTask<NodeCPA, Void, NodeCPA>() {
        override fun doInBackground(vararg params: NodeCPA?): NodeCPA? {
            if (hostAddress == null || params.isEmpty() || params[0] == null) return null
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                Log.e(TAG, "Atraso do ClientTask interrompido.", e)
                return null
            }
            val socket = Socket()
            try {
                socket.bind(null)
                socket.connect(InetSocketAddress(hostAddress, 8888), 5000)
                val output = ObjectOutputStream(socket.getOutputStream())
                output.writeObject(params[0])
                output.flush()
                val input = ObjectInputStream(socket.getInputStream())
                return input.readObject() as? NodeCPA
            } catch (e: Exception) {
                Log.e(TAG, "Erro no cliente: ${e.message}", e)
                return null
            } finally {
                if (socket.isConnected) socket.close()
            }
        }
        override fun onPostExecute(result: NodeCPA?) {
            result?.let {
                uiHandler.obtainMessage(MainActivity.PARTNER_CPA_UPDATE, it).sendToTarget()
            } ?: sendToUi("Falha na sincronização")
        }
    }
}
