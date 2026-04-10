package tech.estacionkus.camerastream.ui.screens.pro

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import tech.estacionkus.camerastream.ui.theme.*

@Composable
fun SrtServerScreen(
    onBack: () -> Unit,
    viewModel: SrtServerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
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
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status card
            Surface(color = if (uiState.serverRunning) Color(0xFF1B5E20) else Surface700, shape = RoundedCornerShape(12.dp)) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            if (uiState.serverRunning) "Servidor activo" else "Servidor detenido",
                            color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Text("Puerto: ${uiState.port}", color = Color.White.copy(0.7f), fontSize = 12.sp)
                        if (uiState.clientCount > 0)
                            Text("Clientes: ${uiState.clientCount}", color = Color(0xFF81C784), fontSize = 12.sp)
                    }
                    Switch(
                        checked = uiState.serverRunning,
                        onCheckedChange = { if (it) viewModel.startServer() else viewModel.stopServer() }
                    )
                }
            }

            // Tunnel card
            if (uiState.serverRunning) {
                Surface(color = Surface700, shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()) {
                            Text("Túnel Cloudflared", color = OnSurface)
                            Switch(
                                checked = uiState.tunnelRunning,
                                onCheckedChange = { if (it) viewModel.startTunnel() else viewModel.stopTunnel() }
                            )
                        }

                        if (uiState.tunnelUrl != null) {
                            Text("URL del túnel:", color = OnSurfaceMuted, fontSize = 11.sp)
                            Surface(color = Surface600, shape = RoundedCornerShape(6.dp)) {
                                Row(
                                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        uiState.tunnelUrl,
                                        color = Color(0xFF81D4FA),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = { clipboard.setText(AnnotatedString(uiState.tunnelUrl)) }) {
                                        Icon(Icons.Default.ContentCopy, null, tint = OnSurfaceMuted, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            // QR Code
                            val qrBitmap = remember(uiState.tunnelUrl) { generateQr(uiState.tunnelUrl) }
                            if (qrBitmap != null) {
                                Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                                    Image(
                                        bitmap = qrBitmap.asImageBitmap(),
                                        contentDescription = "QR",
                                        modifier = Modifier.size(180.dp)
                                    )
                                }
                            }

                            Text(
                                "Conectáte desde OBS: Settings → Stream → Service: Custom → URL de arriba",
                                color = OnSurfaceMuted, fontSize = 11.sp
                            )
                        } else if (uiState.tunnelRunning) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Text("Generando URL...", color = OnSurfaceMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Incoming stats
                if (uiState.incomingBitrateKbps > 0) {
                    Surface(color = Surface700, shape = RoundedCornerShape(12.dp)) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${uiState.incomingBitrateKbps} kbps",
                                    color = Color(0xFF4CAF50), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                Text("Bitrate entrante", color = OnSurfaceMuted, fontSize = 10.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${uiState.clientCount}", color = OnSurface,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                Text("Clientes", color = OnSurfaceMuted, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun generateQr(text: String): Bitmap? {
    return try {
        val bits = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 512, 512)
        val bmp = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
        for (x in 0 until 512) for (y in 0 until 512)
            bmp.setPixel(x, y, if (bits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        bmp
    } catch (_: Exception) { null }
}
