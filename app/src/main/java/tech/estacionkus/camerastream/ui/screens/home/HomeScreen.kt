package tech.estacionkus.camerastream.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.estacionkus.camerastream.ui.theme.*

@Composable
fun HomeScreen(
    onStartStream: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenMedia: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface900)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("CameraStream", style = MaterialTheme.typography.headlineMedium,
                        color = OnSurface, fontWeight = FontWeight.Bold)
                    Text("by EstacionKUS", style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceMuted, letterSpacing = 1.5.sp)
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Default.Settings, null, tint = OnSurfaceMuted)
                }
            }

            // Plan badge
            Surface(
                color = CameraRed.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Verified, null, tint = CameraRed, modifier = Modifier.size(16.dp))
                    Text(
                        text = when (uiState.planId) {
                            "pro" -> "Plan Pro activo"
                            "starter" -> "Plan Starter activo"
                            "agency" -> "Plan Agency activo"
                            else -> "Plan Free"
                        },
                        color = CameraRed, fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Go Live button
            Button(
                onClick = onStartStream,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CameraRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("Ir en vivo", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            // Quick actions grid
            Text("Acciones rápidas", style = MaterialTheme.typography.titleSmall, color = OnSurfaceMuted)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickCard("Configurar", Icons.Default.Tune, Modifier.weight(1f), onClick = onOpenSettings)
                QuickCard("Medios", Icons.Default.VideoLibrary, Modifier.weight(1f), onClick = onOpenMedia)
                QuickCard(
                    title = if (uiState.planId == null) "Activar" else "Cuenta",
                    icon = Icons.Default.CardMembership,
                    modifier = Modifier.weight(1f),
                    onClick = viewModel::onAccountTap
                )
            }

            // Stats (last session)
            if (uiState.lastSessionDuration > 0) {
                Surface(
                    color = Surface700,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem("Última sesión", uiState.lastSessionDuration.formatDuration())
                        StatItem("Resolución", uiState.features.maxResolution.label)
                        StatItem("Plataformas", "${uiState.features.maxPlatforms} máx")
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickCard(title: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        color = Surface700,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, null, tint = CameraRed, modifier = Modifier.size(24.dp))
            Text(title, fontSize = 12.sp, color = OnSurface)
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = OnSurface, fontSize = 14.sp)
        Text(label, fontSize = 10.sp, color = OnSurfaceMuted)
    }
}

private fun Long.formatDuration(): String {
    val h = this / 3600; val m = (this % 3600) / 60; val s = this % 60
    return if (h > 0) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
