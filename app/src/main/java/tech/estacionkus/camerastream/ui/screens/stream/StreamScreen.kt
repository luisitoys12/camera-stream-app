package tech.estacionkus.camerastream.ui.screens.stream

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StreamScreen(onBack: () -> Unit) {
    var isLive by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var seconds by remember { mutableStateOf(0) }
    var bitrateIndex by remember { mutableStateOf(3) }
    val bitrates = listOf(500, 1000, 1500, 2500, 4000, 6000, 8000)

    LaunchedEffect(isLive) {
        seconds = 0
        while (isLive) { kotlinx.coroutines.delay(1000); seconds++ }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isLive)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(8.dp).background(Color.Red, RoundedCornerShape(4.dp)))
                            Text("%02d:%02d:%02d".format(seconds / 3600, seconds / 60 % 60, seconds % 60), color = Color.Red)
                        }
                    else Text("Stream")
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Camera preview placeholder
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f).background(Color(0xFF111111), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Videocam, null, modifier = Modifier.size(64.dp), tint = Color(0xFF444444))
            }

            // Bitrate selector
            Text("Bitrate: ${bitrates[bitrateIndex]} kbps", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
            Slider(value = bitrateIndex.toFloat(), onValueChange = { bitrateIndex = it.toInt() }, valueRange = 0f..6f, steps = 5, modifier = Modifier.fillMaxWidth())

            // Controls
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { isMuted = !isMuted }, modifier = Modifier.weight(1f)) {
                    Icon(if (isMuted) Icons.Default.MicOff else Icons.Default.Mic, null)
                }
                Button(
                    onClick = { isLive = !isLive },
                    modifier = Modifier.weight(2f).height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isLive) Color.Red else MaterialTheme.colorScheme.primary)
                ) {
                    Icon(if (isLive) Icons.Default.Stop else Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (isLive) "Detener" else "En Vivo")
                }
                OutlinedButton(onClick = { }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Cameraswitch, null)
                }
            }
        }
    }
}
