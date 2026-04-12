package tech.estacionkus.camerastream.ui.screens.editor

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoEditorScreen(
    onBack: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val videoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.addClip(it) }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.addAudioLayer(it, "Background Music") }
    }

    // ExoPlayer for preview
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    // Update player when clips change
    LaunchedEffect(uiState.project.clips) {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        uiState.project.clips.forEach { clip ->
            val clipping = MediaItem.ClippingConfiguration.Builder()
                .setStartPositionMs(clip.startMs)
                .setEndPositionMs(clip.endMs)
                .build()
            val mediaItem = MediaItem.Builder()
                .setUri(clip.uri)
                .setClippingConfiguration(clipping)
                .build()
            exoPlayer.addMediaItem(mediaItem)
        }
        if (uiState.project.clips.isNotEmpty()) {
            exoPlayer.prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.dismissError()
        }
    }

    // Export dialog
    if (uiState.isExporting) {
        ExportProgressDialog(
            progress = uiState.exportProgress,
            onCancel = { viewModel.cancelExport() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Video Editor") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.undo() }, enabled = viewModel.canUndo) {
                        Icon(Icons.Default.Undo, "Undo", tint = if (viewModel.canUndo) Color.White else Color.Gray)
                    }
                    IconButton(onClick = { viewModel.redo() }, enabled = viewModel.canRedo) {
                        Icon(Icons.Default.Redo, "Redo", tint = if (viewModel.canRedo) Color.White else Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A2E),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF121212))
        ) {
            // Video Preview (top half)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.project.clips.isEmpty()) {
                    // Empty state — add video prompt
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.VideoLibrary,
                            null,
                            tint = Color.White.copy(0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("Add a video to start editing", color = Color.White.copy(0.5f))
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { videoPickerLauncher.launch("video/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add Video")
                        }
                    }
                } else {
                    // ExoPlayer preview
                    AndroidView(
                        factory = {
                            PlayerView(it).apply {
                                player = exoPlayer
                                useController = true
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Timeline (middle section)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.25f)
                    .background(Color(0xFF1E1E2E))
            ) {
                TimelineView(
                    project = uiState.project,
                    thumbnails = uiState.thumbnails,
                    playbackPositionMs = uiState.playbackPositionMs,
                    zoomLevel = uiState.zoomLevel,
                    selectedClipIndex = uiState.selectedClipIndex,
                    onClipSelected = { viewModel.selectClip(it) },
                    onPositionChanged = { viewModel.setPlaybackPosition(it) },
                    onAddClip = { videoPickerLauncher.launch("video/*") }
                )
            }

            // Tool bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A2E))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                EditorTool.entries.forEach { tool ->
                    val isSelected = uiState.currentTool == tool
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectTool(tool) },
                        label = { Text(tool.label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFE53935),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Tool panel (bottom)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f)
                    .background(Color(0xFF1A1A2E))
            ) {
                when (uiState.currentTool) {
                    EditorTool.TRIM -> TrimPanel(
                        clip = uiState.project.clips.getOrNull(uiState.selectedClipIndex),
                        onTrim = { start, end ->
                            viewModel.trimClip(uiState.selectedClipIndex, start, end)
                        }
                    )
                    EditorTool.TEXT -> TextEditorPanel(
                        textLayers = uiState.project.textLayers,
                        selectedId = uiState.selectedTextLayerId,
                        onAddText = { viewModel.addTextLayer(it) },
                        onUpdateText = { id, transform -> viewModel.updateTextLayer(id, transform) },
                        onRemoveText = { viewModel.removeTextLayer(it) }
                    )
                    EditorTool.TRANSITIONS -> TransitionPanel(
                        clips = uiState.project.clips,
                        transitions = uiState.project.transitions,
                        onSetTransition = { index, transition ->
                            viewModel.setTransition(index, transition)
                        }
                    )
                    EditorTool.FILTERS -> FilterPanel(
                        clip = uiState.project.clips.getOrNull(uiState.selectedClipIndex),
                        onSetFilter = { name, intensity ->
                            viewModel.setClipFilter(uiState.selectedClipIndex, name, intensity)
                        }
                    )
                    EditorTool.AUDIO -> AudioPanel(
                        clip = uiState.project.clips.getOrNull(uiState.selectedClipIndex),
                        audioLayers = uiState.project.audioLayers,
                        onSetVolume = { viewModel.setClipVolume(uiState.selectedClipIndex, it) },
                        onAddMusic = { audioPickerLauncher.launch("audio/*") },
                        onRemoveAudio = { viewModel.removeAudioLayer(it) },
                        onUpdateAudio = { id, transform -> viewModel.updateAudioLayer(id, transform) }
                    )
                    EditorTool.SPEED -> SpeedPanel(
                        clip = uiState.project.clips.getOrNull(uiState.selectedClipIndex),
                        onSetSpeed = { viewModel.setClipSpeed(uiState.selectedClipIndex, it) }
                    )
                    EditorTool.TEMPLATES -> TemplatePanel(
                        onApply = { viewModel.applyTemplate(it) }
                    )
                    EditorTool.EXPORT -> ExportPanel(
                        project = uiState.project,
                        quality = uiState.exportQuality,
                        resolution = uiState.exportResolution,
                        exportedUri = uiState.exportedUri,
                        onSetQuality = { viewModel.setExportQuality(it) },
                        onSetResolution = { viewModel.setExportResolution(it) },
                        onExport = { viewModel.startExport() }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExportProgressDialog(progress: Float, onCancel: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Exporting Video...") },
        text = {
            Column {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFE53935)
                )
                Spacer(Modifier.height(8.dp))
                Text("${(progress * 100).toInt()}%")
            }
        },
        confirmButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    )
}
