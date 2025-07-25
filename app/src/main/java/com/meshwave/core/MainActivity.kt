package com.meshwave.core

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meshwave.core.ui.theme.MeshWaveCoreTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                viewModel.log("Permissão de localização concedida.")
            } else {
                viewModel.log("Permissão de localização negada.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))

        setContent {
            MeshWaveCoreTheme {
                val uiState by viewModel.uiState.collectAsState()
                MainScreen(
                    uiState = uiState,
                    onAnnounceClick = { viewModel.startAnnouncing() },
                    onDiscoverClick = { viewModel.startDiscovery() },
                    onStopClick = { viewModel.stopAllNetwork() }
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    uiState: AppUiState,
    onAnnounceClick: () -> Unit,
    onDiscoverClick: () -> Unit,
    onStopClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Header()
            Spacer(modifier = Modifier.height(16.dp))
            NetworkControls(onAnnounceClick, onDiscoverClick, onStopClick)
            Spacer(modifier = Modifier.height(16.dp))
            NetworkNodesList(uiState)
            Spacer(modifier = Modifier.height(16.dp))
            EventLog(uiState.logMessages)
        }
    }
}

@Composable
fun Header() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text("MeshWave Core", style = MaterialTheme.typography.headlineMedium)
        Text("v0.2.5-alpha", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun NetworkControls(
    onAnnounceClick: () -> Unit,
    onDiscoverClick: () -> Unit,
    onStopClick: () -> Unit
) {
    Column {
        Text("CONTROLE DE REDE", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onAnnounceClick) { Text("Anunciar") }
            Button(onClick = onDiscoverClick) { Text("Descobrir") }
            Button(onClick = onStopClick, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Stop") }
        }
    }
}

@Composable
fun NetworkNodesList(uiState: AppUiState) {
    Column {
        Text("NÓS NA REDE (${uiState.networkNodes.size})", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
        ) {
            items(uiState.networkNodes.values.toList()) { node ->
                NodeItem(node, node.did == uiState.localNodeDid)
            }
        }
    }
}

@Composable
fun NodeItem(node: NodeCPA, isLocal: Boolean) {
    val borderColor = if (isLocal) Color.Red else Color.Transparent
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .padding(4.dp)
    ) {
        Text(
            text = if (isLocal) "${node.username} (Este Dispositivo)" else node.username,
            fontWeight = FontWeight.Bold
        )
        Text(text = "ID: ...${node.did.takeLast(8)}", fontSize = 12.sp)
        Text(text = "Geohash (CLA): ${node.claGeohash}", fontSize = 12.sp)
    }
}

@Composable
fun EventLog(logMessages: List<String>) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(logMessages.size) {
        coroutineScope.launch {
            if (logMessages.isNotEmpty()) {
                listState.animateScrollToItem(logMessages.size - 1)
            }
        }
    }

    Column {
        Text("LOG DE EVENTOS", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(8.dp)
        ) {
            items(logMessages) { message ->
                Text(
                    text = message,
                    color = Color.Green,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
        }
    }
}
