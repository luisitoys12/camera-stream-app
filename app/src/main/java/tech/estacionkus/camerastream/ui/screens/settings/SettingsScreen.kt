package tech.estacionkus.camerastream.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.estacionkus.camerastream.domain.model.Platform

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var platformExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) {
            Toast.makeText(context, "Settings saved", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stream Settings") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = { viewModel.save() }) {
                        Icon(Icons.Default.Save, "Save", tint = Color(0xFFE53935))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Platform Selection
            Text("Platform", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            ExposedDropdownMenuBox(
                expanded = platformExpanded,
                onExpandedChange = { platformExpanded = !platformExpanded }
            ) {
                OutlinedTextField(
                    value = uiState.platform,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = platformExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    label = { Text("Streaming Platform") }
                )
                ExposedDropdownMenu(
                    expanded = platformExpanded,
                    onDismissRequest = { platformExpanded = false }
                ) {
                    Platform.entries.forEach { platform ->
                        DropdownMenuItem(
                            text = { Text(platform.displayName) },
                            onClick = {
                                viewModel.setPlatform(platform)
                                platformExpanded = false
                            }
                        )
                    }
                }
            }

            // RTMP URL
            OutlinedTextField(
                value = uiState.rtmpUrl,
                onValueChange = { viewModel.setRtmpUrl(it) },
                label = { Text("RTMP URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("rtmp://live.twitch.tv/app/") }
            )

            // Stream Key
            OutlinedTextField(
                value = uiState.streamKey,
                onValueChange = { viewModel.setStreamKey(it) },
                label = { Text("Stream Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                placeholder = { Text("Your stream key") }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Video Settings
            Text("Video", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            // Resolution
            Text("Resolution", fontSize = 14.sp, color = Color(0xFF888888))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("720p", "1080p", "1440p").forEach { res ->
                    FilterChip(
                        selected = uiState.resolution == res,
                        onClick = { viewModel.setResolution(res) },
                        label = { Text(res) }
                    )
                }
            }

            // FPS
            Text("Frame Rate", fontSize = 14.sp, color = Color(0xFF888888))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(24, 30, 60).forEach { fps ->
                    FilterChip(
                        selected = uiState.fps == fps,
                        onClick = { viewModel.setFps(fps) },
                        label = { Text("$fps fps") }
                    )
                }
            }

            // Video Bitrate
            Text("Video Bitrate: ${uiState.bitrateKbps} kbps", fontSize = 14.sp, color = Color(0xFF888888))
            Slider(
                value = uiState.bitrateKbps.toFloat(),
                onValueChange = { viewModel.setBitrate(it.toInt()) },
                valueRange = 500f..8000f,
                steps = 14,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFE53935),
                    activeTrackColor = Color(0xFFE53935)
                )
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Audio Settings
            Text("Audio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Text("Audio Bitrate: ${uiState.audioBitrateKbps} kbps", fontSize = 14.sp, color = Color(0xFF888888))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(64, 128, 256).forEach { abr ->
                    FilterChip(
                        selected = uiState.audioBitrateKbps == abr,
                        onClick = { viewModel.setAudioBitrate(abr) },
                        label = { Text("$abr kbps") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
            ) {
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(8.dp))
                Text("Save Settings", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
