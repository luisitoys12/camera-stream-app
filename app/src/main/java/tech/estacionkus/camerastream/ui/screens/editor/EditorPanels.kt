package tech.estacionkus.camerastream.ui.screens.editor

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

// ---- Trim Panel ----

@Composable
fun TrimPanel(
    clip: VideoClip?,
    onTrim: (Long, Long) -> Unit
) {
    if (clip == null) {
        EmptyToolMessage("Select a clip to trim")
        return
    }

    var startMs by remember(clip.id) { mutableLongStateOf(clip.startMs) }
    var endMs by remember(clip.id) { mutableLongStateOf(clip.endMs) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Trim Clip", color = Color.White, fontWeight = FontWeight.Bold)

        Text("Start: ${formatMs(startMs)}", color = Color.White, fontSize = 13.sp)
        Slider(
            value = startMs.toFloat(),
            onValueChange = {
                startMs = it.toLong()
                if (startMs >= endMs) endMs = (startMs + 1000).coerceAtMost(clip.originalDurationMs)
                onTrim(startMs, endMs)
            },
            valueRange = 0f..clip.originalDurationMs.toFloat(),
            colors = SliderDefaults.colors(thumbColor = Color(0xFF2196F3), activeTrackColor = Color(0xFF2196F3))
        )

        Text("End: ${formatMs(endMs)}", color = Color.White, fontSize = 13.sp)
        Slider(
            value = endMs.toFloat(),
            onValueChange = {
                endMs = it.toLong()
                if (endMs <= startMs) startMs = (endMs - 1000).coerceAtLeast(0)
                onTrim(startMs, endMs)
            },
            valueRange = 0f..clip.originalDurationMs.toFloat(),
            colors = SliderDefaults.colors(thumbColor = Color(0xFFE53935), activeTrackColor = Color(0xFFE53935))
        )

        Text(
            "Duration: ${formatMs(endMs - startMs)} / ${formatMs(clip.originalDurationMs)}",
            color = Color.White.copy(0.6f),
            fontSize = 12.sp
        )
    }
}

// ---- Text Editor Panel ----

@Composable
fun TextEditorPanel(
    textLayers: List<TextLayer>,
    selectedId: String?,
    onAddText: (String) -> Unit,
    onUpdateText: (String, TextLayer.() -> TextLayer) -> Unit,
    onRemoveText: (String) -> Unit
) {
    var newText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Text Overlays", color = Color.White, fontWeight = FontWeight.Bold)

        // Add text
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newText,
                onValueChange = { newText = it },
                label = { Text("Text") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF64B5F6),
                    unfocusedBorderColor = Color.Gray
                )
            )
            Button(
                onClick = {
                    if (newText.isNotBlank()) {
                        onAddText(newText)
                        newText = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
            ) {
                Icon(Icons.Default.Add, null)
            }
        }

        // Existing text layers
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(textLayers) { layer ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (layer.id == selectedId) Color(0xFF1565C0) else Color(0xFF2A2A3E)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(layer.text, color = Color.White, fontSize = 13.sp, maxLines = 1)
                            Text(
                                "${formatMs(layer.startMs)} - ${formatMs(layer.endMs)} | ${layer.animation.label}",
                                color = Color.White.copy(0.5f),
                                fontSize = 10.sp
                            )
                        }
                        // Animation selector
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            TextButton(onClick = { expanded = true }) {
                                Text(layer.animation.label, color = Color(0xFF64B5F6), fontSize = 11.sp)
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                TextAnimation.entries.forEach { anim ->
                                    DropdownMenuItem(
                                        text = { Text(anim.label) },
                                        onClick = {
                                            onUpdateText(layer.id) { copy(animation = anim) }
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        IconButton(onClick = { onRemoveText(layer.id) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, "Remove", tint = Color(0xFFE53935))
                        }
                    }
                }
            }
        }
    }
}

// ---- Transition Panel ----

