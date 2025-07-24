package com.meshwave.core

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meshwave.core.ui.theme.MeshWaveCoreTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionManager = PermissionManager(this)

        setContent {
            MeshWaveCoreTheme {
                // Efeito que roda uma vez para verificar e pedir permissão se necessário.
                LaunchedEffect(key1 = true) {
                    if (!permissionManager.hasLocationPermission()) {
                        viewModel.log("Permissão de localização não encontrada. Solicitando...")
                        permissionManager.requestLocationPermission { allGranted ->
                            if (allGranted) {
                                viewModel.log("Permissão concedida pelo usuário.")
                            } else {
                                viewModel.log("Permissão negada pelo usuário.")
                            }
                        }
                    } else {
                        viewModel.log("Permissão de localização já concedida.")
                    }
                }

                val state by viewModel.uiState.collectAsState()
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Scaffold(
                        topBar = { AppTopBar() }
                    ) { paddingValues ->
                        MainScreen(
                            modifier = Modifier.padding(paddingValues),
                            state = state
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar() {
    val appVersion = BuildConfig.APP_VERSION_NAME
    TopAppBar(
        title = { Text("MeshWave Core", fontWeight = FontWeight.Bold) },
        actions = {
            Text(
                text = "v$appVersion",
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(end = 16.dp)
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun MainScreen(modifier: Modifier = Modifier, state: AppUiState) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("NÓS NA REDE (${state.networkNodes.size})", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
            items(state.networkNodes.values.toList()) { node ->
                NodeInfoCard(
                    node = node,
                    isLocalNode = (node.did == state.localNodeDid)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text("LOG DE EVENTOS", style = MaterialTheme.typography.titleMedium)
        val scrollState = rememberLazyListState()
        LaunchedEffect(state.logMessages.size) {
            if (state.logMessages.isNotEmpty()) {
                scrollState.animateScrollToItem(state.logMessages.size - 1)
            }
        }
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            items(state.logMessages) { msg ->
                Text(
                    text = msg,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun NodeInfoCard(node: NodeCPA, isLocalNode: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = if (isLocalNode) BorderStroke(2.dp, node.locationStatus.toColor()) else null,
        colors = CardDefaults.cardColors(containerColor = node.locationStatus.toColor().copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${node.username} (${if (isLocalNode) "Este Dispositivo" else "Remoto"})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text("ID: ...${node.did.takeLast(8)}", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Text("Geohash (CLA): ${node.claGeohash}")
        }
    }
}
