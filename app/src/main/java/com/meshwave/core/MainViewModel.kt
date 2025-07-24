package com.meshwave.core

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class AppUiState(
    val logMessages: List<String> = emptyList(),
    val networkNodes: Map<String, NodeCPA> = emptyMap(),
    val localNodeDid: String? = null
)

@SuppressLint("MissingPermission")
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private val cacheModule = CacheModule(application)
    // A instância local da classe IdentityModule.
    private val identityModule = IdentityModule(cacheModule)
    private val locationModule = LocationModule(application)

    private val TTL1_STALE_MS = 30_000L
    private val TTL2_UNRELIABLE_MS = 60_000L

    init {
        viewModelScope.launch {
            log("ViewModel iniciado. Carregando do cache...")
            // CORRIGIDO: Usa a instância local 'identityModule'
            identityModule.initialize()
            initializeLocalNodeUi()
            log("Nó local inicializado a partir do cache.")

            startLocationUpdateCycle()
            startTtlCheckCycle()
        }
    }

    private fun initializeLocalNodeUi() {
        // CORRIGIDO: Usa a instância local 'identityModule'
        val localNode = identityModule.getOrCreateCurrentNodeCPA()
        _uiState.update {
            it.copy(
                networkNodes = mapOf(localNode.did to localNode),
                localNodeDid = localNode.did
            )
        }
    }

    private fun startLocationUpdateCycle() {
        viewModelScope.launch {
            while(true) {
                log("Tentando obter localização...")
                locationModule.fetchLocationUpdates()
                    .catch { e ->
                        log("Falha ao obter localização: ${e.message}")
                        // CORRIGIDO: Usa a instância local 'identityModule'
                        identityModule.updateCpaStatus(LocationStatus.FAILED)
                    }
                    .collect { locationData ->
                        // CORRIGIDO: Usa a instância local 'identityModule'
                        identityModule.updateCpaWithLocation(locationData)
                        log("Localização recebida: ${locationData.claGeohash}")
                    }
                delay(10_000)
            }
        }
    }

    private fun startTtlCheckCycle() {
        viewModelScope.launch {
            while (true) {
                // CORRIGIDO: Usa a instância local 'identityModule'
                val node = identityModule.getOrCreateCurrentNodeCPA()
                val previousStatus = node.locationStatus
                var newStatus = previousStatus

                if (node.locationStatus == LocationStatus.UPDATED && (System.currentTimeMillis() - node.locationTimestamp > TTL1_STALE_MS)) {
                    newStatus = LocationStatus.STALE
                    log("TTL1 atingido. Status: UPDATED -> STALE")
                } else if (node.locationStatus == LocationStatus.STALE && (System.currentTimeMillis() - node.locationTimestamp > TTL2_UNRELIABLE_MS)) {
                    newStatus = LocationStatus.UNRELIABLE
                    log("TTL2 atingido. Status: STALE -> UNRELIABLE")
                }

                if (newStatus != previousStatus) {
                    // CORRIGIDO: Usa a instância local 'identityModule'
                    identityModule.updateCpaStatus(newStatus)
                }

                updateUiFromIdentityModule()
                delay(5_000)
            }
        }
    }

    private fun updateUiFromIdentityModule() {
        // CORRIGIDO: Usa a instância local 'identityModule'
        val updatedLocalNode = identityModule.getOrCreateCurrentNodeCPA().copy()
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
}
