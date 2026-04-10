package com.cushMedia.camerastream.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cushMedia.camerastream.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val settings by settingsViewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración de Stream") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
            // SRT Section
            Text("SRT", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = settings.srtHost,
                onValueChange = { settingsViewModel.updateSrtHost(it) },
                label = { Text("Host SRT") },
                placeholder = { Text("srt://host:port") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = settings.srtStreamId,
                onValueChange = { settingsViewModel.updateSrtStreamId(it) },
                label = { Text("Stream ID") },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            // RTMP Section
            Text("RTMP (Redes Sociales)", style = MaterialTheme.typography.titleMedium)

            StreamPlatformCard(
                title = "YouTube Live",
                url = settings.youtubeRtmpUrl,
                key = settings.youtubeStreamKey,
                enabled = settings.youtubeEnabled,
                onUrlChange = { settingsViewModel.updateYoutubeUrl(it) },
                onKeyChange = { settingsViewModel.updateYoutubeKey(it) },
                onToggle = { settingsViewModel.toggleYoutube() }
            )

            StreamPlatformCard(
                title = "Twitch",
                url = settings.twitchRtmpUrl,
                key = settings.twitchStreamKey,
                enabled = settings.twitchEnabled,
                onUrlChange = { settingsViewModel.updateTwitchUrl(it) },
                onKeyChange = { settingsViewModel.updateTwitchKey(it) },
                onToggle = { settingsViewModel.toggleTwitch() }
            )

            StreamPlatformCard(
                title = "Facebook Live",
                url = settings.facebookRtmpUrl,
                key = settings.facebookStreamKey,
                enabled = settings.facebookEnabled,
                onUrlChange = { settingsViewModel.updateFacebookUrl(it) },
                onKeyChange = { settingsViewModel.updateFacebookKey(it) },
                onToggle = { settingsViewModel.toggleFacebook() }
            )

            HorizontalDivider()

            // Video quality
            Text("Calidad de Video", style = MaterialTheme.typography.titleMedium)
            val resolutions = listOf("720p (1280x720)", "1080p (1920x1080)", "480p (854x480)")
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = settings.resolution,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Resolución") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    resolutions.forEach { res ->
                        DropdownMenuItem(
                            text = { Text(res) },
                            onClick = { settingsViewModel.updateResolution(res); expanded = false }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = settings.videoBitrate,
                onValueChange = { settingsViewModel.updateBitrate(it) },
                label = { Text("Bitrate de video (kbps)") },
                placeholder = { Text("2500") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { settingsViewModel.saveSettings() },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Guardar configuración")
            }
        }
    }
}

@Composable
fun StreamPlatformCard(
    title: String,
    url: String,
    key: String,
    enabled: Boolean,
    onUrlChange: (String) -> Unit,
    onKeyChange: (String) -> Unit,
    onToggle: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Switch(checked = enabled, onCheckedChange = { onToggle() })
            }
            if (enabled) {
                OutlinedTextField(
                    value = url,
                    onValueChange = onUrlChange,
                    label = { Text("RTMP URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = key,
                    onValueChange = onKeyChange,
                    label = { Text("Stream Key") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
