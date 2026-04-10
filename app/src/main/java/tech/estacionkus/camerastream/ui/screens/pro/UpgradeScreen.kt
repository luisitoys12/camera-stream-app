package tech.estacionkus.camerastream.ui.screens.pro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import tech.estacionkus.camerastream.ui.theme.*

@Composable
fun UpgradeScreen(onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface900)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = OnSurface) }
                Text("Planes CameraStream", style = MaterialTheme.typography.titleLarge, color = OnSurface)
            }

            // Starter
            PlanCard(
                name = "Starter",
                price = "\$9.99/mes",
                color = Color(0xFF1565C0),
                features = listOf(
                    "2 plataformas RTMP simultáneas",
                    "SRT caller (saliente)",
                    "720p 30fps",
                    "Overlays de video nativos",
                    "Grabación local",
                    "Soporte por email"
                ),
                onSubscribe = { uriHandler.openUri("https://buy.stripe.com/9B67sL9YIanMbK4eGcdAk00") }
            )

            // Pro
            PlanCard(
                name = "Pro",
                price = "\$24.99/mes",
                color = CameraRed,
                highlighted = true,
                features = listOf(
                    "5 plataformas RTMP simultáneas",
                    "Servidor SRT local + Túnel Cloudflared",
                    "SRTLA bonding (WiFi + datos)",
                    "1080p 60fps",
                    "Escenas múltiples (como OBS)",
                    "Chroma key",
                    "Cámara manual (ISO, WB, zoom)",
                    "Multi-chat Twitch + Kick",
                    "Control remoto OBS WebSocket",
                    "QR compartir URL instantáneo",
                    "Modo webcam para OBS/PC",
                    "Grabación 1080p con overlays",
                    "Soporte prioritario"
                ),
                onSubscribe = { uriHandler.openUri("https://buy.stripe.com/3cI8wPgn667w6pKfKgdAk01") }
            )

            // Agency
            PlanCard(
                name = "Agency",
                price = "\$79.99/mes",
                color = Color(0xFF6A1B9A),
                features = listOf(
                    "Plataformas RTMP ilimitadas",
                    "Todo lo de Pro",
                    "Branding personalizado",
                    "API access",
                    "SLA 99.9%",
                    "Soporte dedicado"
                ),
                onSubscribe = { uriHandler.openUri("https://buy.stripe.com/00wbJ17QA0NcbK4eGcdAk02") }
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PlanCard(
    name: String,
    price: String,
    color: Color,
    highlighted: Boolean = false,
    features: List<String>,
    onSubscribe: () -> Unit
) {
    Surface(
        color = if (highlighted) color.copy(alpha = 0.15f) else Surface700,
        shape = RoundedCornerShape(16.dp),
        border = if (highlighted) androidx.compose.foundation.BorderStroke(2.dp, color) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    if (highlighted) {
                        Surface(color = color, shape = RoundedCornerShape(4.dp)) {
                            Text("MÁS POPULAR", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                    Text(name, color = OnSurface, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Text(price, color = color, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            features.forEach { f ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Check, null, tint = color, modifier = Modifier.size(16.dp))
                    Text(f, color = OnSurface, fontSize = 13.sp)
                }
            }

            Button(
                onClick = onSubscribe,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = color)
            ) {
                Text("Suscribirse — $price", fontWeight = FontWeight.Bold)
            }
        }
    }
}
