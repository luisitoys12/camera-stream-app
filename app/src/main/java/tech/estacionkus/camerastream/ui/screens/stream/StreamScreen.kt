package tech.estacionkus.camerastream.ui.screens.stream

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StreamScreen(onBack: () -> Unit) {
    var isLive by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var seconds by remember { mutableStateOf(0) }

    LaunchedEffect(isLive) {
        if (isLive) {
            while (isLive) {
                kotlinx.coroutines.delay(1000)
                seconds++
            }
        } else seconds = 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isLive) Text("● %02d:%02d:%02d".format(seconds/3600, seconds/60%60, seconds%60),
                        color = Color.Red)
                    else Text("Stream")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = { isLive = !isLive },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLive) Color.Red else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(if (isLive) Icons.Default.Stop else Icons.Default.PlayArrow, null)
                Spacer(Modifier.width(8.dp))
                Text(if (isLive) "Detener Stream" else "Iniciar Stream")
            }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = { isMuted = !isMuted },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(if (isMuted) Icons.Default.MicOff else Icons.Default.Mic, null)
                Spacer(Modifier.width(8.dp))
                Text(if (isMuted) "Activar Micro" else "Silenciar")
            }
        }
    }
}
