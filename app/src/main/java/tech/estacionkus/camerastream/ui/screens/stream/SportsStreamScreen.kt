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
import tech.estacionkus.camerastream.domain.model.SportState
import tech.estacionkus.camerastream.domain.model.SportType
import tech.estacionkus.camerastream.streaming.SportsModeManager
import tech.estacionkus.camerastream.streaming.SportsStateManager

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
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
    var useFrontCamera by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }

    // Sports state
    var sportState by remember { mutableStateOf(SportState()) }
    var showTeamEditDialog by remember { mutableStateOf<String?>(null) } // "A" or "B" or null
    var showSportPicker by remember { mutableStateOf(false) }
    var clockJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    // Game clock coroutine
    LaunchedEffect(sportState.isClockRunning) {
        if (sportState.isClockRunning) {
            while (sportState.isClockRunning) {
                kotlinx.coroutines.delay(100)
                sportState = sportState.copy(gameClockMs = sportState.gameClockMs + 100)
            }
        }
    }

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
                    // Sport type selector
                    IconButton(onClick = { showSportPicker = true }) {
                        Icon(Icons.Default.SportsSoccer, null, tint = Color(0xFF4CAF50))
                    }
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
                modifier = Modifier.fillMaxWidth().weight(0.4f).background(Color(0xFF0A0A0A), RoundedCornerShape(12.dp)),
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

            // Scoreboard
            ScoreboardPanel(
                sportState = sportState,
                onScoreAPlus = { sportState = sportState.copy(teamA = sportState.teamA.copy(score = sportState.teamA.score + 1)) },
                onScoreAMinus = { sportState = sportState.copy(teamA = sportState.teamA.copy(score = (sportState.teamA.score - 1).coerceAtLeast(0))) },
                onScoreBPlus = { sportState = sportState.copy(teamB = sportState.teamB.copy(score = sportState.teamB.score + 1)) },
                onScoreBMinus = { sportState = sportState.copy(teamB = sportState.teamB.copy(score = (sportState.teamB.score - 1).coerceAtLeast(0))) },
                onEditTeamA = { showTeamEditDialog = "A" },
                onEditTeamB = { showTeamEditDialog = "B" },
                onToggleClock = {
                    sportState = sportState.copy(isClockRunning = !sportState.isClockRunning)
                },
                onResetClock = {
                    sportState = sportState.copy(gameClockMs = 0L, isClockRunning = false)
                },
                onNextPeriod = {
                    val periods = sportState.sportType.periods
                    val idx = periods.indexOf(sportState.period)
                    if (idx < periods.size - 1) {
                        sportState = sportState.copy(period = periods[idx + 1])
                    }
                }
            )

            // Preset selector
            Text("Preset de transmision", style = MaterialTheme.typography.labelMedium)
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
                    StatChip("${selectedPreset.bitrateKbps}kbps", Color(0xFF1565C0))
                    StatChip("SRT ${selectedPreset.srtLatencyMs}ms", Color(0xFF00897B))
                    StatChip("${selectedPreset.fps}fps", Color(0xFF6A1B9A))
                }
            }

            // Controls
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { isMuted = !isMuted },
                    modifier = Modifier.size(52.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
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
                OutlinedButton(
                    onClick = { useFrontCamera = !useFrontCamera },
                    modifier = Modifier.size(52.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Cameraswitch, null, modifier = Modifier.size(20.dp))
                }
                OutlinedButton(
                    onClick = { isRecording = !isRecording },
                    modifier = Modifier.size(52.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = if (isRecording) ButtonDefaults.outlinedButtonColors(contentColor = Color.Red) else ButtonDefaults.outlinedButtonColors()
                ) {
                    Icon(Icons.Default.FiberManualRecord, null, modifier = Modifier.size(20.dp), tint = if (isRecording) Color.Red else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }

    // Team name edit dialog
    if (showTeamEditDialog != null) {
        val isTeamA = showTeamEditDialog == "A"
        val currentName = if (isTeamA) sportState.teamA.name else sportState.teamB.name
        var editName by remember(showTeamEditDialog) { mutableStateOf(currentName) }

        AlertDialog(
            onDismissRequest = { showTeamEditDialog = null },
            title = { Text("Edit ${if (isTeamA) "Home" else "Away"} Team") },
            text = {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Team name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editName.isNotBlank()) {
                        sportState = if (isTeamA) {
                            sportState.copy(teamA = sportState.teamA.copy(name = editName))
                        } else {
                            sportState.copy(teamB = sportState.teamB.copy(name = editName))
                        }
                    }
                    showTeamEditDialog = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showTeamEditDialog = null }) { Text("Cancel") }
            }
        )
    }

    // Sport type picker
    if (showSportPicker) {
        AlertDialog(
            onDismissRequest = { showSportPicker = false },
            title = { Text("Select Sport") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SportType.entries.forEach { sport ->
                        Surface(
                            onClick = {
                                sportState = sportState.copy(
                                    sportType = sport,
                                    period = sport.periods.firstOrNull() ?: "1"
                                )
                                showSportPicker = false
                            },
                            color = if (sportState.sportType == sport) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                sport.displayName,
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                fontWeight = if (sportState.sportType == sport) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSportPicker = false }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun ScoreboardPanel(
    sportState: SportState,
    onScoreAPlus: () -> Unit,
    onScoreAMinus: () -> Unit,
    onScoreBPlus: () -> Unit,
    onScoreBMinus: () -> Unit,
    onEditTeamA: () -> Unit,
    onEditTeamB: () -> Unit,
    onToggleClock: () -> Unit,
    onResetClock: () -> Unit,
    onNextPeriod: () -> Unit
) {
    val clockMs = sportState.gameClockMs
    val minutes = clockMs / 1000 / 60
    val secs = clockMs / 1000 % 60

    Surface(
        color = Color(0xFF1A1A2E),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Period & Clock row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Period
                Surface(
                    onClick = onNextPeriod,
                    color = Color(0xFF2A2A4E),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        sportState.period,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Clock
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "%02d:%02d".format(minutes, secs),
                        color = if (sportState.isClockRunning) Color(0xFF4CAF50) else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                    IconButton(onClick = onToggleClock, modifier = Modifier.size(32.dp)) {
                        Icon(
                            if (sportState.isClockRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            null,
                            tint = if (sportState.isClockRunning) Color(0xFF4CAF50) else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onResetClock, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Replay, null, tint = Color(0xFFFF9800), modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Teams & Scores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Team A
                TeamScoreColumn(
                    teamName = sportState.teamA.name,
                    score = sportState.teamA.score,
                    color = Color(sportState.teamA.color),
                    onPlus = onScoreAPlus,
                    onMinus = onScoreAMinus,
                    onEditName = onEditTeamA
                )

                Text("VS", color = Color(0xFF666666), fontWeight = FontWeight.Bold, fontSize = 14.sp)

                // Team B
                TeamScoreColumn(
                    teamName = sportState.teamB.name,
                    score = sportState.teamB.score,
                    color = Color(sportState.teamB.color),
                    onPlus = onScoreBPlus,
                    onMinus = onScoreBMinus,
                    onEditName = onEditTeamB
                )
            }

            // Sport type indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(sportState.sportType.displayName, color = Color(0xFF888888), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun TeamScoreColumn(
    teamName: String,
    score: Int,
    color: Color,
    onPlus: () -> Unit,
    onMinus: () -> Unit,
    onEditName: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Team name (tappable to edit)
        Surface(
            onClick = onEditName,
            color = color.copy(alpha = 0.2f),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                teamName,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        // Score with +/- buttons
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onMinus, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Remove, null, tint = Color(0xFFEF5350), modifier = Modifier.size(18.dp))
            }
            Text(
                "$score",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            )
            IconButton(onClick = onPlus, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Add, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
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
