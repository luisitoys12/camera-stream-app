package com.cushMedia.camerastream.ui.screens

import androidx.camera.view.PreviewView
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cushMedia.camerastream.viewmodel.StreamViewModel

@Composable
fun StreamScreen(
    onStopStream: () -> Unit,
    streamViewModel: StreamViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isStreaming by streamViewModel.isStreaming.collectAsState()
    val isMuted by streamViewModel.isMuted.collectAsState()
    val streamStatus by streamViewModel.streamStatus.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { previewView ->
                    streamViewModel.startCamera(ctx, lifecycleOwner, previewView)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Status bar top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .statusBarsPadding()
                .align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isStreaming) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color.Red, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("EN VIVO", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(16.dp))
            }
            Text(streamStatus, color = Color.White, fontSize = 12.sp)
        }

        // Controls bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .navigationBarsPadding()
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute button
            IconButton(
                onClick = { streamViewModel.toggleMute() },
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = if (isMuted) "Activar micrófono" else "Silenciar",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Stream button (main)
            Button(
                onClick = {
                    if (isStreaming) {
                        streamViewModel.stopStream()
                        onStopStream()
                    } else {
                        streamViewModel.startStream()
                    }
                },
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isStreaming) Color.Red else MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = if (isStreaming) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isStreaming) "Detener" else "Iniciar",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Flip camera button
            IconButton(
                onClick = { streamViewModel.flipCamera() },
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.FlipCameraAndroid,
                    contentDescription = "Voltear cámara",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
