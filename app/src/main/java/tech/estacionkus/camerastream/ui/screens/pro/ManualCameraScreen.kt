package tech.estacionkus.camerastream.ui.screens.pro

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tech.estacionkus.camerastream.domain.model.WhiteBalance

enum class WhiteBalance(val label: String) { AUTO("Auto"), INCANDESCENT("Incandescente"), DAYLIGHT("Luz día"), CLOUDY("Nublado") }

@Composable
fun ManualCameraScreen(onBack: () -> Unit) {
    var exposure by remember { mutableStateOf(0f) }
    var zoom by remember { mutableStateOf(1f) }
    var wb by remember { mutableStateOf(WhiteBalance.AUTO) }
    var stabilization by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cámara Manual") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Exposición: ${exposure.toInt()}")
            Slider(value = exposure, onValueChange = { exposure = it }, valueRange = -4f..4f, steps = 7, modifier = Modifier.fillMaxWidth())
            Text("Zoom: ${"%,.1f".format(zoom)}x")
            Slider(value = zoom, onValueChange = { zoom = it }, valueRange = 1f..10f, modifier = Modifier.fillMaxWidth())
            Text("Balance de blancos")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                WhiteBalance.entries.forEach { w ->
                    FilterChip(selected = wb == w, onClick = { wb = w }, label = { Text(w.label, style = MaterialTheme.typography.labelSmall) })
                }
            }
            FilterChip(selected = stabilization, onClick = { stabilization = !stabilization }, label = { Text("Estabilización") })
        }
    }
}
