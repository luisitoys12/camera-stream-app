package tech.estacionkus.camerastream.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var rtmpUrl by remember { mutableStateOf("rtmp://live.twitch.tv/live") }
    var streamKey by remember { mutableStateOf("") }
    var bitrate by remember { mutableStateOf("2500") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Stream RTMP", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(value = rtmpUrl, onValueChange = { rtmpUrl = it }, label = { Text("RTMP URL") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = streamKey, onValueChange = { streamKey = it }, label = { Text("Stream Key") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = bitrate, onValueChange = { bitrate = it }, label = { Text("Bitrate (kbps)") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Guardar") }
        }
    }
}
