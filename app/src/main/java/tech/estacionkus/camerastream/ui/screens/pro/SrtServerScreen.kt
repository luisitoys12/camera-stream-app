package tech.estacionkus.camerastream.ui.screens.pro

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SrtServerScreen(
    onBack: () -> Unit,
    viewModel: SrtServerViewModel = hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsState()
    val clipboard = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Servidor SRT") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Server toggle
            Card { 
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(if (ui.serverRunning) "Servidor activo" else "Servidor detenido", style = MaterialTheme.typography.titleMedium)
                        Text("Puerto: ${ui.port}", style = MaterialTheme.typography.bodySmall)
                        if (ui.clientCount > 0) Text("Clientes conectados: ${ui.clientCount}")
                    }
                    Switch(checked = ui.serverRunning, onCheckedChange = { if (it) viewModel.startServer() else viewModel.stopServer() })
                }
            }

            // Tunnel toggle
            if (ui.serverRunning) {
                Card {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Túnel Cloudflared", style = MaterialTheme.typography.titleMedium)
                            Switch(checked = ui.tunnelRunning, onCheckedChange = { if (it) viewModel.startTunnel() else viewModel.stopTunnel() })
                        }
                        if (ui.tunnelUrl != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(ui.tunnelUrl!!, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                IconButton(onClick = { clipboard.setText(AnnotatedString(ui.tunnelUrl!!)) }) {
                                    Icon(Icons.Default.ContentCopy, null)
                                }
                            }
                            Text("Usa esta URL en OBS: Settings → Stream → Custom → pegar URL", style = MaterialTheme.typography.bodySmall)
                        } else if (ui.tunnelRunning) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Text("Generando URL...")
                            }
                        }
                    }
                }
            }
        }
    }
}
