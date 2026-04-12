package tech.estacionkus.camerastream.ui.screens.guest

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tech.estacionkus.camerastream.domain.FeatureGate
import tech.estacionkus.camerastream.streaming.GuestInfo
import tech.estacionkus.camerastream.streaming.GuestModeManager
import tech.estacionkus.camerastream.streaming.GuestModeState
import tech.estacionkus.camerastream.ui.theme.*
import javax.inject.Inject

// -- PiP position -----------------------------------------------------------

enum class PipPosition(val label: String) {
    TOP_LEFT("Top-Left"),
    TOP_RIGHT("Top-Right"),
    BOTTOM_LEFT("Bottom-Left"),
    BOTTOM_RIGHT("Bottom-Right")
}

// -- ViewModel --------------------------------------------------------------

data class GuestUiState(
    val state: GuestModeState    = GuestModeState.IDLE,
    val inviteCode: String?      = null,
    val inviteUrl: String?       = null,
    val guests: List<GuestInfo>  = emptyList(),
    val errorMessage: String?    = null,
    val pipPosition: PipPosition = PipPosition.BOTTOM_RIGHT,
    val canUseGuestMode: Boolean = false,
    val maxGuests: Int           = 0
)

@HiltViewModel
class GuestViewModel @Inject constructor(
    private val guestManager: GuestModeManager,
    private val featureGate: FeatureGate
) : ViewModel() {

    private val _uiState = MutableStateFlow(GuestUiState())
    val uiState: StateFlow<GuestUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            guestManager.state.collect { s ->
                _uiState.update { it.copy(state = s) }
            }
        }
        viewModelScope.launch {
            guestManager.inviteCode.collect { code ->
                _uiState.update { it.copy(inviteCode = code) }
            }
        }
        viewModelScope.launch {
            guestManager.inviteUrl.collect { url ->
                _uiState.update { it.copy(inviteUrl = url) }
            }
        }
        viewModelScope.launch {
            guestManager.guests.collect { list ->
                _uiState.update { it.copy(guests = list) }
            }
        }
        viewModelScope.launch {
            guestManager.errorMessage.collect { err ->
                _uiState.update { it.copy(errorMessage = err) }
            }
        }
        _uiState.update {
            it.copy(
                canUseGuestMode = featureGate.canGuestMode(),
                maxGuests = featureGate.maxGuests()
            )
        }
    }

    fun generateInvite()                      = guestManager.generateInvite()
    fun removeGuest(guestId: String)          = guestManager.removeGuest(guestId)
    fun toggleGuestAudio(guestId: String)     = guestManager.toggleGuestAudio(guestId)
    fun toggleGuestVideo(guestId: String)     = guestManager.toggleGuestVideo(guestId)
    fun stopGuestMode()                       = guestManager.stopGuestMode()
    fun clearError()                          = guestManager.clearError()

    fun setPipPosition(position: PipPosition) {
        _uiState.update { it.copy(pipPosition = position) }
    }
}

// -- Screen -----------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestModeScreen(
    onBack: () -> Unit,
    viewModel: GuestViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = Surface800,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Guest Mode", color = OnSurface)
                        Text(
                            "${state.guests.size}/${state.maxGuests} guests",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = OnSurface)
                    }
                },
                actions = {
                    AnimatedVisibility(visible = state.state != GuestModeState.IDLE) {
                        TextButton(onClick = { viewModel.stopGuestMode() }) {
                            Text("Stop Session", color = CameraRed)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface700)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Feature gate banner
            if (!state.canUseGuestMode) {
                item { ProUpgradeBanner("Upgrade to Pro to use Guest Mode (up to 2 guests)") }
            }

            // Connection state card
            item { ConnectionStateCard(state.state) }

            // Guest slot counter
            item { GuestSlotCounter(used = state.guests.size, max = state.maxGuests) }

            // Invite section
            item {
                InviteSection(
                    state = state,
                    onGenerate = viewModel::generateInvite,
                    context = context
                )
            }

            // Layout preview
            item {
                LayoutPreviewCard(
                    guestCount = state.guests.size,
                    pipPosition = state.pipPosition,
                    onPositionSelect = viewModel::setPipPosition
                )
            }

            // Connected guests
            if (state.guests.isNotEmpty()) {
                item {
                    Text(
                        text = "Active Guests",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                }
                items(state.guests, key = { it.id }) { guest ->
                    GuestRow(
                        guest = guest,
                        onToggleAudio  = { viewModel.toggleGuestAudio(guest.id) },
                        onToggleVideo  = { viewModel.toggleGuestVideo(guest.id) },
                        onRemove       = { viewModel.removeGuest(guest.id) }
                    )
                }
            }
        }
    }
}

// -- Pro upgrade banner -----------------------------------------------------

@Composable
private fun ProUpgradeBanner(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A237E).copy(alpha = 0.3f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD600), modifier = Modifier.size(18.dp))
            Text(message, style = MaterialTheme.typography.bodySmall, color = OnSurface)
        }
    }
}

// -- Connection state card --------------------------------------------------

@Composable
private fun ConnectionStateCard(connectionState: GuestModeState) {
    val (label, color, icon) = when (connectionState) {
        GuestModeState.IDLE      -> Triple("Idle — Generate an invite to start", Color.Gray, Icons.Default.Circle)
        GuestModeState.WAITING   -> Triple("Waiting for guest to connect...", Color(0xFFFF9800), Icons.Default.HourglassEmpty)
        GuestModeState.CONNECTED -> Triple("Guest connected", Color(0xFF4CAF50), Icons.Default.CheckCircle)
        GuestModeState.ERROR     -> Triple("Connection error", CameraRed, Icons.Default.Error)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Column {
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceMuted
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = color
                )
            }
        }
    }
}

