package tech.estacionkus.camerastream.ui.screens.stream

import android.view.SurfaceView
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import tech.estacionkus.camerastream.streaming.StreamState
import tech.estacionkus.camerastream.ui.theme.*
import tech.estacionkus.camerastream.ui.util.formatDuration

@Composable
fun StreamScreen(
    onBack: () -> Unit,
    viewModel: StreamViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val chatConnected by viewModel.chatConnected.collectAsState()
    val context = LocalContext.current

    val surfaceView = remember { SurfaceView(context) }

    // Init stream manager with surface
    LaunchedEffect(Unit) {
        val features = uiState.features
        // RtmpStreamManager init happens via DI; here we attach surface once composed
    }

    var showBitrateSheet by remember { mutableStateOf(false) }
    var showChatSetup by remember { mutableStateOf(false) }
    var showOverlayPicker by remember { mutableStateOf(false) }

    val overlayPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.setOverlayUri(it.toString()) } }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        // ── Layer 1: Camera preview ──────────────────────────────────────────
        AndroidView(
            factory = { ctx ->
                FrameLayout(ctx).apply {
                    addView(surfaceView, FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    ))
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // ── Layer 2: Static image overlay ────────────────────────────────────
        if (uiState.overlayEnabled && uiState.overlayUri.isNotBlank()) {
            AsyncImage(
                model = uiState.overlayUri,
                contentDescription = "Overlay",
                modifier = Modifier.fillMaxSize(),
                alpha = 0.85f
            )
        }

        // ── Layer 3: HUD top bar ─────────────────────────────────────────────
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    // LIVE badge
                    if (uiState.streamState == StreamState.LIVE) {
                        Box(
                            modifier = Modifier
                                .background(CameraRed, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("● LIVE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        // Timer
                        Box(
                            modifier = Modifier
                                .background(Color.Black.copy(0.6f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                uiState.elapsedSeconds.formatDuration(),
                                color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Recording indicator
                    if (uiState.isRecording) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE53935), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text("⏺ REC", color = Color.White, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // ── Layer 4: Chat overlay (right side) ───────────────────────────────
        AnimatedVisibility(
            visible = uiState.chatVisible && chatMessages.isNotEmpty(),
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it },
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(0.5f).width(200.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.45f))
                    .padding(8.dp),
                reverseLayout = true,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(chatMessages.reversed()) { msg ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            msg.author,
                            color = try { Color(android.graphics.Color.parseColor(msg.authorColor)) }
                            catch (_: Exception) { Color(0xFF9B59B6) },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            msg.message,
                            color = Color.White,
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // ── Layer 5: Bottom controls ─────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(0.7f))
                ))
                .padding(bottom = 24.dp, top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            // Error snackbar
            uiState.errorMessage?.let { err ->
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(err, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        IconButton(onClick = viewModel::dismissError) {
                            Icon(Icons.Default.Close, null, tint = Color.White)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flip camera
                ControlButton(icon = Icons.Default.FlipCameraAndroid, label = "Flip") { viewModel.flipCamera() }

                // Overlay toggle
                ControlButton(
                    icon = if (uiState.overlayEnabled) Icons.Default.Layers else Icons.Default.LayersClear,
                    label = "Overlay",
                    active = uiState.overlayEnabled
                ) { if (uiState.overlayUri.isBlank()) showOverlayPicker = true else viewModel.toggleOverlay() }

                // Main stream button
                val isLive = uiState.streamState == StreamState.LIVE
                val isConnecting = uiState.streamState == StreamState.CONNECTING
                FloatingActionButton(
                    onClick = { if (isLive) viewModel.stopStream() else viewModel.startStream() },
                    containerColor = if (isLive) CameraRed else Color(0xFF2ECC71),
                    modifier = Modifier.size(64.dp)
                ) {
                    if (isConnecting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            if (isLive) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (isLive) "Stop" else "Go Live",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                // Mute
                ControlButton(
                    icon = if (uiState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    label = if (uiState.isMuted) "Unmute" else "Mute",
                    active = uiState.isMuted
                ) { viewModel.toggleMute() }

                // Chat
                ControlButton(
                    icon = Icons.Default.Chat,
                    label = "Chat",
                    active = uiState.chatVisible
                ) { if (!chatConnected) showChatSetup = true else viewModel.toggleChat() }
            }

            Spacer(Modifier.height(8.dp))

            // Bitrate row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { showBitrateSheet = true }) {
                    Icon(Icons.Default.Speed, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${uiState.bitrateKbps} kbps", color = Color.White, fontSize = 12.sp)
                }

                // Recording button
                TextButton(onClick = viewModel::toggleRecording) {
                    Icon(
                        if (uiState.isRecording) Icons.Default.StopCircle else Icons.Default.FiberManualRecord,
                        null,
                        tint = if (uiState.isRecording) CameraRed else Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (uiState.isRecording) "Detener grabación" else "Grabar",
                        color = if (uiState.isRecording) CameraRed else Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // ── Bitrate bottom sheet ─────────────────────────────────────────────
        if (showBitrateSheet) {
            BitrateSheet(
                current = uiState.bitrateKbps,
                onSelect = { viewModel.setBitrate(it); showBitrateSheet = false },
                onDismiss = { showBitrateSheet = false }
            )
        }

        // ── Chat setup dialog ─────────────────────────────────────────────────
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

        // ── Overlay picker trigger ────────────────────────────────────────────
        if (showOverlayPicker) {
            overlayPickerLauncher.launch("image/*")
            showOverlayPicker = false
        }
    }
}

@Composable
private fun ControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(44.dp)
                .background(
                    if (active) CameraRed.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.15f),
                    CircleShape
                )
        ) {
            Icon(icon, label, tint = if (active) CameraRed else Color.White, modifier = Modifier.size(22.dp))
        }
        Text(label, color = Color.White.copy(0.75f), fontSize = 10.sp)
    }
}
