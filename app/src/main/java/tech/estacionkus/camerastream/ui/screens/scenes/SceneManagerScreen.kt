package tech.estacionkus.camerastream.ui.screens.scenes

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
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
import tech.estacionkus.camerastream.domain.FeatureGate
import tech.estacionkus.camerastream.domain.SceneManager
import tech.estacionkus.camerastream.domain.model.*
import tech.estacionkus.camerastream.ui.theme.*
import java.util.UUID
import javax.inject.Inject

// ---------------------------------------------------------------------------
// UI State
// ---------------------------------------------------------------------------

data class ScenesUiState(
    val scenes: List<Scene>            = emptyList(),
    val activeSceneId: String          = "",
    val transitionType: TransitionType = TransitionType.CUT,
    val isTransitioning: Boolean       = false,
    val showAddDialog: Boolean         = false,
    val showRenameDialog: Boolean      = false,
    val renameSceneId: String?         = null,
    val selectedSceneId: String?       = null,    // for overlay management panel
    val canUseScenes: Boolean          = false
)

// ---------------------------------------------------------------------------
// ViewModel
// ---------------------------------------------------------------------------

@HiltViewModel
class ScenesViewModel @Inject constructor(
    private val sceneManager: SceneManager,
    private val featureGate: FeatureGate
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScenesUiState())
    val uiState: StateFlow<ScenesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                sceneManager.scenes,
                sceneManager.activeSceneId,
                sceneManager.transitionType,
                sceneManager.isTransitioning
            ) { scenes, activeId, transition, transitioning ->
                _uiState.update {
                    it.copy(
                        scenes = scenes,
                        activeSceneId = activeId,
                        transitionType = transition,
                        isTransitioning = transitioning,
                        canUseScenes = featureGate.canScenes()
                    )
                }
            }.collect()
        }
    }

    fun switchScene(id: String)                        = sceneManager.switchTo(id)
    fun addScene(name: String)                         { sceneManager.addScene(name); _uiState.update { it.copy(showAddDialog = false) } }
    fun deleteScene(id: String)                        = sceneManager.deleteScene(id)
    fun renameScene(id: String, newName: String)       { sceneManager.renameScene(id, newName); _uiState.update { it.copy(showRenameDialog = false, renameSceneId = null) } }
    fun duplicateScene(id: String)                     = sceneManager.duplicateScene(id)
    fun setTransitionType(type: TransitionType)        = sceneManager.setTransitionType(type)
    fun showAddDialog(show: Boolean)                   = _uiState.update { it.copy(showAddDialog = show) }
    fun showRenameDialog(sceneId: String?)             = _uiState.update { it.copy(showRenameDialog = sceneId != null, renameSceneId = sceneId) }
    fun selectScene(sceneId: String?)                  = _uiState.update { it.copy(selectedSceneId = sceneId) }

    fun addOverlayToScene(sceneId: String, type: OverlayType) {
        val overlay = OverlayItem(id = UUID.randomUUID().toString(), type = type, text = type.name)
        sceneManager.addOverlay(sceneId, overlay)
    }

    fun removeOverlayFromScene(sceneId: String, overlayId: String) {
        sceneManager.removeOverlay(sceneId, overlayId)
    }
}

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SceneManagerScreen(
    onBack: () -> Unit,
    viewModel: ScenesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val selectedScene = state.scenes.find { it.id == state.selectedSceneId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scenes", color = OnSurface) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = OnSurface)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showAddDialog(true) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Scene", tint = OnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface700,
                    titleContentColor = OnSurface,
                    navigationIconContentColor = OnSurface,
                    actionIconContentColor = OnSurface
                )
            )
        },
        containerColor = Surface900
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!state.canUseScenes) {
                ProBanner(message = "Upgrade to Pro to unlock scene management")
            }

            // Transition type selector with preview animation
            TransitionTypeSelector(
                currentType = state.transitionType,
                onSelect = viewModel::setTransitionType
            )

            // Scene grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.scenes, key = { it.id }) { scene ->
                    SceneCard(
                        scene = scene,
                        isActive = scene.id == state.activeSceneId,
                        isSelected = scene.id == state.selectedSceneId,
                        onTap = { viewModel.switchScene(scene.id) },
                        onLongPress = { viewModel.selectScene(scene.id) },
                        onRename = { viewModel.showRenameDialog(scene.id) },
                        onDuplicate = { viewModel.duplicateScene(scene.id) },
                        onDelete = { viewModel.deleteScene(scene.id) },
                        onManageOverlays = { viewModel.selectScene(scene.id) }
                    )
                }
            }

            // Quick-switch bar
            QuickSwitchBar(
                scenes = state.scenes,
                activeSceneId = state.activeSceneId,
                isTransitioning = state.isTransitioning,
                onSwitch = viewModel::switchScene
            )
        }
    }

    // Overlay management bottom sheet
    if (selectedScene != null) {
        SceneOverlaySheet(
            scene = selectedScene,
            onDismiss = { viewModel.selectScene(null) },
            onAddOverlay = { type -> viewModel.addOverlayToScene(selectedScene.id, type) },
            onRemoveOverlay = { overlayId -> viewModel.removeOverlayFromScene(selectedScene.id, overlayId) }
        )
    }

    // Add scene dialog
    if (state.showAddDialog) {
        AddSceneDialog(
            onDismiss = { viewModel.showAddDialog(false) },
            onConfirm = viewModel::addScene
        )
    }

    // Rename dialog
    if (state.showRenameDialog && state.renameSceneId != null) {
        val scene = state.scenes.find { it.id == state.renameSceneId }
        if (scene != null) {
            RenameSceneDialog(
                currentName = scene.name,
                onDismiss = { viewModel.showRenameDialog(null) },
                onConfirm = { newName -> viewModel.renameScene(scene.id, newName) }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Transition Type Selector
// ---------------------------------------------------------------------------

@Composable
private fun TransitionTypeSelector(
    currentType: TransitionType,
    onSelect: (TransitionType) -> Unit
) {
    // Animated preview dot that shows the chosen transition direction
    val infiniteTransition = rememberInfiniteTransition(label = "transition_preview")
    val dotOffset by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_offset"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface800)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Transition", style = MaterialTheme.typography.labelMedium, color = OnSurfaceMuted)

            // Tiny animated preview
            Box(
                modifier = Modifier
                    .size(width = 48.dp, height = 24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Surface600),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .offset(x = dotOffset.dp)
                        .clip(CircleShape)
                        .background(CameraRed)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TransitionType.entries.forEach { type ->
                FilterChip(
                    selected = currentType == type,
                    onClick = { onSelect(type) },
                    label = { Text(transitionLabel(type), fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = transitionIcon(type),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CameraRed.copy(alpha = 0.2f),
                        selectedLabelColor = CameraRed,
                        selectedLeadingIconColor = CameraRed
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Scene Card
// ---------------------------------------------------------------------------

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SceneCard(
    scene: Scene,
    isActive: Boolean,
    isSelected: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onManageOverlays: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val borderColor = when {
        isActive   -> CameraRed
        isSelected -> Color(0xFF42A5F5)
        else       -> Color.Transparent
    }
    val thumbnailColor = Color(scene.thumbnailColor)
    val scale by animateFloatAsState(
        targetValue = if (isTransitioning(isActive)) 0.97f else 1f,
        animationSpec = tween(150),
        label = "card_scale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .scale(scale)
            .combinedClickable(
                onClick = onTap,
                onLongClick = onLongPress
            ),
        color = Surface800,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if (isActive || isSelected) 2.dp else 0.dp, borderColor)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Colored thumbnail background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(thumbnailColor.copy(alpha = 0.3f))
            )

            // Overlay count badge
            if (scene.overlays.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(Icons.Default.Layers, null, tint = OnSurface, modifier = Modifier.size(9.dp))
                        Text("${scene.overlays.size}", style = MaterialTheme.typography.labelSmall, color = OnSurface, fontSize = 9.sp)
                    }
                }
            }

            // Active / Live badge
            if (isActive) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    color = CameraRed,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "LIVE",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Selected indicator
            if (isSelected && !isActive) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    color = Color(0xFF42A5F5),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "EDIT",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Bottom info bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.7f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = scene.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurface,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = OnSurfaceMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Rename") },
                                leadingIcon = { Icon(Icons.Default.Edit, null) },
                                onClick = { showMenu = false; onRename() }
                            )
                            DropdownMenuItem(
                                text = { Text("Duplicate") },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, null) },
                                onClick = { showMenu = false; onDuplicate() }
                            )
                            DropdownMenuItem(
                                text = { Text("Manage Overlays") },
                                leadingIcon = { Icon(Icons.Default.Layers, null) },
                                onClick = { showMenu = false; onManageOverlays() }
                            )
                            if (!scene.isDefault) {
                                DropdownMenuItem(
                                    text = { Text("Delete", color = CameraRed) },
                                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = CameraRed) },
                                    onClick = { showMenu = false; onDelete() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Placeholder — scene cards don't animate but we keep the function for future use
@Composable
private fun isTransitioning(active: Boolean): Boolean = false

// ---------------------------------------------------------------------------
// Scene Overlay Sheet
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SceneOverlaySheet(
    scene: Scene,
    onDismiss: () -> Unit,
    onAddOverlay: (OverlayType) -> Unit,
    onRemoveOverlay: (String) -> Unit
) {
    var showAddOverlayPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface800,
        titleContentColor = OnSurface,
        textContentColor = OnSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${scene.name} — Overlays", fontWeight = FontWeight.Bold)
                IconButton(
                    onClick = { showAddOverlayPicker = !showAddOverlayPicker },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Overlay", tint = CameraRed)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Overlay type picker
                AnimatedVisibility(visible = showAddOverlayPicker) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Pick overlay type:",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceMuted
                        )
                        val commonTypes = listOf(
                            OverlayType.TEXT,
                            OverlayType.IMAGE,
                            OverlayType.LOWER_THIRD,
                            OverlayType.COUNTDOWN,
                            OverlayType.CHAT_WIDGET,
                            OverlayType.SCOREBOARD,
                            OverlayType.TICKER,
                            OverlayType.WATERMARK
                        )
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            items(commonTypes) { type ->
                                OutlinedButton(
                                    onClick = {
                                        onAddOverlay(type)
                                        showAddOverlayPicker = false
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    border = BorderStroke(1.dp, Surface600)
                                ) {
                                    Text(overlayTypeLabel(type), fontSize = 10.sp, color = OnSurface)
                                }
                            }
                        }
                        HorizontalDivider(color = Surface600)
                    }
                }

                // Current overlays list
                if (scene.overlays.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No overlays yet. Tap + to add one.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceMuted)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 250.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(scene.overlays, key = { it.id }) { overlay ->
                            OverlayListItem(
                                overlay = overlay,
                                onRemove = { onRemoveOverlay(overlay.id) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = CameraRed)
            }
        }
    )
}

@Composable
private fun OverlayListItem(
    overlay: OverlayItem,
    onRemove: () -> Unit
) {
    Surface(
        color = Surface700,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    overlayTypeIcon(overlay.type),
                    contentDescription = null,
                    tint = CameraRed,
                    modifier = Modifier.size(16.dp)
                )
                Column {
                    Text(
                        overlayTypeLabel(overlay.type),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = OnSurface
                    )
                    if (overlay.text.isNotBlank()) {
                        Text(
                            overlay.text,
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = "Remove",
                    tint = CameraRed,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Quick-Switch Bar
// ---------------------------------------------------------------------------

@Composable
private fun QuickSwitchBar(
    scenes: List<Scene>,
    activeSceneId: String,
    isTransitioning: Boolean,
    onSwitch: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Surface800,
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "Quick Switch",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                scenes.forEach { scene ->
                    val isActive = scene.id == activeSceneId
                    Button(
                        onClick = { if (!isTransitioning) onSwitch(scene.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive) CameraRed else Surface700,
                            contentColor   = if (isActive) Color.White else OnSurface
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        enabled = !isTransitioning
                    ) {
                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            scene.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Dialogs
// ---------------------------------------------------------------------------

@Composable
private fun AddSceneDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface800,
        titleContentColor = OnSurface,
        textContentColor = OnSurface,
        title = { Text("New Scene", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Scene name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CameraRed,
                    unfocusedBorderColor = Surface600,
                    focusedTextColor = OnSurface,
                    unfocusedTextColor = OnSurface,
                    focusedLabelColor = CameraRed,
                    unfocusedLabelColor = OnSurfaceMuted
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name) }, enabled = name.isNotBlank()) {
                Text("Create", color = if (name.isNotBlank()) CameraRed else OnSurfaceMuted)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = OnSurfaceMuted) }
        }
    )
}

@Composable
private fun RenameSceneDialog(currentName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface800,
        titleContentColor = OnSurface,
        textContentColor = OnSurface,
        title = { Text("Rename Scene", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Scene name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CameraRed,
                    unfocusedBorderColor = Surface600,
                    focusedTextColor = OnSurface,
                    unfocusedTextColor = OnSurface,
                    focusedLabelColor = CameraRed,
                    unfocusedLabelColor = OnSurfaceMuted
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name) }, enabled = name.isNotBlank()) {
                Text("Rename", color = if (name.isNotBlank()) CameraRed else OnSurfaceMuted)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = OnSurfaceMuted) }
        }
    )
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
        color = Color(0xFF1A237E).copy(alpha = 0.3f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Star, null, tint = Color(0xFFFFD600), modifier = Modifier.size(18.dp))
            Text(message, style = MaterialTheme.typography.bodySmall, color = OnSurface)
        }
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

