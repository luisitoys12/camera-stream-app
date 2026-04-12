package tech.estacionkus.camerastream.ui.screens.studio

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tech.estacionkus.camerastream.data.overlay.OverlayRepository
import tech.estacionkus.camerastream.domain.FeatureGate
import tech.estacionkus.camerastream.domain.SceneManager
import tech.estacionkus.camerastream.domain.model.*
import tech.estacionkus.camerastream.ui.theme.*
import java.util.UUID
import javax.inject.Inject

// ---------------------------------------------------------------------------
// UI State
// ---------------------------------------------------------------------------

data class OverlayPreset(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val overlays: List<OverlayItem>
)

data class MyStudioUiState(
    val scenes: List<Scene> = emptyList(),
    val activeSceneId: String = "",
    val overlays: List<OverlayItem> = emptyList(),
    val selectedOverlayId: String? = null,
    val presets: List<OverlayPreset> = emptyList(),
    val showAddOverlaySheet: Boolean = false,
    val showEditOverlaySheet: Boolean = false,
    val showPresetDialog: Boolean = false,
    val showPreviewPanel: Boolean = false,
    val canUseScenes: Boolean = false
)

// ---------------------------------------------------------------------------
// ViewModel
// ---------------------------------------------------------------------------

@HiltViewModel
class MyStudioViewModel @Inject constructor(
    private val sceneManager: SceneManager,
    private val overlayRepository: OverlayRepository,
    private val featureGate: FeatureGate
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyStudioUiState())
    val uiState: StateFlow<MyStudioUiState> = _uiState.asStateFlow()

    private val _presets = MutableStateFlow<List<OverlayPreset>>(emptyList())

    init {
        viewModelScope.launch {
            combine(
                sceneManager.scenes,
                sceneManager.activeSceneId,
                _presets
            ) { scenes, activeId, presets ->
                val activeScene = scenes.find { it.id == activeId } ?: scenes.firstOrNull()
                _uiState.update {
                    it.copy(
                        scenes = scenes,
                        activeSceneId = activeId,
                        overlays = activeScene?.overlays ?: emptyList(),
                        presets = presets,
                        canUseScenes = featureGate.canScenes()
                    )
                }
            }.collect()
        }
    }

    fun selectScene(id: String) {
        sceneManager.switchTo(id)
        _uiState.update { it.copy(selectedOverlayId = null) }
    }

    fun addOverlay(type: OverlayType) {
        val sceneId = _uiState.value.activeSceneId
        val overlay = OverlayItem(
            id = UUID.randomUUID().toString(),
            type = type,
            text = defaultTextForType(type),
            zOrder = _uiState.value.overlays.size
        )
        sceneManager.addOverlay(sceneId, overlay)
        _uiState.update { it.copy(showAddOverlaySheet = false) }
    }

    fun deleteOverlay(overlayId: String) {
        sceneManager.removeOverlay(_uiState.value.activeSceneId, overlayId)
        if (_uiState.value.selectedOverlayId == overlayId) {
            _uiState.update { it.copy(selectedOverlayId = null) }
        }
    }

    fun selectOverlay(overlayId: String?) {
        _uiState.update { it.copy(selectedOverlayId = overlayId, showEditOverlaySheet = overlayId != null) }
    }

    fun updateOverlay(overlay: OverlayItem) {
        sceneManager.updateOverlay(_uiState.value.activeSceneId, overlay)
    }

    fun toggleOverlayVisibility(overlayId: String) {
        val overlay = _uiState.value.overlays.find { it.id == overlayId } ?: return
        sceneManager.updateOverlay(_uiState.value.activeSceneId, overlay.copy(isVisible = !overlay.isVisible))
    }

    fun moveOverlay(fromIndex: Int, toIndex: Int) {
        val sceneId = _uiState.value.activeSceneId
        val list = _uiState.value.overlays.toMutableList()
        if (fromIndex in list.indices && toIndex in list.indices) {
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            list.forEachIndexed { index, overlay ->
                sceneManager.updateOverlay(sceneId, overlay.copy(zOrder = index))
            }
        }
    }

    fun savePreset(name: String) {
        val preset = OverlayPreset(name = name, overlays = _uiState.value.overlays)
        _presets.value = _presets.value + preset
        _uiState.update { it.copy(showPresetDialog = false) }
    }

    fun loadPreset(preset: OverlayPreset) {
        val sceneId = _uiState.value.activeSceneId
        _uiState.value.overlays.forEach { sceneManager.removeOverlay(sceneId, it.id) }
        preset.overlays.forEach { overlay ->
            sceneManager.addOverlay(sceneId, overlay.copy(id = UUID.randomUUID().toString()))
        }
    }

    fun deletePreset(presetId: String) {
        _presets.value = _presets.value.filter { it.id != presetId }
    }

    fun toggleAddOverlaySheet(show: Boolean) = _uiState.update { it.copy(showAddOverlaySheet = show) }
    fun toggleEditOverlaySheet(show: Boolean) = _uiState.update { it.copy(showEditOverlaySheet = show) }
    fun togglePresetDialog(show: Boolean) = _uiState.update { it.copy(showPresetDialog = show) }
    fun togglePreviewPanel() = _uiState.update { it.copy(showPreviewPanel = !it.showPreviewPanel) }

    private fun defaultTextForType(type: OverlayType): String = when (type) {
        OverlayType.TEXT -> "Sample Text"
        OverlayType.TICKER -> "Breaking news ticker text here..."
        OverlayType.TIMER -> "00:00"
        OverlayType.QR_CODE -> "https://example.com"
        OverlayType.ALERT -> "New Alert!"
        OverlayType.LOWER_THIRD -> "Your Name | Title"
        OverlayType.SOCIAL_HANDLE -> "@yourhandle"
        OverlayType.CHAT_WIDGET -> ""
        OverlayType.SCOREBOARD -> ""
        else -> ""
    }
}

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyStudioScreen(
    onBack: () -> Unit,
    viewModel: MyStudioViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Studio",
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
                    IconButton(onClick = { viewModel.togglePreviewPanel() }) {
                        Icon(
                            Icons.Default.Preview,
                            contentDescription = "Preview",
                            tint = if (state.showPreviewPanel) CameraRed else OnSurface
                        )
                    }
                    IconButton(onClick = { viewModel.togglePresetDialog(true) }) {
                        Icon(Icons.Default.SaveAlt, contentDescription = "Presets", tint = OnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D0D0D),
                    titleContentColor = OnSurface,
                    navigationIconContentColor = OnSurface,
                    actionIconContentColor = OnSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.toggleAddOverlaySheet(true) },
                containerColor = CameraRed,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Overlay")
            }
        },
        containerColor = Color(0xFF0D0D0D)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Scene tabs
            SceneTabBar(
                scenes = state.scenes,
                activeSceneId = state.activeSceneId,
                onSelectScene = viewModel::selectScene
            )

            if (!state.canUseScenes) {
                ProBanner(message = "Upgrade to Pro for full scene management")
            }

            // Preview panel (collapsible)
            AnimatedVisibility(
                visible = state.showPreviewPanel,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                OverlayPreviewPanel(
                    overlays = state.overlays,
                    selectedOverlayId = state.selectedOverlayId,
                    onSelectOverlay = viewModel::selectOverlay
                )
            }

            // Overlay list
            if (state.overlays.isEmpty()) {
                EmptyOverlayPlaceholder(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(
                        items = state.overlays.sortedBy { it.zOrder },
                        key = { _, item -> item.id }
                    ) { index, overlay ->
                        OverlayListItem(
                            overlay = overlay,
                            isSelected = overlay.id == state.selectedOverlayId,
                            onSelect = { viewModel.selectOverlay(overlay.id) },
                            onDelete = { viewModel.deleteOverlay(overlay.id) },
                            onToggleVisibility = { viewModel.toggleOverlayVisibility(overlay.id) },
                            onMoveUp = {
                                if (index > 0) viewModel.moveOverlay(index, index - 1)
                            },
                            onMoveDown = {
                                if (index < state.overlays.size - 1) viewModel.moveOverlay(index, index + 1)
                            }
                        )
                    }
                }
            }
        }
    }

    // Add overlay bottom sheet
    if (state.showAddOverlaySheet) {
        AddOverlaySheet(
            onDismiss = { viewModel.toggleAddOverlaySheet(false) },
            onSelectType = viewModel::addOverlay
        )
    }

    // Edit overlay bottom sheet
    if (state.showEditOverlaySheet && state.selectedOverlayId != null) {
        val overlay = state.overlays.find { it.id == state.selectedOverlayId }
        if (overlay != null) {
            EditOverlaySheet(
                overlay = overlay,
                onDismiss = {
                    viewModel.toggleEditOverlaySheet(false)
                    viewModel.selectOverlay(null)
                },
                onUpdate = viewModel::updateOverlay
            )
        }
    }

    // Preset dialog
    if (state.showPresetDialog) {
        PresetDialog(
            presets = state.presets,
            onDismiss = { viewModel.togglePresetDialog(false) },
            onSave = viewModel::savePreset,
            onLoad = viewModel::loadPreset,
            onDelete = viewModel::deletePreset
        )
    }
}

