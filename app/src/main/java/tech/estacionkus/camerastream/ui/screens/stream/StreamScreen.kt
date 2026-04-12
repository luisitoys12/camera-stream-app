package tech.estacionkus.camerastream.ui.screens.stream

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.estacionkus.camerastream.streaming.StreamState
import tech.estacionkus.camerastream.streaming.TargetHealth

@Composable
fun StreamScreen(
    onBack: () -> Unit,
    viewModel: StreamViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val scenes by viewModel.scenes.collectAsState()
    val context = LocalContext.current
    var showSettings by remember { mutableStateOf(false) }
    var showChatSetup by remember { mutableStateOf(false) }
    var showScenes by remember { mutableStateOf(false) }

    // Show error toasts
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.dismissError()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Camera Preview - full screen
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            isFrontCamera = uiState.isFrontCamera,
            onCameraReady = {}
        )

        // BRB Overlay (disconnect protection)
        AnimatedVisibility(
            visible = uiState.showBrb,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xE60D0D1A)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Pulsing icon
                    val infiniteTransition = rememberInfiniteTransition(label = "brb")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 0.9f,
                        targetValue = 1.1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "brbPulse"
                    )
                    Icon(
                        Icons.Default.WifiOff,
                        null,
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size((48 * scale).dp)
                    )
                    Text(
                        "BE RIGHT BACK",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 4.sp
                    )
                    Text(
                        "Reconnecting in ${uiState.brbCountdown}s",
                        fontSize = 16.sp,
                        color = Color(0xFFBBBBBB)
                    )
                    LinearProgressIndicator(
                        progress = { uiState.brbCountdown / 60f },
                        modifier = Modifier.width(200.dp),
                        color = Color(0xFFE53935),
                        trackColor = Color(0xFF333333)
                    )
                }
            }
        }

        // Top bar overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                IconButton(
                    onClick = {
                        if (uiState.streamState == StreamState.LIVE) {
                            viewModel.stopStream()
                        }
                        onBack()
                    }
                ) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }

                // Live indicator + timer
                AnimatedVisibility(
                    visible = uiState.streamState == StreamState.LIVE,
                    enter = fadeIn() + slideInHorizontally(),
                    exit = fadeOut() + slideOutHorizontally()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .background(Color(0xCC000000), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        // Pulsing red dot
                        val pulse = rememberInfiniteTransition(label = "live")
                        val dotAlpha by pulse.animateFloat(
                            initialValue = 1f,
                            targetValue = 0.3f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(800),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "liveDot"
                        )
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color.Red.copy(alpha = dotAlpha), CircleShape)
                        )
                        Text(
                            "LIVE",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            formatDuration(uiState.elapsedSeconds),
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }

                // Right side buttons
                Row {
                    // Health indicator
                    AnimatedVisibility(visible = uiState.streamState == StreamState.LIVE) {
                        val healthColor = when (uiState.healthGrade) {
                            "EXCELLENT" -> Color(0xFF4CAF50)
                            "GOOD" -> Color(0xFF2196F3)
                            "FAIR" -> Color(0xFFFFC107)
                            else -> Color(0xFFE53935)
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.MonitorHeart, "Health", tint = healthColor)
                        }
                    }
                    IconButton(onClick = { showSettings = !showSettings }) {
                        Icon(Icons.Default.Settings, "Settings", tint = Color.White)
                    }
                }
            }

            // Stats bar (visible when live)
            AnimatedVisibility(visible = uiState.streamState == StreamState.LIVE) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusChip(
                        label = when (uiState.streamState) {
                            StreamState.LIVE -> "Connected"
                            StreamState.CONNECTING -> "Connecting..."
                            StreamState.RECONNECTING -> "Reconnecting..."
                            StreamState.ERROR -> "Error"
                            else -> "Offline"
                        },
                        color = when (uiState.streamState) {
                            StreamState.LIVE -> Color(0xFF4CAF50)
                            StreamState.CONNECTING, StreamState.RECONNECTING -> Color(0xFFFFC107)
                            StreamState.ERROR -> Color.Red
                            else -> Color.Gray
                        }
                    )
                    StatusChip(label = "${uiState.bitrateKbps} kbps", color = Color(0xFF2196F3))
                    StatusChip(label = "${uiState.fps} fps", color = Color(0xFF9C27B0))
                    StatusChip(label = uiState.networkType, color = Color(0xFF607D8B))
                }
            }

            // Multi-stream status bar
            AnimatedVisibility(visible = uiState.isMultiStream && uiState.targetStatuses.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(uiState.targetStatuses.entries.toList()) { (id, status) ->
                        val target = uiState.multiStreamTargets.find { it.id == id }
                        val name = target?.name ?: id.take(8)
                        val statusColor = when (status.state) {
                            StreamState.LIVE -> when (status.health) {
                                TargetHealth.GOOD -> Color(0xFF4CAF50)
                                TargetHealth.FAIR -> Color(0xFFFFC107)
                                TargetHealth.POOR -> Color(0xFFE53935)
                            }
                            StreamState.CONNECTING, StreamState.RECONNECTING -> Color(0xFFFFC107)
                            else -> Color.Gray
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color(0xCC000000), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).background(statusColor, CircleShape))
                            Spacer(Modifier.width(4.dp))
                            Text(name, color = Color.White, fontSize = 10.sp)
                            if (status.bitrateKbps > 0) {
                                Spacer(Modifier.width(4.dp))
                                Text("${status.bitrateKbps}k", color = Color(0xFF888888), fontSize = 9.sp)
                            }
                        }
                    }
                }
            }

            // Connecting overlay
            AnimatedVisibility(visible = uiState.streamState == StreamState.CONNECTING) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(Color(0xCC000000), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color(0xFFFFC107),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Connecting to stream server...", color = Color.White, fontSize = 13.sp)
                }
            }
        }

        // Recording indicator
        AnimatedVisibility(
            visible = uiState.isRecording,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 52.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .background(Color(0xCCFF0000), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                val pulse = rememberInfiniteTransition(label = "rec")
                val dotAlpha by pulse.animateFloat(
                    initialValue = 1f, targetValue = 0.2f,
                    animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
                    label = "recDot"
                )
                Box(modifier = Modifier.size(8.dp).background(Color.White.copy(alpha = dotAlpha), CircleShape))
                Text("REC", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Chat overlay (right side)
        AnimatedVisibility(
            visible = uiState.chatVisible && chatMessages.isNotEmpty(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
                .width(250.dp)
                .heightIn(max = 300.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .background(Color(0x66000000), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                reverseLayout = true
            ) {
                items(chatMessages.takeLast(20)) { msg ->
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        // Platform badge
                        val badgeColor = when (msg.platform.displayName) {
                            "Twitch" -> Color(0xFF9B59B6)
                            "Kick" -> Color(0xFF53FC18)
                            "YouTube" -> Color(0xFFFF0000)
                            else -> Color.Gray
                        }
                        Box(
                            modifier = Modifier
                                .padding(end = 4.dp, top = 4.dp)
                                .size(6.dp)
                                .background(badgeColor, CircleShape)
                        )
                        Text(
                            "${msg.author}: ",
                            color = try { Color(android.graphics.Color.parseColor(msg.authorColor)) } catch (_: Exception) { Color(0xFF9B59B6) },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(msg.content, color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }

        // Scene switcher (bottom left)
        AnimatedVisibility(
            visible = showScenes,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 8.dp, bottom = 160.dp)
        ) {
            Column(
                modifier = Modifier
                    .width(100.dp)
                    .background(Color(0xCC000000), RoundedCornerShape(12.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Scenes", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                scenes.forEach { scene ->
                    val isActive = scene.id == uiState.activeSceneId
                    Surface(
                        color = if (isActive) Color(0xFF1A237E) else Color(0x33FFFFFF),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.switchScene(scene.id) }
                    ) {
                        Text(
                            scene.name,
                            color = if (isActive) Color(0xFF82B1FF) else Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0x99000000))
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Control buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ControlButton(
                    icon = if (uiState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    label = if (uiState.isMuted) "Unmute" else "Mute",
                    isActive = uiState.isMuted,
                    activeColor = Color(0xFFFF9800),
                    onClick = { viewModel.toggleMute() }
                )
                ControlButton(
                    icon = Icons.Default.Cameraswitch,
                    label = "Flip",
                    onClick = { viewModel.flipCamera() }
                )
                ControlButton(
                    icon = if (uiState.isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                    label = if (uiState.isRecording) "Stop Rec" else "Record",
                    isActive = uiState.isRecording,
                    activeColor = Color.Red,
                    onClick = { viewModel.toggleRecording() }
                )
                ControlButton(
                    icon = Icons.Default.Chat,
                    label = "Chat",
                    isActive = uiState.chatVisible,
                    activeColor = Color(0xFF9C27B0),
                    onClick = {
                        if (!viewModel.chatConnected.value && !uiState.chatVisible) {
                            showChatSetup = true
                        } else {
                            viewModel.toggleChat()
                        }
                    }
                )
                ControlButton(
                    icon = Icons.Default.ViewCarousel,
                    label = "Scenes",
                    isActive = showScenes,
                    activeColor = Color(0xFF2196F3),
                    onClick = { showScenes = !showScenes }
                )
            }

            // GO LIVE / STOP button
            Button(
                onClick = {
                    when (uiState.streamState) {
                        StreamState.IDLE, StreamState.ERROR -> viewModel.startStream()
                        StreamState.LIVE, StreamState.CONNECTING, StreamState.RECONNECTING -> viewModel.stopStream()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.streamState == StreamState.LIVE) Color.Red else Color(0xFFE53935)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(
                    if (uiState.streamState == StreamState.LIVE) Icons.Default.Stop else Icons.Default.PlayArrow,
                    null,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    when (uiState.streamState) {
                        StreamState.LIVE -> if (uiState.isMultiStream) "STOP ALL STREAMS" else "STOP STREAM"
                        StreamState.CONNECTING -> "CONNECTING..."
                        StreamState.RECONNECTING -> "RECONNECTING..."
                        else -> if (uiState.multiStreamTargets.size > 1) "GO LIVE (${uiState.multiStreamTargets.size} targets)" else "GO LIVE"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Settings panel overlay
        AnimatedVisibility(
            visible = showSettings,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            StreamSettingsPanel(
                bitrateKbps = uiState.bitrateKbps,
                fps = uiState.fps,
                onBitrateChange = { viewModel.setBitrate(it) },
                onDismiss = { showSettings = false }
            )
        }

        // Chat setup dialog
        if (showChatSetup) {
            ChatSetupDialog(
                onConfirm = { platform, channel ->
                    viewModel.connectChat(platform, channel)
                    viewModel.toggleChat()
                    showChatSetup = false
                },
                onDismiss = { showChatSetup = false }
            )
        }
    }
}

@Composable
private fun StatusChip(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .background(Color(0xCC000000), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
        Text(label, color = Color.White, fontSize = 11.sp)
    }
}

@Composable
private fun ControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean = false,
    activeColor: Color = Color.White,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .background(
                    if (isActive) activeColor.copy(alpha = 0.2f) else Color(0x33FFFFFF),
                    CircleShape
                )
        ) {
            Icon(
                icon, label,
                tint = if (isActive) activeColor else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(label, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun StreamSettingsPanel(
    bitrateKbps: Int,
    fps: Int,
    onBitrateChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xE61A1A1A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Stream Settings", color = Color.White, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, "Close", tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
            }

            Text("Bitrate: $bitrateKbps kbps", color = Color.White, fontSize = 13.sp)
            val bitrateOptions = listOf(1000, 1500, 2500, 4000, 6000, 8000)
            val currentIndex = bitrateOptions.indexOfFirst { it >= bitrateKbps }.coerceAtLeast(0)
            Slider(
                value = currentIndex.toFloat(),
                onValueChange = { onBitrateChange(bitrateOptions[it.toInt().coerceIn(0, bitrateOptions.lastIndex)]) },
                valueRange = 0f..(bitrateOptions.size - 1).toFloat(),
                steps = bitrateOptions.size - 2,
                colors = SliderDefaults.colors(thumbColor = Color(0xFFE53935), activeTrackColor = Color(0xFFE53935))
            )

            Text("Current FPS: $fps", color = Color(0xFF888888), fontSize = 12.sp)
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return "%02d:%02d:%02d".format(h, m, s)
}
