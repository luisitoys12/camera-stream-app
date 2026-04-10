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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.estacionkus.camerastream.streaming.StreamStats
import tech.estacionkus.camerastream.ui.theme.*

@Composable
fun StreamScreen(
    viewModel: StreamViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val stats by viewModel.stats.collectAsState()
    val isLive = stats.isLive

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Camera preview placeholder (CameraX SurfaceView goes here)
        CameraPreview(modifier = Modifier.fillMaxSize())

        // Live indicator top-left
        AnimatedVisibility(
            visible = isLive,
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
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

        // Duration top-right
        if (isLive) {
            Surface(
                color = Color.Black.copy(alpha = 0.55f),
                shape = MaterialTheme.shapes.extraSmall,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
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

        // Connection dots — which platforms are live
        if (isLive && stats.rtmpConnections.isNotEmpty()) {
            Row(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                stats.rtmpConnections.forEach { (name, connected) ->
                    Surface(
                        color = if (connected) Color(0xFF2E7D32).copy(alpha = 0.85f) else Color(0xFFC62828).copy(alpha = 0.85f),
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Bottom controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp, start = 24.dp, end = 24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back
            StreamControlButton(Icons.Default.ArrowBack, "Volver", size = 44) { onBack() }

            // Flip camera
            StreamControlButton(Icons.Default.FlipCameraAndroid, "Girar cámara", size = 44) {
                viewModel.flipCamera()
            }

            // Go live / Stop — main button
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        if (isLive) CameraRed else Color.White.copy(alpha = 0.9f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { if (isLive) viewModel.stopStream() else viewModel.startStream() }) {
                    Icon(
                        if (isLive) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                        contentDescription = if (isLive) "Detener" else "Iniciar",
                        tint = if (isLive) Color.White else CameraRed,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Mute audio
            StreamControlButton(Icons.Default.Mic, "Micrófono", size = 44) {
                viewModel.toggleMute()
            }

            // Open media overlay picker
            StreamControlButton(Icons.Default.PermMedia, "Overlays", size = 44) {
                viewModel.toggleOverlayPanel()
            }
        }
    }
}

@Composable
private fun StreamControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDesc: String,
    size: Int = 48,
    onClick: () -> Unit
) {
    Surface(
        color = Color.Black.copy(alpha = 0.45f),
        shape = CircleShape,
        modifier = Modifier.size(size.dp)
    ) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = contentDesc, tint = Color.White, modifier = Modifier.size((size * 0.5).dp))
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
