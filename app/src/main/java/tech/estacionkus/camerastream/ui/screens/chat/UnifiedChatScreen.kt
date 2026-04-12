package tech.estacionkus.camerastream.ui.screens.chat

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import tech.estacionkus.camerastream.domain.model.ChatMessage
import tech.estacionkus.camerastream.domain.model.Platform
import tech.estacionkus.camerastream.streaming.MultiChatManager
import tech.estacionkus.camerastream.ui.theme.*
import javax.inject.Inject

// -- Platform colors --------------------------------------------------------

private val TwitchColor  = Color(0xFF9B59B6)
private val KickColor    = Color(0xFF53FC18)
private val YouTubeColor = Color(0xFFFF0000)

private fun Platform.chatColor(): Color = when (this) {
    Platform.TWITCH  -> TwitchColor
    Platform.KICK    -> KickColor
    Platform.YOUTUBE -> YouTubeColor
    else             -> Color.Gray
}

private fun Platform.badge(): String = when (this) {
    Platform.TWITCH  -> "TW"
    Platform.KICK    -> "KI"
    Platform.YOUTUBE -> "YT"
    else             -> "?"
}

// -- Filter options ---------------------------------------------------------

enum class ChatFilter(val label: String) {
    ALL("All"),
    TWITCH("Twitch"),
    KICK("Kick"),
    YOUTUBE("YouTube")
}

// -- ViewModel --------------------------------------------------------------

data class ChatUiState(
    val messages: List<ChatMessage>      = emptyList(),
    val pinnedMessages: List<ChatMessage> = emptyList(),
    val connectedPlatforms: Set<Platform> = emptySet(),
    val twitchChannel: String  = "",
    val kickChannel: String    = "",
    val youtubeChannel: String = "",
    val activeFilter: ChatFilter = ChatFilter.ALL,
    val inputText: String = "",
    val canUseMultiChat: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatManager: MultiChatManager,
    private val featureGate: FeatureGate
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            chatManager.messages.collect { msgs ->
                _uiState.update { it.copy(messages = msgs) }
            }
        }
        viewModelScope.launch {
            chatManager.pinnedMessages.collect { pinned ->
                _uiState.update { it.copy(pinnedMessages = pinned) }
            }
        }
        viewModelScope.launch {
            chatManager.connectedPlatforms.collect { platforms ->
                _uiState.update { it.copy(connectedPlatforms = platforms) }
            }
        }
        _uiState.update { it.copy(canUseMultiChat = featureGate.canMultiChat()) }
    }

    fun setChannel(platform: Platform, channel: String) {
        _uiState.update {
            when (platform) {
                Platform.TWITCH  -> it.copy(twitchChannel = channel)
                Platform.KICK    -> it.copy(kickChannel = channel)
                Platform.YOUTUBE -> it.copy(youtubeChannel = channel)
                else -> it
            }
        }
    }

    fun toggleConnection(platform: Platform) {
        val state = _uiState.value
        if (state.connectedPlatforms.contains(platform)) {
            chatManager.disconnectPlatform(platform)
        } else {
            val channel = when (platform) {
                Platform.TWITCH  -> state.twitchChannel
                Platform.KICK    -> state.kickChannel
                Platform.YOUTUBE -> state.youtubeChannel
                else -> ""
            }
            if (channel.isNotBlank()) {
                chatManager.connect(platform, channel)
            }
        }
    }

    fun pinMessage(messageId: String)   = chatManager.pinMessage(messageId)
    fun unpinMessage(messageId: String) = chatManager.unpinMessage(messageId)
    fun disconnectAll()                 = chatManager.disconnectAll()

    fun setFilter(filter: ChatFilter) {
        _uiState.update { it.copy(activeFilter = filter) }
    }

    fun setInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendCommand(command: String) {
        // Moderator commands: /ban, /timeout, /slow, etc.
        _uiState.update { it.copy(inputText = "") }
    }

    fun filteredMessages(state: ChatUiState): List<ChatMessage> = when (state.activeFilter) {
        ChatFilter.ALL     -> state.messages
        ChatFilter.TWITCH  -> state.messages.filter { it.platform == Platform.TWITCH }
        ChatFilter.KICK    -> state.messages.filter { it.platform == Platform.KICK }
        ChatFilter.YOUTUBE -> state.messages.filter { it.platform == Platform.YOUTUBE }
    }
}