@Composable
fun TransitionPanel(
    clips: List<VideoClip>,
    transitions: Map<Int, Transition>,
    onSetTransition: (Int, Transition) -> Unit
) {
    if (clips.size < 2) {
        EmptyToolMessage("Add at least 2 clips to use transitions")
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Transitions", color = Color.White, fontWeight = FontWeight.Bold)

        for (i in 0 until clips.size - 1) {
            val current = transitions[i] ?: Transition()
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Between Clip ${i + 1} and ${i + 2}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(TransitionType.entries) { type ->
                            FilterChip(
                                selected = current.type == type,
                                onClick = {
                                    onSetTransition(i, Transition(type, current.durationMs))
                                },
                                label = { Text(type.label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF6A1B9A),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    if (current.type != TransitionType.NONE) {
                        Spacer(Modifier.height(8.dp))
                        Text("Duration: ${current.durationMs}ms", color = Color.White.copy(0.6f), fontSize = 11.sp)
                        Slider(
                            value = current.durationMs.toFloat(),
                            onValueChange = {
                                onSetTransition(i, current.copy(durationMs = it.toLong()))
                            },
                            valueRange = 300f..2000f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF6A1B9A),
                                activeTrackColor = Color(0xFF6A1B9A)
                            )
                        )
                    }
                }
            }
        }
    }
}

// ---- Filter Panel ----

@Composable
fun FilterPanel(
    clip: VideoClip?,
    onSetFilter: (String?, Float) -> Unit
) {
    if (clip == null) {
        EmptyToolMessage("Select a clip to apply filters")
        return
    }

    val filters = listOf(
        "None" to null,
        "Vivid" to "vivid",
        "Warm" to "warm",
        "Cool" to "cool",
        "Dramatic" to "dramatic",
        "Vintage" to "vintage",
        "B&W" to "blackwhite",
        "Sepia" to "sepia",
        "Cinema" to "cinema",
        "Teal Orange" to "tealorange",
        "Pastel" to "pastel",
        "Moody" to "moody",
        "High Contrast" to "highcontrast",
        "Low Contrast" to "lowcontrast",
        "Saturation+" to "satup",
        "Desaturate" to "desat",
        "Golden Hour" to "goldenhour",
        "Neon" to "neon",
        "Film Noir" to "filmnoir",
        "Dream" to "dream",
        "Cyberpunk" to "cyberpunk",
        "Soft Focus" to "softfocus",
        "Sharpen" to "sharpen",
        "Chrome" to "chrome",
        "Fade" to "fade",
        "Matte" to "matte",
        "Glow" to "glow",
        "Cross Process" to "crossprocess",
        "Bleach Bypass" to "bleachbypass",
        "Pop Art" to "popart",
        "Lo-fi" to "lofi"
    )

    var intensity by remember(clip.id) { mutableFloatStateOf(clip.filterIntensity) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Filters", color = Color.White, fontWeight = FontWeight.Bold)

        if (clip.filterName != null) {
            Text("Intensity", color = Color.White, fontSize = 12.sp)
            Slider(
                value = intensity,
                onValueChange = {
                    intensity = it
                    onSetFilter(clip.filterName, it)
                },
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF9C27B0),
                    activeTrackColor = Color(0xFF9C27B0)
                )
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filters) { (label, name) ->
                val isSelected = clip.filterName == name
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable { onSetFilter(name, intensity) },
                    color = if (isSelected) Color(0xFF9C27B0) else Color(0xFF2A2A3E),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            label,
                            color = if (isSelected) Color.White else Color.White.copy(0.7f),
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

// ---- Audio Panel ----

@Composable
fun AudioPanel(
    clip: VideoClip?,
    audioLayers: List<AudioLayer>,
    onSetVolume: (Float) -> Unit,
    onAddMusic: () -> Unit,
    onRemoveAudio: (String) -> Unit,
    onUpdateAudio: (String, AudioLayer.() -> AudioLayer) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Audio", color = Color.White, fontWeight = FontWeight.Bold)

        // Clip volume
        if (clip != null) {
            Text("Clip Volume", color = Color.White, fontSize = 13.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (clip.volume == 0f) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    null, tint = Color.White
                )
                Slider(
                    value = clip.volume,
                    onValueChange = onSetVolume,
                    valueRange = 0f..2f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF2196F3),
                        activeTrackColor = Color(0xFF2196F3)
                    )
                )
                Text("${(clip.volume * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
            }
        }

        Divider(color = Color.White.copy(0.1f))

        // Background music
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Background Music", color = Color.White, fontSize = 13.sp)
            Button(
                onClick = onAddMusic,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add", fontSize = 12.sp)
            }
        }

        audioLayers.forEach { layer ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (layer.isVoiceover) Icons.Default.Mic else Icons.Default.MusicNote,
                                null,
                                tint = if (layer.isVoiceover) Color(0xFF4CAF50) else Color(0xFF2196F3),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(layer.name.ifBlank { "Audio" }, color = Color.White, fontSize = 12.sp)
                        }
                        IconButton(onClick = { onRemoveAudio(layer.id) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Delete, "Remove", tint = Color(0xFFE53935), modifier = Modifier.size(16.dp))
                        }
                    }
                    // Volume
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Vol:", color = Color.White.copy(0.6f), fontSize = 10.sp)
                        Slider(
                            value = layer.volume,
                            onValueChange = { v -> onUpdateAudio(layer.id) { copy(volume = v) } },
                            valueRange = 0f..2f,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Fade
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = layer.fadeInMs > 0,
                            onClick = {
                                onUpdateAudio(layer.id) {
                                    copy(fadeInMs = if (fadeInMs > 0) 0 else 1000)
                                }
                            },
                            label = { Text("Fade In", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = layer.fadeOutMs > 0,
                            onClick = {
                                onUpdateAudio(layer.id) {
                                    copy(fadeOutMs = if (fadeOutMs > 0) 0 else 1000)
                                }
                            },
                            label = { Text("Fade Out", fontSize = 10.sp) }
                        )
                    }
                }
            }
        }
    }
}