// ---------------------------------------------------------------------------
// Overlay Preview Panel
// ---------------------------------------------------------------------------

@Composable
private fun OverlayPreviewPanel(
    overlays: List<OverlayItem>,
    selectedOverlayId: String?,
    onSelectOverlay: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = Color(0xFF080818),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF2A2A4A))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "LAYOUT PREVIEW",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceMuted,
                    letterSpacing = 2.sp,
                    fontSize = 9.sp
                )
                Text(
                    "${overlays.count { it.isVisible }} visible",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceMuted,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mock camera view with overlays
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF1A1A2E), Color(0xFF080818))
                        ),
                        RoundedCornerShape(8.dp)
                    )
                    .border(1.dp, Color(0xFF2A2A3A), RoundedCornerShape(8.dp))
                    .drawBehind { drawCameraGrid() }
            ) {
                // Camera icon placeholder
                Icon(
                    Icons.Default.Videocam,
                    contentDescription = null,
                    tint = Color(0xFF222244),
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.Center)
                )

                // Render visible overlays as colored rectangles
                overlays
                    .filter { it.isVisible }
                    .sortedBy { it.zOrder }
                    .forEach { overlay ->
                        val isSelected = overlay.id == selectedOverlayId
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    start = (overlay.x * 100).dp.coerceAtMost(80.dp),
                                    top = (overlay.y * 100).dp.coerceAtMost(60.dp)
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(overlay.width.coerceIn(0.1f, 1f))
                                    .height(24.dp)
                                    .background(
                                        overlayTypeColor(overlay.type).copy(alpha = if (isSelected) 0.6f else 0.35f),
                                        RoundedCornerShape(3.dp)
                                    )
                                    .then(
                                        if (isSelected) Modifier.border(1.dp, overlayTypeColor(overlay.type), RoundedCornerShape(3.dp))
                                        else Modifier
                                    )
                                    .clickable { onSelectOverlay(overlay.id) }
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = if (overlay.text.isNotBlank()) overlay.text.take(20) else overlay.type.name.replace("_", " "),
                                    fontSize = 7.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                // "LIVE" badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    color = CameraRed,
                    shape = RoundedCornerShape(3.dp)
                ) {
                    Text(
                        "LIVE",
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        fontSize = 8.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Tap an overlay in the preview to select it for editing",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceMuted,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun DrawScope.drawCameraGrid() {
    val color = Color(0xFF1A1A2E)
    val w = size.width
    val h = size.height
    // Rule of thirds
    drawLine(color, Offset(w / 3, 0f), Offset(w / 3, h), strokeWidth = 1f)
    drawLine(color, Offset(2 * w / 3, 0f), Offset(2 * w / 3, h), strokeWidth = 1f)
    drawLine(color, Offset(0f, h / 3), Offset(w, h / 3), strokeWidth = 1f)
    drawLine(color, Offset(0f, 2 * h / 3), Offset(w, 2 * h / 3), strokeWidth = 1f)
}

// ---------------------------------------------------------------------------
// Scene Tab Bar
// ---------------------------------------------------------------------------

@Composable
private fun SceneTabBar(
    scenes: List<Scene>,
    activeSceneId: String,
    onSelectScene: (String) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = scenes.indexOfFirst { it.id == activeSceneId }.coerceAtLeast(0),
        containerColor = Color(0xFF1A1A1A),
        contentColor = CameraRed,
        edgePadding = 12.dp,
        divider = { HorizontalDivider(color = Color(0xFF2A2A2A)) }
    ) {
        scenes.forEach { scene ->
            Tab(
                selected = scene.id == activeSceneId,
                onClick = { onSelectScene(scene.id) },
                text = {
                    Text(
                        text = scene.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (scene.id == activeSceneId) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selectedContentColor = CameraRed,
                unselectedContentColor = OnSurfaceMuted
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Overlay List Item
// ---------------------------------------------------------------------------

@Composable
private fun OverlayListItem(
    overlay: OverlayItem,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    onToggleVisibility: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val borderColor = if (isSelected) CameraRed else Color(0xFF2A2A2A)
    val typeColor = overlayTypeColor(overlay.type)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        color = if (isSelected) Color(0xFF1F1010) else Color(0xFF1A1A1A),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Type icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(typeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = overlayTypeIcon(overlay.type),
                    contentDescription = null,
                    tint = typeColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = overlay.type.name.replace("_", " "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (!overlay.isVisible) {
                        Surface(
                            color = Color(0xFF333333),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "Hidden",
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                fontSize = 9.sp,
                                color = OnSurfaceMuted
                            )
                        }
                    }
                }
                if (overlay.text.isNotBlank()) {
                    Text(
                        text = overlay.text,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "Pos: ${(overlay.x * 100).toInt()}%, ${(overlay.y * 100).toInt()}% | Size: ${(overlay.width * 100).toInt()}% | Opacity: ${(overlay.alpha * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceMuted,
                    fontSize = 9.sp
                )
            }

            // Actions column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Visibility toggle
                IconButton(onClick = onToggleVisibility, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (overlay.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle visibility",
                        tint = if (overlay.isVisible) Color(0xFF4CAF50) else OnSurfaceMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
                // Reorder
                IconButton(onClick = onMoveUp, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move up", tint = OnSurfaceMuted, modifier = Modifier.size(14.dp))
                }
                IconButton(onClick = onMoveDown, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move down", tint = OnSurfaceMuted, modifier = Modifier.size(14.dp))
                }
                // Delete
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CameraRed, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Add Overlay Sheet
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddOverlaySheet(
    onDismiss: () -> Unit,
    onSelectType: (OverlayType) -> Unit
) {
    val overlayOptions = listOf(
        Triple(OverlayType.TEXT, Icons.Default.TextFields, "Text"),
        Triple(OverlayType.IMAGE, Icons.Default.Image, "Image"),
        Triple(OverlayType.TICKER, Icons.Default.Newspaper, "Ticker"),
        Triple(OverlayType.TIMER, Icons.Default.Timer, "Timer"),
        Triple(OverlayType.QR_CODE, Icons.Default.QrCode2, "QR Code"),
        Triple(OverlayType.SCOREBOARD, Icons.Default.Scoreboard, "Scoreboard"),
        Triple(OverlayType.ALERT, Icons.Default.NotificationsActive, "Alert"),
        Triple(OverlayType.CHAT_WIDGET, Icons.Default.Chat, "Chat Widget"),
        Triple(OverlayType.SOCIAL_HANDLE, Icons.Default.Share, "Social Handle"),
        Triple(OverlayType.LOWER_THIRD, Icons.Default.Subtitles, "Lower Third"),
        Triple(OverlayType.COUNTDOWN, Icons.Default.HourglassTop, "Countdown"),
        Triple(OverlayType.WATERMARK, Icons.Default.BrandingWatermark, "Watermark"),
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF333333)) }
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text(
                "Add Overlay",
                style = MaterialTheme.typography.titleMedium,
                color = OnSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Choose an overlay type to add to this scene",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            overlayOptions.chunked(4).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { (type, icon, label) ->
                        OverlayTypeButton(
                            icon = icon,
                            label = label,
                            color = overlayTypeColor(type),
                            onClick = { onSelectType(type) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(4 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OverlayTypeButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = OnSurface,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Edit Overlay Sheet
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditOverlaySheet(
    overlay: OverlayItem,
    onDismiss: () -> Unit,
    onUpdate: (OverlayItem) -> Unit
) {
    var text by remember(overlay.id) { mutableStateOf(overlay.text) }
    var alpha by remember(overlay.id) { mutableFloatStateOf(overlay.alpha) }
    var x by remember(overlay.id) { mutableFloatStateOf(overlay.x) }
    var y by remember(overlay.id) { mutableFloatStateOf(overlay.y) }
    var width by remember(overlay.id) { mutableFloatStateOf(overlay.width) }
    var height by remember(overlay.id) { mutableFloatStateOf(overlay.height) }
    var fontSize by remember(overlay.id) { mutableFloatStateOf(overlay.fontSize) }
    var animationType by remember(overlay.id) { mutableStateOf(overlay.animationType) }
    var isVisible by remember(overlay.id) { mutableStateOf(overlay.isVisible) }
    val typeColor = overlayTypeColor(overlay.type)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF333333)) }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(typeColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            overlayTypeIcon(overlay.type),
                            contentDescription = null,
                            tint = typeColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        "Edit ${overlay.type.name.replace("_", " ")}",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = { isVisible = !isVisible }) {
                    Icon(
                        if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle visibility",
                        tint = if (isVisible) Color(0xFF4CAF50) else OnSurfaceMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Text field for text-based overlays
            if (overlay.type in listOf(OverlayType.TEXT, OverlayType.TICKER, OverlayType.ALERT, OverlayType.QR_CODE, OverlayType.LOWER_THIRD, OverlayType.SOCIAL_HANDLE)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = {
                        Text(
                            when (overlay.type) {
                                OverlayType.QR_CODE -> "URL / Content"
                                OverlayType.LOWER_THIRD -> "Name | Title"
                                OverlayType.SOCIAL_HANDLE -> "@handle"
                                else -> "Content"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CameraRed,
                        unfocusedBorderColor = Color(0xFF333333),
                        focusedTextColor = OnSurface,
                        unfocusedTextColor = OnSurface,
                        focusedLabelColor = CameraRed,
                        unfocusedLabelColor = OnSurfaceMuted
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Section header: Dimensions
            SectionHeader("Position & Size")

            SliderProperty(
                label = "Position X",
                value = x,
                onValueChange = { x = it },
                valueRange = 0f..1f,
                displayValue = "${(x * 100).toInt()}%"
            )
            SliderProperty(
                label = "Position Y",
                value = y,
                onValueChange = { y = it },
                valueRange = 0f..1f,
                displayValue = "${(y * 100).toInt()}%"
            )
            SliderProperty(
                label = "Width",
                value = width,
                onValueChange = { width = it },
                valueRange = 0.05f..1f,
                displayValue = "${(width * 100).toInt()}%"
            )
            SliderProperty(
                label = "Height",
                value = height,
                onValueChange = { height = it },
                valueRange = 0.05f..1f,
                displayValue = "${(height * 100).toInt()}%"
            )

            Spacer(modifier = Modifier.height(4.dp))
            SectionHeader("Appearance")

            // Font size for text overlays
            if (overlay.type in listOf(OverlayType.TEXT, OverlayType.TICKER, OverlayType.ALERT, OverlayType.TIMER, OverlayType.LOWER_THIRD, OverlayType.SOCIAL_HANDLE)) {
                SliderProperty(
                    label = "Font Size",
                    value = fontSize,
                    onValueChange = { fontSize = it },
                    valueRange = 8f..72f,
                    displayValue = "${fontSize.toInt()}sp"
                )
            }

            SliderProperty(
                label = "Opacity",
                value = alpha,
                onValueChange = { alpha = it },
                valueRange = 0f..1f,
                displayValue = "${(alpha * 100).toInt()}%"
            )

            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader("Animation")

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AnimationType.entries.toList()) { anim ->
                    FilterChip(
                        selected = animationType == anim,
                        onClick = { animationType = anim },
                        label = {
                            Text(
                                anim.name.replace("_", " ").lowercase()
                                    .replaceFirstChar { it.uppercase() },
                                fontSize = 11.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CameraRed.copy(alpha = 0.2f),
                            selectedLabelColor = CameraRed,
                            containerColor = Color(0xFF242424),
                            labelColor = OnSurfaceMuted
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    onUpdate(
                        overlay.copy(
                            text = text,
                            alpha = alpha,
                            x = x,
                            y = y,
                            width = width,
                            height = height,
                            fontSize = fontSize,
                            animationType = animationType,
                            isVisible = isVisible
                        )
                    )
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CameraRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Apply Changes", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelSmall,
        color = OnSurfaceMuted,
        letterSpacing = 1.sp,
        fontSize = 10.sp,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun SliderProperty(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    displayValue: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = OnSurfaceMuted)
            Text(displayValue, style = MaterialTheme.typography.labelMedium, color = OnSurface)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = CameraRed,
                activeTrackColor = CameraRed,
                inactiveTrackColor = Color(0xFF333333)
            )
        )
    }
}

// ---------------------------------------------------------------------------
// Preset Dialog
// ---------------------------------------------------------------------------

@Composable
private fun PresetDialog(
    presets: List<OverlayPreset>,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onLoad: (OverlayPreset) -> Unit,
    onDelete: (String) -> Unit
) {
    var presetName by remember { mutableStateOf("") }
    var showSaveField by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        titleContentColor = OnSurface,
        textContentColor = OnSurface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.SaveAlt, contentDescription = null, tint = CameraRed, modifier = Modifier.size(20.dp))
                Text("Overlay Presets", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (presets.isEmpty() && !showSaveField) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Layers, contentDescription = null, tint = OnSurfaceMuted, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No presets yet. Save your current overlay layout.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                presets.forEach { preset ->
                    Surface(
                        color = Color(0xFF242424),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLoad(preset) }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Layers, contentDescription = null, tint = OnSurfaceMuted, modifier = Modifier.size(16.dp))
                                Column {
                                    Text(preset.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text("${preset.overlays.size} overlays", style = MaterialTheme.typography.labelSmall, color = OnSurfaceMuted)
                                }
                            }
                            IconButton(onClick = { onDelete(preset.id) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete preset", tint = CameraRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                if (showSaveField) {
                    OutlinedTextField(
                        value = presetName,
                        onValueChange = { presetName = it },
                        label = { Text("Preset name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CameraRed,
                            unfocusedBorderColor = Color(0xFF333333),
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface,
                            focusedLabelColor = CameraRed,
                            unfocusedLabelColor = OnSurfaceMuted
                        )
                    )
                }
            }
        },
        confirmButton = {
            if (showSaveField) {
                Button(
                    onClick = {
                        if (presetName.isNotBlank()) {
                            onSave(presetName)
                            presetName = ""
                            showSaveField = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CameraRed)
                ) {
                    Text("Save")
                }
            } else {
                TextButton(onClick = { showSaveField = true }) {
                    Text("Save Current", color = CameraRed)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = OnSurfaceMuted)
            }
        }
    )
}

// ---------------------------------------------------------------------------
// Empty State
// ---------------------------------------------------------------------------

@Composable
private fun EmptyOverlayPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFF1A1A1A), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Layers,
                    contentDescription = null,
                    tint = OnSurfaceMuted,
                    modifier = Modifier.size(40.dp)
                )
            }
            Text(
                "No overlays in this scene",
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurface,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Tap the + button to add text, images, scoreboards, chat widgets and more",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Pro Banner
// ---------------------------------------------------------------------------

@Composable
private fun ProBanner(message: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = Color(0xFF1A237E).copy(alpha = 0.25f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFF3949AB).copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD600), modifier = Modifier.size(16.dp))
            Text(message, style = MaterialTheme.typography.bodySmall, color = OnSurface)
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                color = Color(0xFF3949AB),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    "Upgrade",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

private fun overlayTypeIcon(type: OverlayType): ImageVector = when (type) {
    OverlayType.TEXT -> Icons.Default.TextFields
    OverlayType.IMAGE -> Icons.Default.Image
    OverlayType.GIF -> Icons.Default.Gif
    OverlayType.TICKER -> Icons.Default.Newspaper
    OverlayType.TIMER -> Icons.Default.Timer
    OverlayType.COUNTDOWN -> Icons.Default.HourglassTop
    OverlayType.QR_CODE -> Icons.Default.QrCode2
    OverlayType.SCOREBOARD -> Icons.Default.Scoreboard
    OverlayType.ALERT -> Icons.Default.NotificationsActive
    OverlayType.LOWER_THIRD -> Icons.Default.Subtitles
    OverlayType.WATERMARK -> Icons.Default.BrandingWatermark
    OverlayType.BROWSER -> Icons.Default.Language
    OverlayType.CHAT_WIDGET -> Icons.Default.Chat
    OverlayType.SOCIAL_HANDLE -> Icons.Default.Share
}

private fun overlayTypeColor(type: OverlayType): Color = when (type) {
    OverlayType.TEXT -> Color(0xFF42A5F5)
    OverlayType.IMAGE -> Color(0xFF66BB6A)
    OverlayType.GIF -> Color(0xFFAB47BC)
    OverlayType.TICKER -> Color(0xFFFFA726)
    OverlayType.TIMER -> Color(0xFF26C6DA)
    OverlayType.COUNTDOWN -> Color(0xFF5C6BC0)
    OverlayType.QR_CODE -> Color(0xFF8D6E63)
    OverlayType.SCOREBOARD -> Color(0xFFEF5350)
    OverlayType.ALERT -> Color(0xFFFFEE58)
    OverlayType.LOWER_THIRD -> Color(0xFF78909C)
    OverlayType.WATERMARK -> Color(0xFF90A4AE)
    OverlayType.BROWSER -> Color(0xFF4DB6AC)
    OverlayType.CHAT_WIDGET -> Color(0xFF7E57C2)
    OverlayType.SOCIAL_HANDLE -> Color(0xFFEC407A)
}