// -- Screen -----------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedChatScreen(
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val filteredMsgs = viewModel.filteredMessages(state)

    LaunchedEffect(filteredMsgs.size) {
        if (filteredMsgs.isNotEmpty()) {
            listState.animateScrollToItem(filteredMsgs.lastIndex)
        }
    }

    Scaffold(
        containerColor = Surface800,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Multi-Platform Chat",
                            style = MaterialTheme.typography.titleMedium,
                            color = OnSurface
                        )
                        Text(
                            "${state.connectedPlatforms.size} platform(s) connected",
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
                    if (state.connectedPlatforms.isNotEmpty()) {
                        IconButton(onClick = { viewModel.disconnectAll() }) {
                            Icon(Icons.Default.LinkOff, contentDescription = "Disconnect All", tint = CameraRed)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface700)
            )
        },
        bottomBar = {
            ChatInputBar(
                text = state.inputText,
                onTextChange = viewModel::setInputText,
                onSend = viewModel::sendCommand
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Platform connection bar
            PlatformConnectionBar(
                state = state,
                onChannelChange = viewModel::setChannel,
                onToggle = viewModel::toggleConnection
            )

            HorizontalDivider(color = Surface600)

            // Pinned messages section
            AnimatedVisibility(visible = state.pinnedMessages.isNotEmpty()) {
                Column {
                    PinnedMessagesBar(
                        pinned = state.pinnedMessages,
                        onUnpin = viewModel::unpinMessage
                    )
                    HorizontalDivider(color = Surface600)
                }
            }

            // Filter chips
            ChatFilterBar(
                activeFilter = state.activeFilter,
                connectedPlatforms = state.connectedPlatforms,
                onFilterSelect = viewModel::setFilter
            )

            HorizontalDivider(color = Surface600)

            // Messages
            if (filteredMsgs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = null,
                            tint = OnSurfaceMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (state.connectedPlatforms.isEmpty())
                                "Connect a platform to see chat"
                            else "No messages yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(filteredMsgs, key = { it.id }) { message ->
                        ChatMessageRow(
                            message = message,
                            isPinned = state.pinnedMessages.any { it.id == message.id },
                            onPin = { viewModel.pinMessage(message.id) },
                            onUnpin = { viewModel.unpinMessage(message.id) }
                        )
                    }
                }
            }
        }
    }
}

// -- Platform connection bar ------------------------------------------------

@Composable
private fun PlatformConnectionBar(
    state: ChatUiState,
    onChannelChange: (Platform, String) -> Unit,
    onToggle: (Platform) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface700)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        PlatformRow(
            platform = Platform.TWITCH,
            label = "Twitch",
            color = TwitchColor,
            channel = state.twitchChannel,
            isConnected = state.connectedPlatforms.contains(Platform.TWITCH),
            onChannelChange = { onChannelChange(Platform.TWITCH, it) },
            onToggle = { onToggle(Platform.TWITCH) }
        )
        PlatformRow(
            platform = Platform.KICK,
            label = "Kick",
            color = KickColor,
            channel = state.kickChannel,
            isConnected = state.connectedPlatforms.contains(Platform.KICK),
            onChannelChange = { onChannelChange(Platform.KICK, it) },
            onToggle = { onToggle(Platform.KICK) }
        )
        PlatformRow(
            platform = Platform.YOUTUBE,
            label = "YouTube",
            color = YouTubeColor,
            channel = state.youtubeChannel,
            isConnected = state.connectedPlatforms.contains(Platform.YOUTUBE),
            onChannelChange = { onChannelChange(Platform.YOUTUBE, it) },
            onToggle = { onToggle(Platform.YOUTUBE) }
        )
    }
}

@Composable
private fun PlatformRow(
    platform: Platform,
    label: String,
    color: Color,
    channel: String,
    isConnected: Boolean,
    onChannelChange: (String) -> Unit,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Connection status dot
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (isConnected) color else Color.Gray.copy(alpha = 0.4f))
        )

        // Platform badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(color.copy(alpha = 0.15f))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        OutlinedTextField(
            value = channel,
            onValueChange = onChannelChange,
            placeholder = { Text("Channel / ID", fontSize = 11.sp, color = OnSurfaceMuted) },
            singleLine = true,
            enabled = !isConnected,
            modifier = Modifier
                .weight(1f)
                .height(44.dp),
            textStyle = MaterialTheme.typography.bodySmall.copy(color = OnSurface),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = color,
                unfocusedBorderColor = Surface600,
                disabledBorderColor = Surface600,
                focusedTextColor = OnSurface,
                unfocusedTextColor = OnSurface,
                disabledTextColor = OnSurfaceMuted
            )
        )

        FilledTonalButton(
            onClick = onToggle,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = if (isConnected) color.copy(alpha = 0.2f) else Surface600
            ),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Text(
                text = if (isConnected) "Disconnect" else "Connect",
                fontSize = 10.sp,
                color = if (isConnected) color else OnSurface
            )
        }
    }
}

// -- Filter bar -------------------------------------------------------------

