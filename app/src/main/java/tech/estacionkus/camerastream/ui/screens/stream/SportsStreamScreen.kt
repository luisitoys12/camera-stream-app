package tech.estacionkus.camerastream.ui.screens.stream

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tech.estacionkus.camerastream.streaming.SportsModeManager

@Composable
fun SportsStreamScreen(
    onBack: () -> Unit,
    onStartStream: (SportsModeManager.BroadcastPreset) -> Unit
) {
    var selectedPreset by remember { mutableStateOf(SportsModeManager.BroadcastPreset.SPORTS_720P) }
    var isLive by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var seconds by remember { mutableStateOf(0) }
    var signalStrength by remember { mutableStateOf(4) } // 1-5

    LaunchedEffect(isLive) {
        seconds = 0
        while (isLive) { kotlinx.coroutines.delay(1000); seconds++ }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isLive) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(Modifier.size(8.dp).background(Color.Red, RoundedCornerShape(4.dp)))
                        Text("%02d:%02d:%02d".format(seconds/3600, seconds/60%60, seconds%60), color = Color.Red, fontWeight = FontWeight.Bold)
                    } else Text("Modo Deportes")
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    // Signal indicator
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                        (1..5).forEach { i ->
                            Box(modifier = Modifier.width(4.dp).height((i * 4).dp + 4.dp).padding(horizontal = 1.dp)
                                .background(if (i <= signalStrength) Color(0xFF4CAF50) else Color(0xFF333333), RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Camera preview
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f).background(Color(0xFF0A0A0A), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Videocam, null, modifier = Modifier.size(48.dp), tint = Color(0xFF333333))
                    if (isLive) {
                        Surface(color = Color.Red, shape = RoundedCornerShape(4.dp)) {
                            Text("EN VIVO", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 2.sp)
                        }
                    }
                }
                // Preset badge
                Surface(
                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                    color = Color.Black.copy(0.7f), shape = RoundedCornerShape(6.dp)
                ) {
                    Text(selectedPreset.description, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White, fontSize = 10.sp)
                }
            }

            // Preset selector
            Text("Preset de transmisión", style = MaterialTheme.typography.labelMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SportsModeManager.BroadcastPreset.entries) { preset ->
                    FilterChip(
                        selected = selectedPreset == preset,
                        onClick = { if (!isLive) selectedPreset = preset },
                        label = { Text(preset.label, fontSize = 11.sp) }
                    )
                }
            }

            // Stats row
            if (isLive) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatChip("📡 ${selectedPreset.bitrateKbps}kbps", Color(0xFF1565C0))
                    StatChip("⏱ SRT ${selectedPreset.srtLatencyMs}ms", Color(0xFF00897B))
                    StatChip("🎥 ${selectedPreset.fps}fps", Color(0xFF6A1B9A))
                }
            }

            // Controls
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { isMuted = !isMuted }, modifier = Modifier.size(52.dp), contentPadding = PaddingValues(0.dp)) {
                    Icon(if (isMuted) Icons.Default.MicOff else Icons.Default.Mic, null, modifier = Modifier.size(20.dp))
                }
                Button(
                    onClick = {
                        isLive = !isLive
                        if (isLive) onStartStream(selectedPreset)
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isLive) Color.Red else MaterialTheme.colorScheme.primary)
                ) {
                    Icon(if (isLive) Icons.Default.Stop else Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (isLive) "Detener" else "Iniciar", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = { }, modifier = Modifier.size(52.dp), contentPadding = PaddingValues(0.dp)) {
                    Icon(Icons.Default.Cameraswitch, null, modifier = Modifier.size(20.dp))
                }
                OutlinedButton(onClick = { }, modifier = Modifier.size(52.dp), contentPadding = PaddingValues(0.dp)) {
                    Icon(Icons.Default.FiberManualRecord, null, modifier = Modifier.size(20.dp), tint = Color.Red)
                }
            }
        }
    }
}

@Composable
private fun StatChip(text: String, color: Color) {
    Surface(color = color.copy(0.15f), shape = RoundedCornerShape(6.dp)) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}
