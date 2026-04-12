package tech.estacionkus.camerastream.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartStream: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenMedia: () -> Unit,
    onUpgrade: () -> Unit,
    onOpenStudio: () -> Unit = {},
    onOpenScenes: () -> Unit = {},
    onOpenChat: () -> Unit = {},
    onOpenSports: () -> Unit = {},
    onOpenHealth: () -> Unit = {},
    onOpenGuest: () -> Unit = {},
    onOpenManualCam: () -> Unit = {},
    onOpenFilters: () -> Unit = {},
    onOpenRadio: () -> Unit = {},
    onOpenEditor: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val planName by viewModel.planName.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "CameraStream",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                    Text(
                        "Radio Broadcast Beta",
                        fontSize = 12.sp,
                        color = Color.White.copy(0.5f)
                    )
                }
                Row {
                    Surface(
                        color = when (planName) {
                            "Pro" -> Color(0xFFE53935).copy(0.2f)
                            "Agency" -> Color(0xFF6A1B9A).copy(0.2f)
                            else -> Color.White.copy(0.1f)
                        },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            "$planName Plan",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = when (planName) {
                                "Pro" -> Color(0xFFE53935)
                                "Agency" -> Color(0xFF6A1B9A)
                                else -> Color.White.copy(0.7f)
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // GO LIVE button
            Button(
                onClick = onStartStream,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(8.dp))
                Text("GO LIVE", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(4.dp))

            // Stream Tools
            Text("Stream Tools", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureCard("My Studio", Icons.Default.Dashboard, Color(0xFF2196F3), Modifier.weight(1f), onOpenStudio)
                FeatureCard("Scenes", Icons.Default.ViewCarousel, Color(0xFF9C27B0), Modifier.weight(1f), onOpenScenes)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureCard("Chat", Icons.Default.Chat, Color(0xFF4CAF50), Modifier.weight(1f), onOpenChat)
                FeatureCard("Sports", Icons.Default.SportsSoccer, Color(0xFFFF9800), Modifier.weight(1f), onOpenSports)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureCard("Health", Icons.Default.MonitorHeart, Color(0xFF00BCD4), Modifier.weight(1f), onOpenHealth)
                FeatureCard("Guests", Icons.Default.Groups, Color(0xFFE91E63), Modifier.weight(1f), onOpenGuest)
            }

            // New v3 features
            Text("New in v3", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF64B5F6), modifier = Modifier.padding(top = 4.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureCard("Editor", Icons.Default.MovieCreation, Color(0xFF00BCD4), Modifier.weight(1f), onOpenEditor)
                FeatureCard("Filters", Icons.Default.FilterVintage, Color(0xFFAB47BC), Modifier.weight(1f), onOpenFilters)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureCard("Radio", Icons.Default.Radio, Color(0xFFE53935), Modifier.weight(1f), onOpenRadio)
                FeatureCard("SRT Server", Icons.Default.Wifi, Color(0xFF607D8B), Modifier.weight(1f), onOpenMedia)
            }

            Text("Pro Features", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White, modifier = Modifier.padding(top = 4.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureCard("Camera Pro", Icons.Default.CameraAlt, Color(0xFF795548), Modifier.weight(1f), onOpenManualCam)
                Spacer(Modifier.weight(1f))
            }

            OutlinedButton(
                onClick = onUpgrade,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder(true).copy(
                    brush = Brush.horizontalGradient(listOf(Color(0xFFE53935), Color(0xFF6A1B9A)))
                )
            ) {
                Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700))
                Spacer(Modifier.width(8.dp))
                Text("Upgrade Plan", color = Color.White)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(4.dp))
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = color)
        }
    }
}
