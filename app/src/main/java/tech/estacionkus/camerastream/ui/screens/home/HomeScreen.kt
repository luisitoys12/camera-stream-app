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
    viewModel: HomeViewModel = hiltViewModel()
) {
    val planName by viewModel.planName.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("CameraStream Pro", fontWeight = FontWeight.Bold)
                        Text(
                            "$planName Plan",
                            fontSize = 12.sp,
                            color = when (planName) {
                                "Pro" -> Color(0xFFE53935)
                                "Agency" -> Color(0xFF6A1B9A)
                                else -> Color.Gray
                            }
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onUpgrade) {
                        Icon(Icons.Default.Star, "Upgrade", tint = Color(0xFFFFD700))
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(8.dp))

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

            // Feature grid
            Text("Stream Tools", fontWeight = FontWeight.Bold, fontSize = 16.sp)

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

            Text("Pro Features", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 4.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureCard("SRT Server", Icons.Default.Wifi, Color(0xFF607D8B), Modifier.weight(1f), onOpenMedia)
                FeatureCard("Camera Pro", Icons.Default.CameraAlt, Color(0xFF795548), Modifier.weight(1f), onOpenManualCam)
            }

            OutlinedButton(
                onClick = onUpgrade,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700))
                Spacer(Modifier.width(8.dp))
                Text("Upgrade Plan")
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
