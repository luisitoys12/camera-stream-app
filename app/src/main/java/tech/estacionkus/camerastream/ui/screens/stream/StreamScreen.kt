package tech.estacionkus.camerastream.ui.screens.stream

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.estacionkus.camerastream.ui.theme.*

@Composable
fun StreamScreen(
    viewModel: StreamViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val stats by viewModel.stats.collectAsState()
    val overlayPanelVisible by viewModel.overlayPanelVisible.collectAsState()
    val activeOverlays by viewModel.activeOverlays.collectAsState()
    val mediaAssets by viewModel.mediaAssets.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val isLive = stats.isLive

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        // ── 1. Camera preview (bottom layer) ──────────────────────────────
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            onCameraReady = viewModel::onCameraReady
        )

        // ── 2. Overlay renderer (above camera, below UI) ──────────────────
        OverlayRenderer(
            overlays = activeOverlays,
            onAutoHide = viewModel::removeOverlay
        )

        // ── 3. HUD top bar ────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LIVE badge
            AnimatedVisibility(
                visible = isLive,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Surface(
                    color = CameraRed,
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(Modifier.size(6.dp).background(Color.White, CircleShape))
                        Text("LIVE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.sp)
                    }
                }
            }

            // Platform connection pills
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                stats.rtmpConnections.forEach { (name, connected) ->
                    Surface(
                        color = if (connected) Color(0xFF1B5E20).copy(alpha = 0.85f)
                        else Color(0xFFB71C1C).copy(alpha = 0.85f),
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            name,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Duration
            if (isLive) {
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        formatDuration(stats.durationSeconds),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // ── 4. Bottom controls ────────────────────────────────────────────
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp, start = 24.dp, end = 24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StreamCtrlBtn(Icons.Default.ArrowBack, "Volver") { onBack() }

            StreamCtrlBtn(Icons.Default.FlipCameraAndroid, "Voltear cámara") {
                viewModel.flipCamera()
            }

            // Go Live / Stop
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .background(
                        if (isLive) CameraRed else Color.White.copy(alpha = 0.92f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { if (isLive) viewModel.stopStream() else viewModel.startStream() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        if (isLive) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                        contentDescription = if (isLive) "Detener" else "Transmitir",
                        tint = if (isLive) Color.White else CameraRed,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            StreamCtrlBtn(
                icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                contentDesc = "Micrófono",
                active = isMuted
            ) { viewModel.toggleMute() }

            // Overlays button — shows badge with active count
            Box {
                StreamCtrlBtn(Icons.Default.Layers, "Overlays") {
                    viewModel.toggleOverlayPanel()
                }
                if (activeOverlays.isNotEmpty()) {
                    Surface(
                        color = CameraRed,
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(16.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                activeOverlays.size.toString(),
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // ── 5. Overlay panel (bottom sheet inside the same screen) ────────
        OverlayPanel(
            visible = overlayPanelVisible,
            mediaAssets = mediaAssets,
            activeOverlays = activeOverlays,
            onAddOverlay = viewModel::addOverlay,
            onRemoveOverlay = viewModel::removeOverlay,
            onDismiss = { viewModel.toggleOverlayPanel() }
        )
    }
}

@Composable
private fun StreamCtrlBtn(
    icon: ImageVector,
    contentDesc: String,
    active: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        color = if (active) CameraRed.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.45f),
        shape = CircleShape,
        modifier = Modifier.size(48.dp)
    ) {
        IconButton(onClick = onClick) {
            Icon(
                icon,
                contentDescription = contentDesc,
                tint = if (active) CameraRed else Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
