package tech.estacionkus.camerastream.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onStartStream: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenMedia: () -> Unit,
    onUpgrade: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CameraStream") },
                actions = {
                    IconButton(onClick = onUpgrade) { Icon(Icons.Default.Star, "Pro") }
                    IconButton(onClick = onOpenSettings) { Icon(Icons.Default.Settings, "Settings") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = onStartStream, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(8.dp)); Text("Iniciar Stream")
            }
            OutlinedButton(onClick = onOpenMedia, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Icon(Icons.Default.Wifi, null); Spacer(Modifier.width(8.dp)); Text("Servidor SRT (Pro)")
            }
            OutlinedButton(onClick = onUpgrade, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Icon(Icons.Default.Star, null); Spacer(Modifier.width(8.dp)); Text("Ver planes Pro")
            }
        }
    }
}