@Composable
private fun ChatFilterBar(
    activeFilter: ChatFilter,
    connectedPlatforms: Set<Platform>,
    onFilterSelect: (ChatFilter) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface800)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(ChatFilter.values()) { filter ->
            val filterColor = when (filter) {
                ChatFilter.ALL     -> OnSurface
                ChatFilter.TWITCH  -> TwitchColor
                ChatFilter.KICK    -> KickColor
                ChatFilter.YOUTUBE -> YouTubeColor
            }
            val isEnabled = when (filter) {
                ChatFilter.ALL     -> true
                ChatFilter.TWITCH  -> connectedPlatforms.contains(Platform.TWITCH)
                ChatFilter.KICK    -> connectedPlatforms.contains(Platform.KICK)
                ChatFilter.YOUTUBE -> connectedPlatforms.contains(Platform.YOUTUBE)
            }
            FilterChip(
                selected = activeFilter == filter,
                onClick = { if (isEnabled) onFilterSelect(filter) },
                enabled = isEnabled,
                label = { Text(filter.label, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = filterColor.copy(alpha = 0.2f),
                    selectedLabelColor = filterColor
                )
            )
        }
    }
}

// -- Pinned messages --------------------------------------------------------

@Composable
private fun PinnedMessagesBar(
    pinned: List<ChatMessage>,
    onUnpin: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface700.copy(alpha = 0.8f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.PushPin,
                contentDescription = null,
                tint = OnSurfaceMuted,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Pinned (${pinned.size})",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceMuted
            )
        }
        pinned.forEach { msg ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlatformBadge(msg.platform)
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "${msg.author}: ${msg.content}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { onUnpin(msg.id) },
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Unpin",
                        modifier = Modifier.size(12.dp),
                        tint = OnSurfaceMuted
                    )
                }
            }
        }
    }
}

// -- Chat message row -------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatMessageRow(
    message: ChatMessage,
    isPinned: Boolean,
    onPin: () -> Unit,
    onUnpin: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        onClick = { showMenu = true },
        shape = RoundedCornerShape(4.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.Top
        ) {
            PlatformBadge(message.platform)
            Spacer(Modifier.width(4.dp))

            // User badges (mod, sub, etc.)
            message.badges.forEach { badge ->
                BadgeChip(badge)
                Spacer(Modifier.width(2.dp))
            }

            // Username
            Text(
                text = message.author,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = runCatching { Color(android.graphics.Color.parseColor(message.authorColor)) }
                    .getOrDefault(message.platform.chatColor())
            )

            Text(
                text = ": ",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceMuted
            )

            // Emote-aware content (display text as-is; emotes shown as text tokens)
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurface,
                modifier = Modifier.weight(1f)
            )

            if (isPinned) {
                Icon(
                    Icons.Default.PushPin,
                    contentDescription = "Pinned",
                    modifier = Modifier.size(11.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            if (isPinned) {
                DropdownMenuItem(
                    text = { Text("Unpin") },
                    onClick = { onUnpin(); showMenu = false },
                    leadingIcon = { Icon(Icons.Default.PushPin, null) }
                )
            } else {
                DropdownMenuItem(
                    text = { Text("Pin") },
                    onClick = { onPin(); showMenu = false },
                    leadingIcon = { Icon(Icons.Default.PushPin, null) }
                )
            }
            DropdownMenuItem(
                text = { Text("/ban ${message.author}", color = CameraRed) },
                onClick = { showMenu = false },
                leadingIcon = { Icon(Icons.Default.Block, null, tint = CameraRed) }
            )
            DropdownMenuItem(
                text = { Text("/timeout ${message.author} 60") },
                onClick = { showMenu = false },
                leadingIcon = { Icon(Icons.Default.Timer, null) }
            )
        }
    }
}

// -- Chat input bar ---------------------------------------------------------

@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: (String) -> Unit
) {
    val keyboard = LocalSoftwareKeyboardController.current

    Surface(
        color = Surface700,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { Text("Type /ban, /timeout... ", fontSize = 12.sp, color = OnSurfaceMuted) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodySmall.copy(color = OnSurface),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (text.isNotBlank()) { onSend(text); keyboard?.hide() }
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CameraRed,
                    unfocusedBorderColor = Surface600,
                    focusedTextColor = OnSurface,
                    unfocusedTextColor = OnSurface
                )
            )

            IconButton(
                onClick = {
                    if (text.isNotBlank()) { onSend(text); keyboard?.hide() }
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (text.isNotBlank()) CameraRed else Surface600)
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (text.isNotBlank()) Color.White else OnSurfaceMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// -- Utility composables ----------------------------------------------------

@Composable
private fun PlatformBadge(platform: Platform) {
    val color = platform.chatColor()
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 4.dp, vertical = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = platform.badge(),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun BadgeChip(badge: String) {
    val label = when (badge) {
        "moderator"   -> "MOD"
        "subscriber"  -> "SUB"
        "vip"         -> "VIP"
        "broadcaster" -> "HOST"
        else          -> badge.take(3).uppercase()
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(Surface600)
            .padding(horizontal = 3.dp, vertical = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontSize = 8.sp, color = OnSurfaceMuted)
    }
}
