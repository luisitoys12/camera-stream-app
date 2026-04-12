package tech.estacionkus.camerastream.ui.screens.pro

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.estacionkus.camerastream.domain.model.ColorFilter
import tech.estacionkus.camerastream.domain.model.FocusMode
import tech.estacionkus.camerastream.domain.model.GridOverlay
import tech.estacionkus.camerastream.domain.model.WhiteBalance
import tech.estacionkus.camerastream.ui.screens.stream.CameraPreview
import tech.estacionkus.camerastream.ui.theme.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.DrawScope

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualCameraScreen(
    onBack: () -> Unit,
    viewModel: ManualCameraViewModel = hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsState()
    val settings = ui.settings

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Camera Pro",
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = OnSurface)
                    }
                },
                actions = {
                    // Torch toggle
                    IconButton(onClick = { viewModel.toggleTorch() }) {
                        Icon(
                            if (settings.torchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Torch",
                            tint = if (settings.torchEnabled) Color(0xFFFFD700) else OnSurfaceMuted
                        )
                    }
                    // Flip camera
                    IconButton(onClick = { viewModel.flipCamera() }) {
                        Icon(Icons.Default.Cameraswitch, contentDescription = "Flip camera", tint = OnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D0D0D)
                )
            )
        },
        containerColor = Color(0xFF0D0D0D)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Camera Preview ──────────────────────────────────────────────
            CameraPreviewBox(
                isFront = ui.isFront,
                gridOverlay = settings.gridOverlay,
                colorFilter = settings.colorFilter,
                iso = settings.iso,
                exposureCompensation = settings.exposureCompensation,
                zoomRatio = settings.zoomRatio,
                isBeautyOn = settings.beautyFilterEnabled,
                isTorchOn = settings.torchEnabled
            )

            // ── Controls ────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ISO
                ControlCard(
                    title = "ISO",
                    badgeText = if (settings.iso == -1) "AUTO" else "${settings.iso}",
                    badgeColor = if (settings.iso == -1) Color(0xFF4CAF50) else CameraRed
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(-1, 100, 200, 400, 800, 1600).forEach { v ->
                            val selected = settings.iso == v
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setIso(v) },
                                color = if (selected) CameraRed else Color(0xFF2A2A2A),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (v == -1) "Auto" else "$v",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    fontSize = 12.sp,
                                    color = if (selected) Color.White else OnSurfaceMuted,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Exposure compensation
                ControlCard(
                    title = "Exposure",
                    badgeText = "${if (settings.exposureCompensation >= 0) "+" else ""}${settings.exposureCompensation} EV"
                ) {
                    Slider(
                        value = settings.exposureCompensation.toFloat(),
                        onValueChange = { viewModel.setExposure(it.toInt()) },
                        valueRange = -4f..4f,
                        steps = 7,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = CameraRed,
                            activeTrackColor = CameraRed,
                            inactiveTrackColor = Color(0xFF333333)
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("-4 EV", fontSize = 10.sp, color = OnSurfaceMuted)
                        Text("0 EV", fontSize = 10.sp, color = OnSurfaceMuted)
                        Text("+4 EV", fontSize = 10.sp, color = OnSurfaceMuted)
                    }
                }

                // White Balance
                ControlCard(title = "White Balance", badgeText = settings.whiteBalance.label) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WhiteBalance.entries.forEach { wb ->
                            val selected = settings.whiteBalance == wb
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setWhiteBalance(wb) },
                                color = if (selected) CameraRed else Color(0xFF2A2A2A),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        wbIcon(wb),
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (selected) Color.White else OnSurfaceMuted
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        wb.label,
                                        fontSize = 10.sp,
                                        color = if (selected) Color.White else OnSurfaceMuted,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                // Focus
                ControlCard(title = "Focus", badgeText = settings.focusMode.name) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FocusMode.entries.forEach { mode ->
                            val selected = settings.focusMode == mode
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setFocusMode(mode) },
                                color = if (selected) CameraRed else Color(0xFF2A2A2A),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    mode.name,
                                    modifier = Modifier
                                        .padding(vertical = 9.dp)
                                        .fillMaxWidth(),
                                    fontSize = 12.sp,
                                    color = if (selected) Color.White else OnSurfaceMuted,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    AnimatedVisibility(
                        visible = settings.focusMode == FocusMode.MANUAL,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Focus Distance", fontSize = 12.sp, color = OnSurfaceMuted)
                                Text(
                                    "Near ← → Far",
                                    fontSize = 10.sp,
                                    color = OnSurfaceMuted
                                )
                            }
                            Slider(
                                value = settings.manualFocusDistance,
                                onValueChange = { viewModel.setManualFocus(it) },
                                valueRange = 0f..1f,
                                modifier = Modifier.fillMaxWidth(),
                                colors = SliderDefaults.colors(
                                    thumbColor = CameraRed,
                                    activeTrackColor = CameraRed,
                                    inactiveTrackColor = Color(0xFF333333)
                                )
                            )
                        }
                    }
                }

                // Zoom
                ControlCard(
                    title = "Zoom",
                    badgeText = "${"%.1f".format(settings.zoomRatio)}x"
                ) {
                    Slider(
                        value = settings.zoomRatio,
                        onValueChange = { viewModel.setZoom(it) },
                        valueRange = 1f..10f,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = CameraRed,
                            activeTrackColor = CameraRed,
                            inactiveTrackColor = Color(0xFF333333)
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("1x", "2x", "5x", "10x").forEach { label ->
                            val zoomValue = label.dropLast(1).toFloat()
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { viewModel.setZoom(zoomValue) },
                                color = if (settings.zoomRatio == zoomValue) CameraRed.copy(alpha = 0.2f) else Color.Transparent,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    label,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    color = if (settings.zoomRatio == zoomValue) CameraRed else OnSurfaceMuted,
                                    fontWeight = if (settings.zoomRatio == zoomValue) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Beauty Filter
                ControlCard(title = "Beauty Filter") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.AutoFixHigh,
                                contentDescription = null,
                                tint = if (settings.beautyFilterEnabled) Color(0xFFE91E63) else OnSurfaceMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                "Enabled",
                                fontSize = 14.sp,
                                color = OnSurface
                            )
                        }
                        Switch(
                            checked = settings.beautyFilterEnabled,
                            onCheckedChange = { viewModel.toggleBeautyFilter() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFE91E63)
                            )
                        )
                    }
                    AnimatedVisibility(
                        visible = settings.beautyFilterEnabled,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(color = Color(0xFF2A2A2A))
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Smoothing", fontSize = 13.sp, color = OnSurfaceMuted)
                                Text("${(settings.beautySmoothing * 100).toInt()}%", fontSize = 12.sp, color = OnSurface)
                            }
                            Slider(
                                value = settings.beautySmoothing,
                                onValueChange = { viewModel.setBeautySmoothing(it) },
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFFE91E63),
                                    activeTrackColor = Color(0xFFE91E63),
                                    inactiveTrackColor = Color(0xFF333333)
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Brightness", fontSize = 13.sp, color = OnSurfaceMuted)
                                Text("${(settings.beautyBrightness * 100).toInt()}%", fontSize = 12.sp, color = OnSurface)
                            }
                            Slider(
                                value = settings.beautyBrightness,
                                onValueChange = { viewModel.setBeautyBrightness(it) },
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFFE91E63),
                                    activeTrackColor = Color(0xFFE91E63),
                                    inactiveTrackColor = Color(0xFF333333)
                                )
                            )
                        }
                    }
                }

                // Color Filters
                ControlCard(title = "Color Filter", badgeText = settings.colorFilter.label) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ColorFilter.entries.forEach { filter ->
                            val selected = settings.colorFilter == filter
                            ColorFilterChip(
                                filter = filter,
                                selected = selected,
                                onClick = { viewModel.setColorFilter(filter) }
                            )
                        }
                    }
                }

                // Grid Overlay
                ControlCard(title = "Grid Overlay", badgeText = settings.gridOverlay.label) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GridOverlay.entries.forEach { grid ->
                            val selected = settings.gridOverlay == grid
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setGridOverlay(grid) },
                                color = if (selected) CameraRed else Color(0xFF2A2A2A),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        gridIcon(grid),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (selected) Color.White else OnSurfaceMuted
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        grid.label,
                                        fontSize = 9.sp,
                                        color = if (selected) Color.White else OnSurfaceMuted,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // Toggle row: Stabilization + Night mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ToggleCard(
                        label = "Stabilization",
                        icon = Icons.Default.Vibration,
                        checked = settings.stabilizationEnabled,
                        onToggle = { viewModel.toggleStabilization() },
                        activeColor = Color(0xFF2196F3),
                        modifier = Modifier.weight(1f)
                    )
                    ToggleCard(
                        label = "Night Mode",
                        icon = Icons.Default.NightsStay,
                        checked = settings.nightModeEnabled,
                        onToggle = { viewModel.toggleNightMode() },
                        activeColor = Color(0xFF9C27B0),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Camera Preview Box
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CameraPreviewBox(
    isFront: Boolean,
    gridOverlay: GridOverlay,
    colorFilter: ColorFilter,
    iso: Int,
    exposureCompensation: Int,
    zoomRatio: Float,
    isBeautyOn: Boolean,
    isTorchOn: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            isFrontCamera = isFront,
            onCameraReady = {}
        )

        // Color filter overlay tint
        if (colorFilter != ColorFilter.NONE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorFilterTint(colorFilter))
            )
        }

        // Grid overlay
        if (gridOverlay != GridOverlay.NONE) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawGridOverlay(gridOverlay)
            }
        }

        // HUD badge strip at top
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            HudBadge(text = if (iso == -1) "ISO AUTO" else "ISO $iso")
            HudBadge(text = "${if (exposureCompensation >= 0) "+" else ""}${exposureCompensation} EV")
            HudBadge(text = "${"%.1f".format(zoomRatio)}x")
        }

        // Active filter indicator (bottom right)
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (isBeautyOn) {
                HudBadge(text = "BEAUTY", color = Color(0xFFE91E63))
            }
            if (isTorchOn) {
                HudBadge(text = "TORCH", color = Color(0xFFFFD700))
            }
            if (colorFilter != ColorFilter.NONE) {
                HudBadge(text = colorFilter.label.uppercase(), color = Color(0xFF9C27B0))
            }
        }

        // Torch warning overlay
        if (isTorchOn) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFD700))
            )
        }
    }
}

