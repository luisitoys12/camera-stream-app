package tech.estacionkus.camerastream.ui.screens.stream

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import tech.estacionkus.camerastream.data.media.MediaAsset
import tech.estacionkus.camerastream.data.media.MediaAssetType
import tech.estacionkus.camerastream.data.media.OverlayCategory
import tech.estacionkus.camerastream.data.overlay.ActiveOverlay
import tech.estacionkus.camerastream.data.overlay.OverlayPosition
import tech.estacionkus.camerastream.ui.theme.*
import java.util.UUID

/**
 * Panel deslizable desde abajo que muestra la biblioteca de medios
 * y permite agregar overlays directamente al stream activo.
 */
@Composable
fun OverlayPanel(
    visible: Boolean,
    mediaAssets: List<MediaAsset>,
    activeOverlays: List<ActiveOverlay>,
    onAddOverlay: (ActiveOverlay) -> Unit,
    onRemoveOverlay: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(onClick = onDismiss)
            )

            // Panel
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.52f),
                color = Surface800,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Handle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Surface600)
                        )
                    }

                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Overlays",
                            style = MaterialTheme.typography.titleMedium,
                            color = OnSurface,
                            fontWeight = FontWeight.Bold
                        )
                        if (activeOverlays.isNotEmpty()) {
                            TextButton(onClick = { activeOverlays.forEach { onRemoveOverlay(it.id) } }) {
                                Text("Limpiar todo", color = CameraRed, fontSize = 12.sp)
                            }
                        }
                    }

                    // Active overlays row
                    if (activeOverlays.isNotEmpty()) {
                        Text(
                            "Activos (${activeOverlays.size})",
                            modifier = Modifier.padding(horizontal = 20.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceMuted
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(activeOverlays, key = { it.id }) { overlay ->
                                ActiveOverlayChip(
                                    overlay = overlay,
                                    onRemove = { onRemoveOverlay(overlay.id) }
                                )
                            }
                        }
                        HorizontalDivider(color = Surface600, modifier = Modifier.padding(horizontal = 16.dp))
                    }

                    // Category tabs
                    var selectedCat by remember { mutableStateOf(OverlayCategory.INTRO) }
                    val categories = OverlayCategory.values().toList()

                    ScrollableTabRow(
                        selectedTabIndex = categories.indexOf(selectedCat),
                        containerColor = Surface800,
                        contentColor = CameraRed,
                        edgePadding = 12.dp
                    ) {
                        categories.forEach { cat ->
                            Tab(
                                selected = selectedCat == cat,
                                onClick = { selectedCat = cat },
                                text = {
                                    Text(
                                        cat.label(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            )
                        }
                    }

                    val filtered = mediaAssets.filter { it.category == selectedCat }

                    if (filtered.isEmpty()) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.PermMedia,
                                    contentDescription = null,
                                    tint = OnSurfaceMuted,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    "Sin medios en ${selectedCat.label()}",
                                    color = OnSurfaceMuted,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(filtered, key = { it.id }) { asset ->
                                val isActive = activeOverlays.any { it.id == asset.id }
                                MediaOverlayTile(
                                    asset = asset,
                                    isActive = isActive,
                                    onClick = {
                                        if (isActive) {
                                            onRemoveOverlay(asset.id)
                                        } else {
                                            onAddOverlay(
                                                ActiveOverlay(
                                                    id = asset.id,
                                                    uri = asset.uri,
                                                    name = asset.name,
                                                    category = asset.category,
                                                    isVideo = asset.type == MediaAssetType.VIDEO,
                                                    position = defaultPositionFor(asset.category),
                                                    scalePercent = defaultScaleFor(asset.category)
                                                )
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaOverlayTile(
    asset: MediaAsset,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(100.dp)
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(8.dp))
            .background(Surface700)
            .border(
                width = if (isActive) 2.dp else 0.dp,
                color = if (isActive) CameraRed else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = asset.uri,
            contentDescription = asset.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        if (isActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CameraRed.copy(alpha = 0.25f))
            )
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(16.dp)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.5f))
        ) {
            Text(
                asset.name,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun ActiveOverlayChip(overlay: ActiveOverlay, onRemove: () -> Unit) {
    Surface(
        color = CameraRed.copy(alpha = 0.15f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CameraRed.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                if (overlay.isVideo) Icons.Default.PlayCircle else Icons.Default.Image,
                contentDescription = null,
                tint = CameraRed,
                modifier = Modifier.size(14.dp)
            )
            Text(
                overlay.name.take(12),
                style = MaterialTheme.typography.labelSmall,
                color = OnSurface
            )
            IconButton(onClick = onRemove, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Default.Close, contentDescription = null, tint = OnSurfaceMuted, modifier = Modifier.size(12.dp))
            }
        }
    }
}

private fun OverlayCategory.label() = when (this) {
    OverlayCategory.INTRO -> "Intro"
    OverlayCategory.OUTRO -> "Outro"
    OverlayCategory.LOWER_THIRD -> "Lower Third"
    OverlayCategory.LOGO -> "Logo"
    OverlayCategory.BACKGROUND -> "Fondo"
    OverlayCategory.OTHER -> "Otros"
}

private fun defaultPositionFor(cat: OverlayCategory) = when (cat) {
    OverlayCategory.LOWER_THIRD -> OverlayPosition.BOTTOM_LEFT
    OverlayCategory.LOGO -> OverlayPosition.TOP_RIGHT
    OverlayCategory.INTRO, OverlayCategory.OUTRO -> OverlayPosition.CENTER
    OverlayCategory.BACKGROUND -> OverlayPosition.CENTER
    else -> OverlayPosition.BOTTOM_LEFT
}

private fun defaultScaleFor(cat: OverlayCategory) = when (cat) {
    OverlayCategory.LOWER_THIRD -> 50f
    OverlayCategory.LOGO -> 18f
    OverlayCategory.INTRO, OverlayCategory.OUTRO -> 100f
    OverlayCategory.BACKGROUND -> 100f
    else -> 30f
}