private fun transitionLabel(type: TransitionType): String = when (type) {
    TransitionType.CUT        -> "Cut"
    TransitionType.FADE       -> "Fade"
    TransitionType.SLIDE_LEFT -> "Slide L"
    TransitionType.SLIDE_RIGHT -> "Slide R"
}

private fun transitionIcon(type: TransitionType) = when (type) {
    TransitionType.CUT        -> Icons.Default.ContentCut
    TransitionType.FADE       -> Icons.Default.Gradient
    TransitionType.SLIDE_LEFT -> Icons.Default.ArrowBack
    TransitionType.SLIDE_RIGHT -> Icons.Default.ArrowForward
}

private fun overlayTypeLabel(type: OverlayType): String = when (type) {
    OverlayType.IMAGE       -> "Image"
    OverlayType.GIF         -> "GIF"
    OverlayType.LOWER_THIRD -> "Lower Third"
    OverlayType.WATERMARK   -> "Watermark"
    OverlayType.COUNTDOWN   -> "Countdown"
    OverlayType.SCOREBOARD  -> "Scoreboard"
    OverlayType.BROWSER     -> "Browser"
    OverlayType.TEXT        -> "Text"
    OverlayType.TICKER      -> "Ticker"
    OverlayType.ALERT       -> "Alert"
    OverlayType.CHAT_WIDGET -> "Chat Widget"
    OverlayType.TIMER       -> "Timer"
    OverlayType.QR_CODE     -> "QR Code"
    OverlayType.SOCIAL_HANDLE -> "Social Handle"
}

private fun overlayTypeIcon(type: OverlayType) = when (type) {
    OverlayType.IMAGE       -> Icons.Default.Image
    OverlayType.GIF         -> Icons.Default.Gif
    OverlayType.LOWER_THIRD -> Icons.Default.Subtitles
    OverlayType.WATERMARK   -> Icons.Default.BrandingWatermark
    OverlayType.COUNTDOWN   -> Icons.Default.Timer
    OverlayType.SCOREBOARD  -> Icons.Default.Scoreboard
    OverlayType.BROWSER     -> Icons.Default.Web
    OverlayType.TEXT        -> Icons.Default.TextFields
    OverlayType.TICKER      -> Icons.Default.ViewStream
    OverlayType.ALERT       -> Icons.Default.NotificationsActive
    OverlayType.CHAT_WIDGET -> Icons.Default.Chat
    OverlayType.TIMER       -> Icons.Default.Timer
    OverlayType.QR_CODE     -> Icons.Default.QrCode
    OverlayType.SOCIAL_HANDLE -> Icons.Default.AlternateEmail
}