// ---- Speed Panel ----

@Composable
fun SpeedPanel(
    clip: VideoClip?,
    onSetSpeed: (Float) -> Unit
) {
    if (clip == null) {
        EmptyToolMessage("Select a clip to change speed")
        return
    }

    val speeds = listOf(0.25f, 0.5f, 0.75f, 1f, 1.5f, 2f, 3f, 4f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Speed Control", color = Color.White, fontWeight = FontWeight.Bold)
        Text("Current: ${clip.speed}x", color = Color.White.copy(0.7f), fontSize = 13.sp)

        // Speed presets
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(speeds) { speed ->
                val isSelected = clip.speed == speed
                Surface(
                    modifier = Modifier
                        .size(60.dp)
                        .clickable { onSetSpeed(speed) },
                    color = if (isSelected) Color(0xFFFF9800) else Color(0xFF2A2A3E),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "${speed}x",
                            color = Color.White,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Custom slider
        Slider(
            value = clip.speed,
            onValueChange = onSetSpeed,
            valueRange = 0.25f..4f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFFF9800),
                activeTrackColor = Color(0xFFFF9800)
            )
        )

        Text(
            "Duration at ${clip.speed}x: ${formatMs(clip.trimmedDurationMs)}",
            color = Color.White.copy(0.5f),
            fontSize = 12.sp
        )
    }
}

// ---- Template Panel ----

