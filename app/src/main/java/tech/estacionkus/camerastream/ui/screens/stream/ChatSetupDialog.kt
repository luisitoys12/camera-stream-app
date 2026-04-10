package tech.estacionkus.camerastream.ui.screens.stream

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChatSetupDialog(onConfirm: (platform: String, channel: String) -> Unit, onDismiss: () -> Unit) {
    var channel by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Conectar Chat") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Plataforma: Twitch", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = channel,
                    onValueChange = { channel = it },
                    label = { Text("Nombre del canal") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "El chat se muestra en tiempo real sobre el stream. Solo lectura.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (channel.isNotBlank()) onConfirm("TWITCH", channel) },
                enabled = channel.isNotBlank()
            ) { Text("Conectar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
