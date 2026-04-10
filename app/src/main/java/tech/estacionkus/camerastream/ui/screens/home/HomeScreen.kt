package tech.estacionkus.camerastream.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tech.estacionkus.camerastream.ui.theme.*

@Composable
fun HomeScreen(
    onStartStream: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenMedia: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface900)
    ) {
        // Subtle gradient top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(Brush.verticalGradient(
                    colors = listOf(CameraRed.copy(alpha = 0.08f), Color.Transparent)
                ))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            // Logo mark
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(CameraRed.copy(alpha = 0.15f), shape = MaterialTheme.shapes.large),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.RadioButtonChecked,
                    contentDescription = null,
                    tint = CameraRed,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "CameraStream",
                style = MaterialTheme.typography.displaySmall,
                color = OnSurface
            )
            Text(
                "by EstacionKUS",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceMuted,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(48.dp))

            // Main CTA
            Button(
                onClick = onStartStream,
                modifier = Modifier.fillMaxWidth(0.65f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CameraRed),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.FiberManualRecord, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Ir en vivo", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(16.dp))

            // Secondary actions
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionButton(
                    icon = Icons.Default.PhotoLibrary,
                    label = "Medios",
                    onClick = onOpenMedia
                )
                QuickActionButton(
                    icon = Icons.Default.Settings,
                    label = "Ajustes",
                    onClick = onOpenSettings
                )
            }

            Spacer(Modifier.height(40.dp))

            // Quick status row
            val platforms = listOf("YouTube", "Twitch", "Facebook", "Kick", "TikTok")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(platforms) { name ->
                    Surface(
                        color = Surface700,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            name,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Surface600)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