@Composable
fun TemplatePanel(
    onApply: (EditorTemplate) -> Unit
) {
    val templates = remember { getBuiltInTemplates() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Templates", color = Color.White, fontWeight = FontWeight.Bold)

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(templates) { template ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onApply(template) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Surface(
                            color = categoryColor(template.category).copy(0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                template.category.label,
                                color = categoryColor(template.category),
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(template.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text(template.description, color = Color.White.copy(0.5f), fontSize = 10.sp, maxLines = 2)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${template.aspectRatio.label} | ~${template.suggestedDurationSec}s",
                            color = Color.White.copy(0.4f),
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

// ---- Export Panel ----

@Composable
fun ExportPanel(
    project: EditorProject,
    quality: ExportQuality,
    resolution: ExportResolution,
    exportedUri: Uri?,
    onSetQuality: (ExportQuality) -> Unit,
    onSetResolution: (ExportResolution) -> Unit,
    onExport: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Export", color = Color.White, fontWeight = FontWeight.Bold)

        // Resolution
        Text("Resolution", color = Color.White, fontSize = 13.sp)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ExportResolution.entries) { res ->
                FilterChip(
                    selected = resolution == res,
                    onClick = { onSetResolution(res) },
                    label = { Text(res.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE53935),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Quality
        Text("Quality", color = Color.White, fontSize = 13.sp)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ExportQuality.entries) { q ->
                FilterChip(
                    selected = quality == q,
                    onClick = { onSetQuality(q) },
                    label = { Text(q.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2196F3),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Info
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Output: MP4 (H.264/AAC)", color = Color.White, fontSize = 12.sp)
                Text("Clips: ${project.clips.size}", color = Color.White.copy(0.6f), fontSize = 12.sp)
                Text("Duration: ${formatMs(project.totalDurationMs)}", color = Color.White.copy(0.6f), fontSize = 12.sp)
                Text("Resolution: ${resolution.width}x${resolution.height}", color = Color.White.copy(0.6f), fontSize = 12.sp)
            }
        }

        // Export button
        Button(
            onClick = onExport,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
            enabled = project.clips.isNotEmpty(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Download, null)
            Spacer(Modifier.width(8.dp))
            Text("Export Video", fontWeight = FontWeight.Bold)
        }

        // Share/save after export
        if (exportedUri != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Export Complete!", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                        Text("Saved to Gallery", color = Color.White.copy(0.6f), fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "video/mp4"
                                putExtra(Intent.EXTRA_STREAM, exportedUri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Video").apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            })
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Share")
                    }
                }
            }
        }
    }
}

// ---- Helpers ----

@Composable
fun EmptyToolMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(message, color = Color.White.copy(0.5f))
    }
}

private fun formatMs(ms: Long): String {
    val sec = ms / 1000
    val min = sec / 60
    val s = sec % 60
    return "%d:%02d".format(min, s)
}

private fun categoryColor(category: TemplateCategory): Color = when (category) {
    TemplateCategory.INTRO -> Color(0xFFE53935)
    TemplateCategory.OUTRO -> Color(0xFF9C27B0)
    TemplateCategory.SOCIAL -> Color(0xFFFF4081)
    TemplateCategory.TUTORIAL -> Color(0xFF2196F3)
    TemplateCategory.GAMING -> Color(0xFF4CAF50)
    TemplateCategory.VLOG -> Color(0xFFFF9800)
    TemplateCategory.PRODUCT -> Color(0xFF795548)
    TemplateCategory.NEWS -> Color(0xFF607D8B)
    TemplateCategory.EVENT -> Color(0xFFFFEB3B)
    TemplateCategory.PROMO -> Color(0xFF00BCD4)
}

fun getBuiltInTemplates(): List<EditorTemplate> = listOf(
    EditorTemplate(
        id = "intro_epic", name = "Epic Intro", category = TemplateCategory.INTRO,
        aspectRatio = AspectRatio.LANDSCAPE,
        textLayers = listOf(
            TextLayer(text = "YOUR CHANNEL", startMs = 500, endMs = 4000, x = 0.5f, y = 0.4f, fontSize = 48f, animation = TextAnimation.SCALE_UP),
            TextLayer(text = "Welcome Back!", startMs = 1500, endMs = 4000, x = 0.5f, y = 0.6f, fontSize = 24f, animation = TextAnimation.FADE_IN)
        ),
        transitionType = TransitionType.ZOOM_IN, suggestedDurationSec = 5,
        description = "Bold channel intro with zoom"
    ),
    EditorTemplate(
        id = "outro_sub", name = "Subscribe Outro", category = TemplateCategory.OUTRO,
        aspectRatio = AspectRatio.LANDSCAPE,
        textLayers = listOf(
            TextLayer(text = "Thanks for Watching!", startMs = 0, endMs = 3000, x = 0.5f, y = 0.3f, fontSize = 36f, animation = TextAnimation.FADE_IN),
            TextLayer(text = "SUBSCRIBE", startMs = 1000, endMs = 5000, x = 0.5f, y = 0.5f, fontSize = 48f, color = 0xFFFF0000.toInt(), animation = TextAnimation.BOUNCE),
            TextLayer(text = "Like & Share", startMs = 2000, endMs = 5000, x = 0.5f, y = 0.7f, fontSize = 20f, animation = TextAnimation.SLIDE_UP)
        ),
        transitionType = TransitionType.CROSSFADE, suggestedDurationSec = 6,
        description = "CTA outro with subscribe prompt"
    ),
    EditorTemplate(
        id = "social_tiktok", name = "TikTok/Reels", category = TemplateCategory.SOCIAL,
        aspectRatio = AspectRatio.PORTRAIT,
        textLayers = listOf(
            TextLayer(text = "WAIT FOR IT...", startMs = 0, endMs = 2000, x = 0.5f, y = 0.2f, fontSize = 32f, animation = TextAnimation.TYPEWRITER),
            TextLayer(text = "@yourhandle", startMs = 0, endMs = 15000, x = 0.5f, y = 0.9f, fontSize = 16f, animation = TextAnimation.NONE)
        ),
        transitionType = TransitionType.SLIDE_UP, suggestedDurationSec = 15,
        description = "Vertical format for TikTok/Reels"
    ),
    EditorTemplate(
        id = "social_shorts", name = "YouTube Shorts", category = TemplateCategory.SOCIAL,
        aspectRatio = AspectRatio.PORTRAIT,
        textLayers = listOf(
            TextLayer(text = "Did you know?", startMs = 0, endMs = 2000, x = 0.5f, y = 0.15f, fontSize = 28f, animation = TextAnimation.FADE_IN),
            TextLayer(text = "#Shorts", startMs = 0, endMs = 60000, x = 0.5f, y = 0.95f, fontSize = 14f, animation = TextAnimation.NONE)
        ),
        transitionType = TransitionType.CROSSFADE, suggestedDurationSec = 30,
        description = "Quick shorts with hook text"
    ),
    EditorTemplate(
        id = "tutorial_step", name = "Step-by-Step", category = TemplateCategory.TUTORIAL,
        aspectRatio = AspectRatio.LANDSCAPE,
        textLayers = listOf(
            TextLayer(text = "Step 1: Getting Started", startMs = 0, endMs = 5000, x = 0.5f, y = 0.1f, fontSize = 28f, animation = TextAnimation.SLIDE_LEFT),
            TextLayer(text = "Follow along below", startMs = 1000, endMs = 5000, x = 0.5f, y = 0.9f, fontSize = 16f, animation = TextAnimation.FADE_IN)
        ),
        transitionType = TransitionType.SLIDE_LEFT, suggestedDurationSec = 60,
        description = "Tutorial with numbered steps"
    ),
    EditorTemplate(
        id = "gaming_montage", name = "Gaming Montage", category = TemplateCategory.GAMING,
        aspectRatio = AspectRatio.LANDSCAPE,
        textLayers = listOf(
            TextLayer(text = "HIGHLIGHTS", startMs = 0, endMs = 3000, x = 0.5f, y = 0.5f, fontSize = 56f, color = 0xFFFF0000.toInt(), animation = TextAnimation.SCALE_UP),
            TextLayer(text = "GG", startMs = 0, endMs = 60000, x = 0.9f, y = 0.1f, fontSize = 20f, animation = TextAnimation.NONE)
        ),
        transitionType = TransitionType.ZOOM_IN, suggestedDurationSec = 30,
        description = "Fast-paced gaming highlights"
    ),
    EditorTemplate(
        id = "vlog_day", name = "Day in My Life", category = TemplateCategory.VLOG,
        aspectRatio = AspectRatio.LANDSCAPE,
        textLayers = listOf(
            TextLayer(text = "A Day in My Life", startMs = 0, endMs = 4000, x = 0.5f, y = 0.5f, fontSize = 36f, animation = TextAnimation.FADE_IN_OUT),
            TextLayer(text = "Morning", startMs = 5000, endMs = 8000, x = 0.1f, y = 0.1f, fontSize = 20f, animation = TextAnimation.SLIDE_RIGHT)
        ),
        transitionType = TransitionType.DISSOLVE, suggestedDurationSec = 120,
        description = "Aesthetic vlog with timestamps"
    ),
    EditorTemplate(
        id = "product_review", name = "Quick Review", category = TemplateCategory.PRODUCT,
        aspectRatio = AspectRatio.LANDSCAPE,
        textLayers = listOf(
            TextLayer(text = "PRODUCT REVIEW", startMs = 0, endMs = 3000, x = 0.5f, y = 0.15f, fontSize = 32f, animation = TextAnimation.SLIDE_UP),
            TextLayer(text = "Rating: /10", startMs = 0, endMs = 60000, x = 0.9f, y = 0.9f, fontSize = 18f, animation = TextAnimation.NONE)
        ),
        transitionType = TransitionType.WIPE, suggestedDurationSec = 90,
        description = "Product showcase with rating"
    ),
    EditorTemplate(
        id = "news_breaking", name = "Breaking News", category = TemplateCategory.NEWS,
        aspectRatio = AspectRatio.LANDSCAPE,
        textLayers = listOf(
            TextLayer(text = "BREAKING NEWS", startMs = 0, endMs = 3000, x = 0.5f, y = 0.1f, fontSize = 40f, color = 0xFFFF0000.toInt(), animation = TextAnimation.SCALE_UP),
            TextLayer(text = "Headline goes here", startMs = 1000, endMs = 30000, x = 0.5f, y = 0.85f, fontSize = 20f, animation = TextAnimation.SLIDE_LEFT)
        ),
        transitionType = TransitionType.CROSSFADE, suggestedDurationSec = 30,
        description = "News-style lower third"
    ),
    EditorTemplate(
        id = "event_invite", name = "Event Invite", category = TemplateCategory.EVENT,
        aspectRatio = AspectRatio.SQUARE,
        textLayers = listOf(
            TextLayer(text = "YOU'RE INVITED", startMs = 0, endMs = 3000, x = 0.5f, y = 0.3f, fontSize = 36f, animation = TextAnimation.BOUNCE),
            TextLayer(text = "Date & Time", startMs = 1500, endMs = 5000, x = 0.5f, y = 0.5f, fontSize = 24f, animation = TextAnimation.FADE_IN),
            TextLayer(text = "RSVP Now!", startMs = 2500, endMs = 5000, x = 0.5f, y = 0.7f, fontSize = 20f, color = 0xFFFFD700.toInt(), animation = TextAnimation.SLIDE_UP)
        ),
        transitionType = TransitionType.ZOOM_OUT, suggestedDurationSec = 10,
        description = "Square invite for social sharing"
    ),
    EditorTemplate(
        id = "promo_sale", name = "Sale Promo", category = TemplateCategory.PROMO,
        aspectRatio = AspectRatio.PORTRAIT,
        textLayers = listOf(
            TextLayer(text = "SALE", startMs = 0, endMs = 2000, x = 0.5f, y = 0.3f, fontSize = 64f, color = 0xFFFF0000.toInt(), animation = TextAnimation.SCALE_UP),
            TextLayer(text = "Up to 50% OFF", startMs = 1000, endMs = 5000, x = 0.5f, y = 0.5f, fontSize = 28f, animation = TextAnimation.BOUNCE),
            TextLayer(text = "Shop Now", startMs = 2000, endMs = 5000, x = 0.5f, y = 0.7f, fontSize = 22f, animation = TextAnimation.FADE_IN)
        ),
        transitionType = TransitionType.ZOOM_IN, suggestedDurationSec = 10,
        description = "Eye-catching sale announcement"
    ),
    EditorTemplate(
        id = "promo_launch", name = "Product Launch", category = TemplateCategory.PROMO,
        aspectRatio = AspectRatio.LANDSCAPE,
        textLayers = listOf(
            TextLayer(text = "INTRODUCING", startMs = 0, endMs = 2500, x = 0.5f, y = 0.35f, fontSize = 24f, animation = TextAnimation.FADE_IN),
            TextLayer(text = "Product Name", startMs = 1000, endMs = 5000, x = 0.5f, y = 0.5f, fontSize = 44f, animation = TextAnimation.SCALE_UP),
            TextLayer(text = "Available Now", startMs = 3000, endMs = 5000, x = 0.5f, y = 0.7f, fontSize = 20f, animation = TextAnimation.SLIDE_UP)
        ),
        transitionType = TransitionType.DISSOLVE, suggestedDurationSec = 15,
        description = "Sleek product launch reveal"
    )
)