// -- Guest slot counter -----------------------------------------------------

@Composable
private fun GuestSlotCounter(used: Int, max: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface700)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Guest Slots",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceMuted
                )
                Text(
                    text = "$used / $max",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        max == 0       -> Color.Gray
                        used >= max    -> CameraRed
                        used > max / 2 -> Color(0xFFFF9800)
                        else           -> Color(0xFF4CAF50)
                    }
                )
            }
            // Slot dots
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(max.coerceAtLeast(0)) { index ->
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(
                                if (index < used) Color(0xFF4CAF50)
                                else Surface600
                            )
                    )
                }
            }
        }
    }
}

// -- Invite section ---------------------------------------------------------

@Composable
private fun InviteSection(
    state: GuestUiState,
    onGenerate: () -> Unit,
    context: Context
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface700)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Invite Link",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )

            if (state.inviteCode == null) {
                Button(
                    onClick = onGenerate,
                    enabled = state.canUseGuestMode,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CameraRed)
                ) {
                    Icon(Icons.Default.Link, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Generate Invite Link")
                }
            } else {
                // Code display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Surface600)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.inviteCode,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 4.sp
                        ),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = OnSurface
                    )
                }

                state.inviteUrl?.let { url ->
                    Text(
                        text = url,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(
                                ClipData.newPlainText("Invite URL", state.inviteUrl ?: state.inviteCode)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Surface600)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Copy", fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Join my stream: ${state.inviteUrl ?: state.inviteCode}")
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Invite"))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = CameraRed)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Share", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// -- Layout preview ---------------------------------------------------------

@Composable
private fun LayoutPreviewCard(
    guestCount: Int,
    pipPosition: PipPosition,
    onPositionSelect: (PipPosition) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface700)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Layout Preview",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )

            // Preview box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Surface600)
                    .border(1.dp, Surface600, RoundedCornerShape(8.dp))
            ) {
                // Host area
                Text(
                    text = "Host Camera",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceMuted,
                    modifier = Modifier.align(Alignment.Center)
                )

                // PiP guest box
                if (guestCount > 0 || true) {
                    val pipAlignment = when (pipPosition) {
                        PipPosition.TOP_LEFT     -> Alignment.TopStart
                        PipPosition.TOP_RIGHT    -> Alignment.TopEnd
                        PipPosition.BOTTOM_LEFT  -> Alignment.BottomStart
                        PipPosition.BOTTOM_RIGHT -> Alignment.BottomEnd
                    }
                    Box(
                        modifier = Modifier
                            .align(pipAlignment)
                            .padding(8.dp)
                            .size(width = 64.dp, height = 48.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF4CAF50).copy(alpha = 0.25f))
                            .border(1.dp, Color(0xFF4CAF50).copy(alpha = 0.7f), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Guest",
                            fontSize = 9.sp,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }

            // Position buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PipPosition.entries.forEach { position ->
                    FilterChip(
                        selected = pipPosition == position,
                        onClick = { onPositionSelect(position) },
                        label = { Text(position.label, fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CameraRed.copy(alpha = 0.2f),
                            selectedLabelColor = CameraRed
                        )
                    )
                }
            }
        }
    }
}

// -- Guest row --------------------------------------------------------------

@Composable
private fun GuestRow(
    guest: GuestInfo,
    onToggleAudio: () -> Unit,
    onToggleVideo: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface700)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CameraRed.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = guest.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CameraRed
                    )
                }

                // Name + connection status
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = guest.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = OnSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (guest.isConnected) Color(0xFF4CAF50) else Color.Gray
                                )
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (guest.isConnected) "Connected" else "Disconnected",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (guest.isConnected) Color(0xFF4CAF50) else Color.Gray
                        )
                    }
                }

                // Kick button
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.PersonRemove,
                        contentDescription = "Remove Guest",
                        tint = CameraRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Controls row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Audio toggle
                GuestControlChip(
                    label = if (guest.hasAudio) "Mic ON" else "Mic OFF",
                    icon = if (guest.hasAudio) Icons.Default.Mic else Icons.Default.MicOff,
                    active = guest.hasAudio,
                    onClick = onToggleAudio,
                    modifier = Modifier.weight(1f)
                )

                // Video toggle
                GuestControlChip(
                    label = if (guest.hasVideo) "Video ON" else "Video OFF",
                    icon = if (guest.hasVideo) Icons.Default.Videocam else Icons.Default.VideocamOff,
                    active = guest.hasVideo,
                    onClick = onToggleVideo,
                    modifier = Modifier.weight(1f)
                )
            }

            // Audio level indicator bar
            AudioLevelBar(level = if (guest.hasAudio && guest.isConnected) 0.65f else 0f)
        }
    }
}

@Composable
private fun GuestControlChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (active) OnSurface else OnSurfaceMuted
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (active) Surface600 else CameraRed.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 11.sp)
    }
}

@Composable
private fun AudioLevelBar(level: Float) {
    Column {
        Text(
            text = "Audio Level",
            style = MaterialTheme.typography.labelSmall,
            color = OnSurfaceMuted,
            fontSize = 10.sp
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Surface600)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(level.coerceIn(0f, 1f))
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        when {
                            level > 0.8f -> CameraRed
                            level > 0.5f -> Color(0xFF4CAF50)
                            else         -> Color(0xFF4CAF50).copy(alpha = 0.6f)
                        }
                    )
            )
        }
    }
}
