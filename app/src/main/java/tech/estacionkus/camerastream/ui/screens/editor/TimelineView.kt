package tech.estacionkus.camerastream.ui.screens.editor

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TimelineView(
    project: EditorProject,
    thumbnails: Map<String, List<Bitmap>>,
    playbackPositionMs: Long,
    zoomLevel: Float,
    selectedClipIndex: Int,
    onClipSelected: (Int) -> Unit,
    onPositionChanged: (Long) -> Unit,
    onAddClip: () -> Unit
) {
    val scrollState = rememberScrollState()
    val msPerDp = (10f / zoomLevel).coerceAtLeast(1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Time ruler
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .horizontalScroll(scrollState)
        ) {
            val totalMs = project.totalDurationMs.coerceAtLeast(10000L)
            val stepMs = when {
                zoomLevel >= 3f -> 1000L
                zoomLevel >= 1.5f -> 2000L
                else -> 5000L
            }
            for (t in 0..totalMs step stepMs) {
                val sec = t / 1000
                val widthDp = (stepMs / msPerDp).dp
                Box(modifier = Modifier.width(widthDp)) {
                    Text(
                        "${sec / 60}:${"%02d".format(sec % 60)}",
                        color = Color.White.copy(0.4f),
                        fontSize = 9.sp
                    )
                }
            }
        }

        // Video track
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            project.clips.forEachIndexed { index, clip ->
                val isSelected = index == selectedClipIndex
                val widthDp = (clip.trimmedDurationMs / msPerDp).dp.coerceAtLeast(60.dp)
                val clipThumbs = thumbnails[clip.id] ?: emptyList()

                // Transition indicator
                if (index > 0) {
                    val transition = project.transitions[index - 1]
                    if (transition != null && transition.type != TransitionType.NONE) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(48.dp)
                                .background(Color(0xFF6A1B9A).copy(0.5f), RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(transition.type.label.take(2), color = Color.White, fontSize = 8.sp)
                        }
                    }
                }

                // Clip
                Box(
                    modifier = Modifier
                        .width(widthDp)
                        .height(48.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isSelected) Color(0xFF1565C0) else Color(0xFF37474F)
                        )
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) Color(0xFF64B5F6) else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { onClipSelected(index) }
                ) {
                    // Thumbnail strip
                    Row(modifier = Modifier.fillMaxSize()) {
                        clipThumbs.forEach { thumb ->
                            Image(
                                bitmap = thumb.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .width(40.dp)
                                    .fillMaxHeight(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        if (clipThumbs.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Clip ${index + 1}",
                                    color = Color.White,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // Speed indicator
                    if (clip.speed != 1f) {
                        Surface(
                            color = Color(0xFFFF9800).copy(0.8f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(2.dp)
                        ) {
                            Text(
                                "${clip.speed}x",
                                color = Color.White,
                                fontSize = 8.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    // Filter indicator
                    if (clip.filterName != null) {
                        Surface(
                            color = Color(0xFF9C27B0).copy(0.8f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(2.dp)
                        ) {
                            Text(
                                clip.filterName.take(6),
                                color = Color.White,
                                fontSize = 7.sp,
                                modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            // Add clip button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF333333))
                    .clickable(onClick = onAddClip),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, "Add clip", tint = Color.White)
            }
        }

        Spacer(Modifier.height(4.dp))

        // Text layers track
        if (project.textLayers.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(Icons.Default.TextFields, null, tint = Color.White.copy(0.5f), modifier = Modifier.size(16.dp))
                project.textLayers.forEach { layer ->
                    val startDp = (layer.startMs / msPerDp).dp
                    val widthDp = ((layer.endMs - layer.startMs) / msPerDp).dp.coerceAtLeast(40.dp)
                    Spacer(Modifier.width(startDp))
                    Box(
                        modifier = Modifier
                            .width(widthDp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFFB300).copy(0.7f))
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            layer.text,
                            color = Color.White,
                            fontSize = 8.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Audio layers track
        if (project.audioLayers.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(Icons.Default.MusicNote, null, tint = Color.White.copy(0.5f), modifier = Modifier.size(16.dp))
                project.audioLayers.forEach { layer ->
                    val widthDp = ((layer.endMs - layer.startMs) / msPerDp).dp.coerceAtLeast(40.dp)
                    Box(
                        modifier = Modifier
                            .width(widthDp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (layer.isVoiceover) Color(0xFF4CAF50).copy(0.7f)
                                else Color(0xFF2196F3).copy(0.7f)
                            )
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            layer.name.ifBlank { if (layer.isVoiceover) "Voiceover" else "Music" },
                            color = Color.White,
                            fontSize = 8.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Duration display
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "${project.clips.size} clips",
                color = Color.White.copy(0.5f),
                fontSize = 10.sp
            )
            Text(
                formatMs(project.totalDurationMs),
                color = Color.White.copy(0.5f),
                fontSize = 10.sp
            )
        }
    }
}

private fun formatMs(ms: Long): String {
    val sec = ms / 1000
    val min = sec / 60
    val s = sec % 60
    return "%d:%02d".format(min, s)
}
