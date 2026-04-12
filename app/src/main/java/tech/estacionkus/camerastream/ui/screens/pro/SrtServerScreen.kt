package tech.estacionkus.camerastream.ui.screens.pro

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SrtServerScreen(
    onBack: () -> Unit,
    viewModel: SrtServerViewModel = hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsState()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var customRelayUrl by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SRT Server") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Server Control
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2A1A)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Wifi, null, tint = Color(0xFF4CAF50))
                        Text("SRT Server", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Text(
                        "Start local SRT server for receiving video. Viewers connect directly via LAN or port-forwarded address.",
                        fontSize = 13.sp, color = Color(0xFFBBBBBB)
                    )
                    Button(
                        onClick = { if (ui.serverRunning) viewModel.stopServer() else viewModel.startServer() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (ui.serverRunning) Color(0xFFE53935) else Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            if (ui.serverRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                            null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (ui.serverRunning) "Stop SRT Server" else "Start SRT Server")
                    }
                }
            }

            // Server Status
            Card(shape = RoundedCornerShape(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            if (ui.serverRunning) "Server Active" else "Server Stopped",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (ui.serverRunning) Color(0xFF4CAF50) else Color.Gray
                        )
                        Text("Port: ${ui.port}", style = MaterialTheme.typography.bodySmall)
                        if (ui.clientCount > 0) {
                            Text("Clients: ${ui.clientCount}", color = Color(0xFF4CAF50))
                        }
                        if (ui.incomingBitrateKbps > 0) {
                            Text("Bitrate: ${ui.incomingBitrateKbps} kbps", fontSize = 12.sp)
                        }
                    }
                    Icon(
                        if (ui.serverRunning) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        null,
                        tint = if (ui.serverRunning) Color(0xFF4CAF50) else Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Local URL - always shown when server running
            if (ui.serverRunning) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Local URL (LAN)", fontWeight = FontWeight.Bold, color = Color.White)
                        val localUrl = ui.localSrtUrl ?: "srt://192.168.x.x:${ui.port}"
                        Text(
                            localUrl,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF81C784)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { clipboard.setText(AnnotatedString(localUrl)) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Copy")
                            }
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, "SRT URL: $localUrl")
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
                    }
                }

                // Public URL (tunnel) section
                if (ui.tunnelUrl != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Public URL (Tunnel)", fontWeight = FontWeight.Bold, color = Color.White)
                            Text(
                                ui.tunnelUrl!!,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF64B5F6)
                            )
                            OutlinedButton(
                                onClick = { clipboard.setText(AnnotatedString(ui.tunnelUrl!!)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Copy Public URL")
                            }
                        }
                    }
                }
            }

            // Custom SRT Relay option
            Card(shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Custom SRT Relay", fontWeight = FontWeight.Bold)
                    Text(
                        "Connect to your own SRT relay server (e.g., SRT Live Server, Nimble Streamer).",
                        fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = customRelayUrl,
                        onValueChange = { customRelayUrl = it },
                        label = { Text("SRT Relay URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("srt://relay.example.com:9999") }
                    )
                    Button(
                        onClick = { viewModel.connectToRelay(customRelayUrl) },
                        enabled = customRelayUrl.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF607D8B)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Connect to Relay")
                    }
                }
            }

            // SRT UDP Tunnel Warning
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = Color(0xFFE65100), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("SRT requires UDP", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                    }
                    Text(
                        "Cloudflare tunnels do NOT support UDP, which SRT needs. For remote SRT access, use one of these alternatives:",
                        fontSize = 13.sp, color = Color(0xFF4E342E)
                    )
                    Text("• Pinggy (\$2.50/mo) — UDP tunnel support", fontSize = 12.sp, color = Color(0xFF4E342E))
                    Text("• Playit.gg (free) — Free UDP tunnels for gaming/streaming", fontSize = 12.sp, color = Color(0xFF4E342E))
                    Text("• VPS with public IP (DigitalOcean, Hetzner, Linode)", fontSize = 12.sp, color = Color(0xFF4E342E))
                    Text("• Port forwarding on your router (port ${ui.port} UDP)", fontSize = 12.sp, color = Color(0xFF4E342E))
                }
            }

            // Instructions
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("How to connect", fontWeight = FontWeight.Bold)
                    Text("1. Start the SRT server above", fontSize = 13.sp)
                    Text("2. On OBS/VLC: Settings > Stream > Custom", fontSize = 13.sp)
                    Text("3. Paste the Local URL (same network) or Public URL", fontSize = 13.sp)
                    Text("4. For remote access: use a UDP tunnel or port forwarding on port ${ui.port}", fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
