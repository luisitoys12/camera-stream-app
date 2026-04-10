package tech.estacionkus.camerastream.ui.screens.pro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun UpgradeScreen(onBack: () -> Unit) {
    val uri = LocalUriHandler.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Planes") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            PlanCard("Starter", "$9.99/mes", Color(0xFF1565C0),
                listOf("2 plataformas RTMP", "SRT caller", "720p", "Overlays de video", "Grabación local"),
                onClick = { uri.openUri("https://buy.stripe.com/9B67sL9YIanMbK4eGcdAk00") })
            PlanCard("Pro", "$24.99/mes", Color(0xFFE53935), true,
                listOf("5 plataformas RTMP", "Servidor SRT + Cloudflared", "1080p 60fps", "Escenas (como OBS)", "Chroma key", "Cámara manual", "Multi-chat Twitch+Kick", "QR compartir URL", "Modo webcam para OBS"),
                onClick = { uri.openUri("https://buy.stripe.com/3cI8wPgn667w6pKfKgdAk01") })
            PlanCard("Agency", "$79.99/mes", Color(0xFF6A1B9A),
                listOf("Plataformas ilimitadas", "Todo Pro", "Branding personalizado", "API access", "Soporte dedicado"),
                onClick = { uri.openUri("https://buy.stripe.com/00wbJ17QA0NcbK4eGcdAk02") })
        }
    }
}

@Composable
private fun PlanCard(name: String, price: String, color: Color, highlighted: Boolean = false, features: List<String>, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(price, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            if (highlighted) Surface(color = color, shape = MaterialTheme.shapes.extraSmall) {
                Text("MÁS POPULAR", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            features.forEach { f ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, null, tint = color, modifier = Modifier.size(14.dp))
                    Text(f, fontSize = 13.sp)
                }
            }
            Button(onClick = onClick, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = color)) {
                Text("Suscribirse")
            }
        }
    }
}
