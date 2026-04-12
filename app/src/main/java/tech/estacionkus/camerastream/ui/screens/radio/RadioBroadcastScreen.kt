package tech.estacionkus.camerastream.ui.screens.radio

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioBroadcastScreen(
    onBack: () -> Unit,
    onUpgrade: () -> Unit,
    viewModel: RadioBroadcastViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isAgency by viewModel.isAgency.collectAsState()

    if (!isAgency) {
        // Agency lock screen
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Radio Broadcast") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1A1A2E), Color(0xFF0F3460))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Lock, null, tint = Color.White.copy(0.5f), modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Agency Exclusive Feature", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Radio Broadcast requires Agency plan", color = Color.White.copy(0.6f))
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = onUpgrade,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
                    ) {
                        Text("Upgrade to Agency")
                    }
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Radio, null, tint = Color(0xFFE53935))
                        Spacer(Modifier.width(8.dp))
                        Text("Radio Broadcast")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ON AIR Button
            OnAirButton(
                isLive = uiState.isLive,
                onToggle = { viewModel.toggleBroadcast() }
            )

            // VU Meters
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Audio Levels", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    VuMeter("L", uiState.audioLevelLeft)
                    Spacer(Modifier.height(8.dp))
                    VuMeter("R", uiState.audioLevelRight)
                }
            }

            // Now Playing
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Now Playing", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.nowPlaying,
                        onValueChange = { viewModel.setNowPlaying(it) },
                        label = { Text("Song / Show name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.MusicNote, null) }
                    )
                }
            }

            // Broadcast Mode
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Broadcast Mode", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.mode == RadioMode.AUDIO_ONLY,
                            onClick = { viewModel.setMode(RadioMode.AUDIO_ONLY) },
                            label = { Text("Audio Only") },
                            leadingIcon = {
                                if (uiState.mode == RadioMode.AUDIO_ONLY)
                                    Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                            }
                        )
                        FilterChip(
                            selected = uiState.mode == RadioMode.VISUAL_RADIO,
                            onClick = { viewModel.setMode(RadioMode.VISUAL_RADIO) },
                            label = { Text("Visual Radio") },
                            leadingIcon = {
                                if (uiState.mode == RadioMode.VISUAL_RADIO)
                                    Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                            }
                        )
                    }
                }
            }

            // Schedule
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Schedule", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.currentShow,
                        onValueChange = { viewModel.setCurrentShow(it) },
                        label = { Text("Current Show") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.LiveTv, null) }
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.nextShow,
                        onValueChange = { viewModel.setNextShow(it) },
                        label = { Text("Next Show") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Schedule, null) }
                    )
                }
            }

            // Stream destination
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Stream Destination", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.rtmpUrl,
                        onValueChange = { viewModel.setRtmpUrl(it) },
                        label = { Text("RTMP URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("rtmp://your-server/radio") }
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.streamKey,
                        onValueChange = { viewModel.setStreamKey(it) },
                        label = { Text("Stream Key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // Listener count
            if (uiState.isLive) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Headphones, null, tint = Color(0xFF4CAF50))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Listeners", fontWeight = FontWeight.Bold)
                            Text("${uiState.listenerCount}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun OnAirButton(isLive: Boolean, onToggle: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "onair")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isLive) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isLive) 0.8f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isLive) Color(0xFFE53935) else Color(0xFF424242),
        label = "bg"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(vertical = 8.dp)
    ) {
        // Glow ring
        if (isLive) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935).copy(alpha = glowAlpha * 0.3f))
            )
        }
        // Button
        Button(
            onClick = onToggle,
            modifier = Modifier
                .size(140.dp)
                .scale(if (isLive) scale else 1f),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = bgColor),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (isLive) "ON AIR" else "OFF AIR",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color.White
                )
                Text(
                    if (isLive) "Tap to stop" else "Tap to start",
                    fontSize = 10.sp,
                    color = Color.White.copy(0.7f)
                )
            }
        }
    }
}

@Composable
private fun VuMeter(channel: String, level: Float) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(channel, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp))
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF333333))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(level.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF4CAF50), Color(0xFFFFEB3B), Color(0xFFE53935))
                        )
                    )
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            "${(level * 100).toInt()}%",
            color = Color.White.copy(0.7f),
            fontSize = 11.sp,
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.End
        )
    }
}

enum class RadioMode { AUDIO_ONLY, VISUAL_RADIO }

data class RadioUiState(
    val isLive: Boolean = false,
    val mode: RadioMode = RadioMode.AUDIO_ONLY,
    val nowPlaying: String = "",
    val currentShow: String = "",
    val nextShow: String = "",
    val rtmpUrl: String = "",
    val streamKey: String = "",
    val audioLevelLeft: Float = 0f,
    val audioLevelRight: Float = 0f,
    val listenerCount: Int = 0
)
