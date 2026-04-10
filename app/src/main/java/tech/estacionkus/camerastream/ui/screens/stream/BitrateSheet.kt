package tech.estacionkus.camerastream.ui.screens.stream

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BitrateSheet(current: Int, onSelect: (Int) -> Unit, onDismiss: () -> Unit) {
    val presets = listOf(
        500 to "500 kbps — Muy baja (móvil malo)",
        1000 to "1 Mbps — Baja",
        1500 to "1.5 Mbps — Media-baja",
        2500 to "2.5 Mbps — Media (recomendada)",
        3500 to "3.5 Mbps — Alta",
        5000 to "5 Mbps — Muy alta",
        8000 to "8 Mbps — Ultra (WiFi)"
    )

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Bitrate de video", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            presets.forEach { (kbps, label) ->
                ListItem(
                    headlineContent = { Text(label) },
                    trailingContent = {
                        if (kbps == current) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier.clickable { onSelect(kbps) }
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
