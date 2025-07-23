package com.meshwave.core

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Enum para um controle de estado mais limpo
enum class ConnectionState(val displayText: String) {
    INITIALIZING("Inicializando..."),
    DISCONNECTED("Desconectado"),
    SEARCHING("Buscando..."),
    CONNECTING("Conectando..."),
    CONNECTED("Conectado"),
    FAILED("Falha na Conexão")
}

// --- SEU AppUiState MODIFICADO ---
// Trocamos 'localNodeCPA' por 'networkNodes' e 'localNodeDid'
data class AppUiState(
    val connectionState: ConnectionState = ConnectionState.INITIALIZING, // Usando o Enum
    val logMessages: List<String> = emptyList(),
    val networkNodes: Map<String, NodeCPA> = emptyMap(), // Chave: did (String)
    val localNodeDid: String? = null
)

@SuppressLint("MissingPermission")
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    // Seus módulos existentes
    private val locationModule = LocationModule(application)
    // Supondo que IdentityModule seja um singleton ou tenha um método 'getInstance'
    // private val identityModule = IdentityModule.getInstance()

    init {
        log("ViewModel iniciado.")
        initializeLocalNode()
        startLocationUpdates()
    }

    private fun initializeLocalNode() {
        // Mantém sua lógica original de usar o IdentityModule
        val localNode = IdentityModule.getOrCreateCurrentNodeCPA()
        _uiState.update {
            it.copy(
                // Adiciona o nó local ao mapa de nós da rede
                networkNodes = mapOf(localNode.did to localNode),
                localNodeDid = localNode.did
            )
        }
        log("Nó local inicializado: ${localNode.username}")
    }

    private fun startLocationUpdates() {
        log("Solicitando localização...")
        viewModelScope.launch {
            locationModule.fetchLocationUpdates()
                .catch { e ->
                    // Em caso de erro no fluxo (ex: permissão negada)
                    log("Falha ao obter localização: ${e.message}")
                    IdentityModule.markCpaLocationAsFailed()
                    updateLocalNodeInState()
                }
                .collect { locationData ->
                    // Acessa o IdentityModule como um singleton
                    IdentityModule.updateCpaWithLocation(locationData)
                    log("Localização: ${locationData.claGeohash}")
                    updateLocalNodeInState()
                }
        }
    }

    /**
     * Função auxiliar para pegar o NodeCPA atualizado do IdentityModule
     * e sincronizá-lo com o nosso mapa de nós no AppUiState.
     */
    private fun updateLocalNodeInState() {
        val updatedLocalNode = IdentityModule.getCurrentNodeCPA() ?: return
        _uiState.update { currentState ->
            val newNodes = currentState.networkNodes.toMutableMap()
            newNodes[updatedLocalNode.did] = updatedLocalNode
            currentState.copy(networkNodes = newNodes)
        }
    }

    fun log(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val logMessage = "[$timestamp] $message"
        _uiState.update {
            it.copy(logMessages = (it.logMessages + logMessage).takeLast(100))
        }
    }

    // Função para a UI atualizar o status da conexão
    fun updateConnectionState(state: ConnectionState) {
        _uiState.update { it.copy(connectionState = state) }
    }
}
