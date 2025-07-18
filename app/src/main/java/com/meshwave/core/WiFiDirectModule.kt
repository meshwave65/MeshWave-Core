// Arquivo: app/src/main/java/com/meshwave/core/WiFiDirectModule.kt
package com.meshwave.core

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat

class WiFiDirectModule(private val context: Context, private val uiHandler: Handler) {

    private val wifiP2pManager: WifiP2pManager? by lazy(LazyThreadSafetyMode.NONE) {
        context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager?
    }

    private var channel: WifiP2pManager.Channel? = null
    private val receiver: BroadcastReceiver
    private val intentFilter = IntentFilter()

    private val peerListListener = WifiP2pManager.PeerListListener { peerList ->
        val refreshedPeers = peerList.deviceList
        if (refreshedPeers.isEmpty()) {
            Log.d(TAG, "Nenhum par encontrado.")
            sendToUi("Nenhum par encontrado")
            return@PeerListListener
        }

        Log.d(TAG, "${refreshedPeers.size} pares encontrados:")
        val peerNames = refreshedPeers.joinToString { it.deviceName }
        sendToUi("Pares: $peerNames")
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
            Log.e(TAG, "Serviço P2P indisponível.")
            sendToUi("Serviço P2P indisponível")
            return
        }

        if (channel != null) {
            Log.w(TAG, "Módulo já iniciado, reiniciando busca.")
            discoverPeers()
            return
        }

        // A inicialização do canal é assíncrona, o listener nos dirá quando estiver pronto.
        // O listener de 'onChannelDisconnected' é importante para a robustez.
        channel = wifiP2pManager?.initialize(context, Looper.getMainLooper()) {
            // Este listener é chamado se o canal for desconectado permanentemente.
            Log.e(TAG, "Canal do Wi-Fi P2P foi desconectado. Reinicialize o módulo.")
            sendToUi("Canal P2P perdido")
            channel = null // Limpa o canal para permitir reinicialização
        }

        if (channel == null) {
            Log.e(TAG, "Falha ao iniciar a chamada para inicializar o canal do Wi-Fi P2P.")
            sendToUi("Falha na inicialização")
            return
        }

        context.registerReceiver(receiver, intentFilter)
        Log.d(TAG, "Módulo Wi-Fi Direct iniciado e receiver registrado.")
        sendToUi("Iniciado")

        discoverPeers()
    }

    private fun discoverPeers() {
        // --- GUARDA DE SEGURANÇA ADICIONAL ---
        if (channel == null) {
            Log.e(TAG, "Não é possível buscar pares: o canal é nulo. O módulo pode não ter sido inicializado corretamente.")
            sendToUi("Erro: Canal P2P nulo")
            return
        }
        // --- FIM DA GUARDA ---

        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.NEARBY_WIFI_DEVICES, Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            listOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        val missingPermissions = requiredPermissions.filter {
            ActivityCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            val message = "Permissões faltando para buscar pares: ${missingPermissions.joinToString()}"
            Log.w(TAG, message)
            sendToUi(message)
            return
        }

        wifiP2pManager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Comando para buscar pares enviado com sucesso.")
                sendToUi("Buscando pares...")
            }

            override fun onFailure(reasonCode: Int) {
                val errorMsg = when(reasonCode) {
                    WifiP2pManager.P2P_UNSUPPORTED -> "P2P não é suportado neste dispositivo."
                    WifiP2pManager.ERROR -> "Erro interno do framework P2P."
                    WifiP2pManager.BUSY -> "Sistema P2P ocupado, tente novamente."
                    else -> "Código de erro desconhecido: $reasonCode"
                }
                Log.e(TAG, "Falha ao iniciar busca por pares: $errorMsg")
                sendToUi("Erro ao buscar: $errorMsg")
            }
        })
    }

    fun stop() {
        if (channel != null) {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Receiver não estava registrado.", e)
            }
            channel = null
            Log.d(TAG, "Módulo Wi-Fi Direct parado.")
            sendToUi("Parado")
        }
    }

    private fun sendToUi(statusMessage: String) {
        val message = uiHandler.obtainMessage(MainActivity.WIFI_STATUS_UPDATE, statusMessage)
        message.sendToTarget()
    }

    inner class WiFiDirectBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                    if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                        Log.d(TAG, "Wi-Fi P2P está Habilitado")
                        sendToUi("Rádio Habilitado")
                    } else {
                        Log.d(TAG, "Wi-Fi P2P está Desabilitado")
                        sendToUi("Rádio Desabilitado")
                        channel = null // Se o rádio for desligado, o canal se torna inválido
                    }
                }
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    if (channel != null) { // Verifica se o canal ainda é válido
                        Log.d(TAG, "Lista de pares mudou. Solicitando atualização...")
                        wifiP2pManager?.requestPeers(channel, peerListListener)
                    }
                }
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    Log.d(TAG, "Status da conexão mudou.")
                }
                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    // Nada a fazer por enquanto
                }
            }
        }
    }
}
