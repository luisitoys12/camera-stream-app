package tech.estacionkus.camerastream.ui.screens.pro

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UpgradeScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Planes") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Pro — \$24.99/mes", style = MaterialTheme.typography.titleLarge)
            Text("5 plataformas, SRT, 1080p, Escenas, Chroma key y más")
            Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text("Suscribirse")
            }
        }
    }
}