@Composable
private fun HudBadge(
    text: String,
    color: Color = Color(0x99000000)
) {
    Surface(
        color = color,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            fontSize = 9.sp,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Control Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ControlCard(
    title: String,
    badgeText: String? = null,
    badgeColor: Color = CameraRed,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF2A2A2A))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceMuted,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
                if (badgeText != null) {
                    Surface(
                        color = badgeColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            badgeText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            color = badgeColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            content()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Toggle Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ToggleCard(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onToggle: () -> Unit,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onToggle),
        color = if (checked) activeColor.copy(alpha = 0.15f) else Color(0xFF1A1A1A),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (checked) activeColor.copy(alpha = 0.4f) else Color(0xFF2A2A2A))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (checked) activeColor else OnSurfaceMuted
            )
            Text(
                label,
                fontSize = 13.sp,
                color = if (checked) OnSurface else OnSurfaceMuted,
                fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = checked,
                onCheckedChange = { onToggle() },
                modifier = Modifier.height(24.dp),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = activeColor
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Color Filter Chip
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ColorFilterChip(
    filter: ColorFilter,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = colorFilterPreview(filter)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(bgColor, RoundedCornerShape(8.dp))
                .then(
                    if (selected) Modifier.border(2.dp, CameraRed, RoundedCornerShape(8.dp))
                    else Modifier.border(1.dp, Color(0xFF333333), RoundedCornerShape(8.dp))
                ),
            contentAlignment = Alignment.Center
        ) {
            if (filter == ColorFilter.NONE) {
                Icon(Icons.Default.Block, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            filter.label,
            fontSize = 10.sp,
            color = if (selected) CameraRed else OnSurfaceMuted,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Grid Canvas
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawGridOverlay(type: GridOverlay) {
    val w = size.width
    val h = size.height
    val color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.3f)
    val stroke = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))

    when (type) {
        GridOverlay.THIRDS -> {
            drawLine(color, Offset(w / 3, 0f), Offset(w / 3, h), strokeWidth = 1f)
            drawLine(color, Offset(2 * w / 3, 0f), Offset(2 * w / 3, h), strokeWidth = 1f)
            drawLine(color, Offset(0f, h / 3), Offset(w, h / 3), strokeWidth = 1f)
            drawLine(color, Offset(0f, 2 * h / 3), Offset(w, 2 * h / 3), strokeWidth = 1f)
        }
        GridOverlay.GRID_4X4 -> {
            for (i in 1..3) {
                drawLine(color, Offset(w * i / 4, 0f), Offset(w * i / 4, h), strokeWidth = 1f)
                drawLine(color, Offset(0f, h * i / 4), Offset(w, h * i / 4), strokeWidth = 1f)
            }
        }
        GridOverlay.CENTER -> {
            val cx = w / 2; val cy = h / 2; val r = 30f
            drawLine(color, Offset(cx, cy - r), Offset(cx, cy + r), strokeWidth = 2f)
            drawLine(color, Offset(cx - r, cy), Offset(cx + r, cy), strokeWidth = 2f)
        }
        GridOverlay.NONE -> {}
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun colorFilterTint(filter: ColorFilter): Color = when (filter) {
    ColorFilter.WARM -> Color(0x33FF8C00)
    ColorFilter.COOL -> Color(0x330080FF)
    ColorFilter.VIVID -> Color(0x22FF0088)
    ColorFilter.BW -> Color(0x66808080)
    ColorFilter.VINTAGE -> Color(0x33AA6622)
    ColorFilter.NONE -> Color.Transparent
}

private fun colorFilterPreview(filter: ColorFilter): Color = when (filter) {
    ColorFilter.NONE -> Color(0xFF2A2A2A)
    ColorFilter.WARM -> Color(0xFF8B4513)
    ColorFilter.COOL -> Color(0xFF1565C0)
    ColorFilter.VIVID -> Color(0xFF880E4F)
    ColorFilter.BW -> Color(0xFF616161)
    ColorFilter.VINTAGE -> Color(0xFF6D4C41)
}

private fun wbIcon(wb: WhiteBalance) = when (wb) {
    WhiteBalance.AUTO -> Icons.Default.WbAuto
    WhiteBalance.DAYLIGHT -> Icons.Default.WbSunny
    WhiteBalance.CLOUDY -> Icons.Default.WbCloudy
    WhiteBalance.FLUORESCENT -> Icons.Default.WbSunny
    WhiteBalance.INCANDESCENT -> Icons.Default.WbIncandescent
    WhiteBalance.TUNGSTEN -> Icons.Default.Lightbulb
}

private fun gridIcon(grid: GridOverlay) = when (grid) {
    GridOverlay.NONE -> Icons.Default.GridOff
    GridOverlay.THIRDS -> Icons.Default.GridOn
    GridOverlay.GRID_4X4 -> Icons.Default.Grid4x4
    GridOverlay.CENTER -> Icons.Default.CropFree
}
