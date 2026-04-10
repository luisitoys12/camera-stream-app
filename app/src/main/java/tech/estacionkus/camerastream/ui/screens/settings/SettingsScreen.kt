package tech.estacionkus.camerastream.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.estacionkus.camerastream.domain.model.Platform

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Platform selector
            Text("Plataforma RTMP", style = MaterialTheme.typography.titleSmall)
            var expandedPlatform by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedPlatform,
                onExpandedChange = { expandedPlatform = it }
            ) {
                OutlinedTextField(
                    value = uiState.platform,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Plataforma") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPlatform) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expandedPlatform, onDismissRequest = { expandedPlatform = false }) {
                    Platform.entries.forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p.displayName) },
                            onClick = {
                                viewModel.setPlatform(p)
                                expandedPlatform = false
                            }
                        )
                    }
                }
            }

            // RTMP URL
            OutlinedTextField(
                value = uiState.rtmpUrl,
                onValueChange = viewModel::setRtmpUrl,
                label = { Text("RTMP URL") },
                placeholder = { Text("rtmp://...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Stream key
            OutlinedTextField(
                value = uiState.streamKey,
                onValueChange = viewModel::setStreamKey,
                label = { Text("Stream Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            HorizontalDivider()

            // Bitrate
            Text("Calidad de video", style = MaterialTheme.typography.titleSmall)
            Text("Bitrate: ${uiState.bitrateKbps} kbps", style = MaterialTheme.typography.bodySmall)
            Slider(
                value = uiState.bitrateKbps.toFloat(),
                onValueChange = { viewModel.setBitrate(it.toInt()) },
                valueRange = 500f..5000f,
                steps = 8,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            // Save button
            Button(
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar configuración") }
        }
    }
}
