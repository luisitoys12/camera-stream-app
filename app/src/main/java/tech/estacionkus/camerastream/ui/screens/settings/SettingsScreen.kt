package tech.estacionkus.camerastream.ui.screens.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.estacionkus.camerastream.data.settings.StreamSettings
import tech.estacionkus.camerastream.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface800)
            )
        },
        containerColor = Surface900
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SRT Section
            SettingsSection(title = "SRT", icon = Icons.Default.Wifi) {
                StreamToggle("Activar SRT", settings.enableSrt) { viewModel.save(settings.copy(enableSrt = it)) }
                if (settings.enableSrt) {
                    StreamKeyField("Host / IP", settings.srtHost) { viewModel.save(settings.copy(srtHost = it)) }
                    StreamKeyField("Puerto", settings.srtPort.toString()) { viewModel.save(settings.copy(srtPort = it.toIntOrNull() ?: 9998)) }
                    StreamKeyField("Latencia (ms)", settings.srtLatencyMs.toString()) { viewModel.save(settings.copy(srtLatencyMs = it.toIntOrNull() ?: 200)) }
                    StreamKeyField("Passphrase", settings.srtPassphrase, secret = true) { viewModel.save(settings.copy(srtPassphrase = it)) }
                }
            }

            // Platform Sections
            PlatformSection("YouTube", settings.enableYoutube, settings.youtubeKey,
                onToggle = { viewModel.save(settings.copy(enableYoutube = it)) },
                onKey = { viewModel.save(settings.copy(youtubeKey = it)) }
            )
            PlatformSection("Twitch", settings.enableTwitch, settings.twitchKey,
                onToggle = { viewModel.save(settings.copy(enableTwitch = it)) },
                onKey = { viewModel.save(settings.copy(twitchKey = it)) }
            )
            PlatformSection("Facebook", settings.enableFacebook, settings.facebookKey,
                onToggle = { viewModel.save(settings.copy(enableFacebook = it)) },
                onKey = { viewModel.save(settings.copy(facebookKey = it)) }
            )
            PlatformSection("Kick", settings.enableKick, settings.kickKey,
                onToggle = { viewModel.save(settings.copy(enableKick = it)) },
                onKey = { viewModel.save(settings.copy(kickKey = it)) }
            )
            PlatformSection("TikTok", settings.enableTiktok, settings.tiktokKey,
                onToggle = { viewModel.save(settings.copy(enableTiktok = it)) },
                onKey = { viewModel.save(settings.copy(tiktokKey = it)) }
            )

            // Video Quality
            SettingsSection(title = "Calidad de video", icon = Icons.Default.Videocam) {
                val resOptions = listOf("1920x1080", "1280x720", "854x480")
                ResolutionDropdown(settings.resolution, resOptions) { viewModel.save(settings.copy(resolution = it)) }
                StreamKeyField("FPS", settings.fps.toString()) { viewModel.save(settings.copy(fps = it.toIntOrNull() ?: 30)) }
                StreamKeyField("Bitrate de video (kbps)", settings.videoBitrateKbps.toString()) {
                    viewModel.save(settings.copy(videoBitrateKbps = it.toIntOrNull() ?: 4000))
                }
                StreamKeyField("Bitrate de audio (kbps)", settings.audioBitrateKbps.toString()) {
                    viewModel.save(settings.copy(audioBitrateKbps = it.toIntOrNull() ?: 128))
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(color = Surface800, shape = MaterialTheme.shapes.medium) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = CameraRed, modifier = Modifier.size(18.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, color = OnSurface)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun PlatformSection(
    name: String, enabled: Boolean, key: String,
    onToggle: (Boolean) -> Unit, onKey: (String) -> Unit
) {
    SettingsSection(title = name, icon = Icons.Default.Stream) {
        StreamToggle("Activar $name", enabled, onToggle)
        if (enabled) {
            StreamKeyField("Stream Key", key, secret = true, onValue = onKey)
        }
    }
}

@Composable
private fun StreamToggle(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = OnSurface)
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun StreamKeyField(label: String, value: String, secret: Boolean = false, onValue: (String) -> Unit) {
    var showSecret by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (secret && !showSecret) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (secret) {{
            IconButton(onClick = { showSecret = !showSecret }) {
                Icon(if (showSecret) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
            }
        }} else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CameraRed,
            unfocusedBorderColor = Surface600,
            focusedLabelColor = CameraRed
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResolutionDropdown(current: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = current,
            onValueChange = {},
            readOnly = true,
            label = { Text("Resolución") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CameraRed)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { res ->
                DropdownMenuItem(
                    text = { Text(res) },
                    onClick = { onSelect(res); expanded = false }
                )
            }
        }
    }
}
