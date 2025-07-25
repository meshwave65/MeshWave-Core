package com.meshwave.core

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
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
    private val identityModule = IdentityModule(cacheModule)
    private val locationModule = LocationModule(application)

    private val networkManager: NetworkManager by lazy {
        NetworkManager(
            context = getApplication(),
            viewModel = this,
            scope = viewModelScope,
            getInitialCpa = { identityModule.getLocalNode() }
        )
    }

    private var locationCycleJob: Job? = null

    init {
        viewModelScope.launch {
            log("ViewModel iniciado. Carregando do cache...")
            identityModule.initialize()
            val syncedNodes = cacheModule.getSyncedNodes()
            _uiState.update { it.copy(networkNodes = syncedNodes) }
            updateLocalNodeInState()
            log("Nó local inicializado. Cache sincronizado contém ${syncedNodes.size} nós.")
            startLocationUpdateCycle()
        }
    }

    private fun startLocationUpdateCycle() {
        locationCycleJob?.cancel()
        locationCycleJob = viewModelScope.launch {
            while (true) {
                log("Ciclo de localização aguardando...")
                locationModule.fetchLocationUpdates()
                    .catch { e ->
                        log("Falha no ciclo de localização: ${e.message}")
                        identityModule.updateCpaStatus(LocationStatus.FAILED)
                        updateLocalNodeInState()
                    }
                    .collect { locationData ->
                        val currentNode = identityModule.getLocalNode()
                        val b1 = currentNode.claGeohash
                        val b2 = locationData.claGeohash
                        log("Localização recebida: ${locationData.claGeohash}")

                        if (b1 != b2) {
                            log("Geohash alterado. Atualizando sistema...")
                            identityModule.updateCpaWithLocation(locationData)
                            val updatedNode = identityModule.getLocalNode()
                            updateLocalNodeInState()
                            networkManager.updateNodeCpa(updatedNode)
                        } else {
                            log("Geohash inalterado. Nenhuma atualização necessária.")
                        }
                    }
                delay(15_000)
            }
        }
    }

    private fun updateLocalNodeInState() {
        val localNode = identityModule.getLocalNode()
        _uiState.update { currentState ->
            val newNodes = currentState.networkNodes.toMutableMap()
            newNodes[localNode.did] = localNode
            currentState.copy(
                networkNodes = newNodes,
                localNodeDid = localNode.did
            )
        }
    }

    fun startAnnouncing() {
        log("Iniciando anúncio e descoberta...")
        networkManager.startAnnouncing()
        networkManager.startDiscovery()
    }

    fun startDiscovery() {
        log("Comando de descoberta recebido. A descoberta agora é iniciada com o anúncio.")
        networkManager.startDiscovery()
    }

    fun stopAllNetwork() {
        networkManager.stopAll()
    }

    fun log(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val logMessage = "[$timestamp] $message"
        _uiState.update {
            it.copy(logMessages = (it.logMessages + logMessage).takeLast(100))
        }
    }

    suspend fun onNodeDiscovered(discoveredNode: NodeCPA) {
        var isNew = false
        _uiState.update { currentState ->
            if (currentState.networkNodes[discoveredNode.did] != discoveredNode) {
                isNew = true
                val newNodes = currentState.networkNodes.toMutableMap()
                newNodes[discoveredNode.did] = discoveredNode
                currentState.copy(networkNodes = newNodes)
            } else {
                currentState
            }
        }

        if (isNew) {
            log("Nó descoberto/atualizado: ${discoveredNode.username}")
            cacheModule.saveSyncedNodes(_uiState.value.networkNodes)
        }
    }

    suspend fun onNodeLost(serviceName: String) {
        val username = serviceName.removePrefix("${NetworkManager.SERVICE_NAME_PREFIX}-")
        var removedNode: NodeCPA? = null
        _uiState.update { currentState ->
            val nodeToFind = currentState.networkNodes.values.find { it.username == username }
            if (nodeToFind != null) {
                removedNode = nodeToFind
                val newNodes = currentState.networkNodes.toMutableMap()
                newNodes.remove(nodeToFind.did)
                currentState.copy(networkNodes = newNodes)
            } else {
                currentState
            }
        }

        if (removedNode != null) {
            log("Nó perdido: ${removedNode!!.username}")
            cacheModule.saveSyncedNodes(_uiState.value.networkNodes)
        }
    }

    override fun onCleared() {
        super.onCleared()
        networkManager.stopAll()
        locationCycleJob?.cancel()
    }
}
