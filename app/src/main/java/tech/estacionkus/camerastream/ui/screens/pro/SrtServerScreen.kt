package tech.estacionkus.camerastream.ui.screens.pro

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.estacionkus.camerastream.streaming.TunnelStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SrtServerScreen(
    onBack: () -> Unit,
    viewModel: SrtServerViewModel = hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsState()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SRT Server") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Auto-setup card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2A1A)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFF4CAF50))
                        Text("Auto-Setup", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Text(
                        "Start SRT server and Cloudflare tunnel automatically. Get a shareable URL in seconds.",
                        fontSize = 13.sp, color = Color(0xFFBBBBBB)
                    )
                    Button(
                        onClick = { viewModel.startAutoSetup() },
                        enabled = !ui.serverRunning && ui.tunnelStatus != TunnelStatus.DOWNLOADING,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.RocketLaunch, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Start SRT + Tunnel")
                    }
                }
            }

            // Server status
            Card(shape = RoundedCornerShape(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            if (ui.serverRunning) "Server Active" else "Server Stopped",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("Port: ${ui.port}", style = MaterialTheme.typography.bodySmall)
                        if (ui.clientCount > 0) {
                            Text("Clients: ${ui.clientCount}", color = Color(0xFF4CAF50))
                        }
                        if (ui.incomingBitrateKbps > 0) {
                            Text("Bitrate: ${ui.incomingBitrateKbps} kbps", fontSize = 12.sp)
                        }
                    }
                    Switch(
                        checked = ui.serverRunning,
                        onCheckedChange = { if (it) viewModel.startServer() else viewModel.stopServer() }
                    )
                }
            }

            // Tunnel status
            Card(shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Cloudflare Tunnel", style = MaterialTheme.typography.titleMedium)
                            Text(
                                when (ui.tunnelStatus) {
                                    TunnelStatus.IDLE -> "Not running"
                                    TunnelStatus.DOWNLOADING -> "Downloading binary..."
                                    TunnelStatus.STARTING -> "Starting..."
                                    TunnelStatus.ACTIVE -> "Active"
                                    TunnelStatus.ERROR -> "Error"
                                },
                                color = when (ui.tunnelStatus) {
                                    TunnelStatus.ACTIVE -> Color(0xFF4CAF50)
                                    TunnelStatus.ERROR -> Color.Red
                                    TunnelStatus.DOWNLOADING, TunnelStatus.STARTING -> Color(0xFFFFC107)
                                    else -> Color.Gray
                                },
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = ui.tunnelRunning,
                            onCheckedChange = { if (it) viewModel.startTunnel() else viewModel.stopTunnel() },
                            enabled = ui.serverRunning
                        )
                    }

                    if (ui.tunnelStatus == TunnelStatus.DOWNLOADING) {
                        LinearProgressIndicator(
                            progress = { ui.downloadProgress },
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF4CAF50)
                        )
                        Text("${(ui.downloadProgress * 100).toInt()}%", fontSize = 12.sp)
                    }

                    if (ui.tunnelStatusMessage.isNotBlank()) {
                        Text(ui.tunnelStatusMessage, fontSize = 12.sp, color = Color(0xFF888888))
                    }
                }
            }

            // Shareable URL
            if (ui.tunnelUrl != null || ui.srtUrl != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Shareable URLs", fontWeight = FontWeight.Bold)

                        if (ui.tunnelUrl != null) {
                            Text("Tunnel URL:", fontSize = 12.sp, color = Color.Gray)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(ui.tunnelUrl!!, fontSize = 12.sp, modifier = Modifier.weight(1f), color = Color(0xFF64B5F6))
                                IconButton(onClick = { clipboard.setText(AnnotatedString(ui.tunnelUrl!!)) }) {
                                    Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        if (ui.srtUrl != null) {
                            Text("SRT URL:", fontSize = 12.sp, color = Color.Gray)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(ui.srtUrl!!, fontSize = 12.sp, modifier = Modifier.weight(1f), color = Color(0xFF81C784))
                                IconButton(onClick = { clipboard.setText(AnnotatedString(ui.srtUrl!!)) }) {
                                    Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val shareText = buildString {
                                        append("CameraStream SRT Server\n")
                                        ui.tunnelUrl?.let { append("Tunnel: $it\n") }
                                        ui.srtUrl?.let { append("SRT: $it\n") }
                                    }
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share SRT URL"))
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Share")
                            }
                        }

                        Text(
                            "Use this URL in OBS: Settings > Stream > Custom, then paste the URL",
                            fontSize = 11.sp, color = Color(0xFF888888)
                        )
                    }
                }
            }
        }
    }
}
