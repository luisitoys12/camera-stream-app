package tech.estacionkus.camerastream.ui.screens.sports

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
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
import tech.estacionkus.camerastream.domain.model.*
import tech.estacionkus.camerastream.streaming.SportsStateManager
import tech.estacionkus.camerastream.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// ---------------------------------------------------------------------------
// Game Event
// ---------------------------------------------------------------------------

data class GameEvent(
    val id: String = UUID.randomUUID().toString(),
    val type: GameEventType,
    val teamLabel: String,
    val clock: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class GameEventType(val label: String, val icon: ImageVector, val color: Color) {
    GOAL("Goal", Icons.Default.SportsScore, Color(0xFF4CAF50)),
    FOUL("Foul", Icons.Default.GppBad, Color(0xFFFF9800)),
    TIMEOUT("Timeout", Icons.Default.PauseCircle, Color(0xFF2196F3)),
    PERIOD_END("Period End", Icons.Default.Flag, Color(0xFF9C27B0)),
    OTHER("Event", Icons.Default.NotificationsActive, Color(0xFF607D8B))
}

// ---------------------------------------------------------------------------
// UI State
// ---------------------------------------------------------------------------

data class SportsModeUiState(
    val sportState: SportState = SportState(),
    val formattedClock: String = "00:00",
    val scoreboardText: String = "",
    val teamANameInput: String = "Team A",
    val teamBNameInput: String = "Team B",
    val teamAColor: Long = 0xFF1E88E5,
    val teamBColor: Long = 0xFFE53935,
    val gameEvents: List<GameEvent> = emptyList(),
    val showEventLog: Boolean = false,
    val canUseSportsMode: Boolean = false
)

// ---------------------------------------------------------------------------
// ViewModel
// ---------------------------------------------------------------------------

@HiltViewModel
class SportsModeViewModel @Inject constructor(
    private val sportsStateManager: SportsStateManager,
    private val featureGate: FeatureGate
) : ViewModel() {

    private val _uiState = MutableStateFlow(SportsModeUiState())
    val uiState: StateFlow<SportsModeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sportsStateManager.state.collect { sportState ->
                _uiState.update {
                    it.copy(
                        sportState = sportState,
                        formattedClock = sportsStateManager.formatClock(),
                        scoreboardText = sportsStateManager.getScoreboardText(),
                        teamANameInput = sportState.teamA.name,
                        teamBNameInput = sportState.teamB.name,
                        teamAColor = sportState.teamA.color,
                        teamBColor = sportState.teamB.color,
                        canUseSportsMode = featureGate.canSportsMode()
                    )
                }
            }
        }
    }

    fun setSportType(type: SportType) {
        sportsStateManager.setSportType(type)
    }

    fun setTeamAName(name: String) {
        _uiState.update { it.copy(teamANameInput = name) }
        sportsStateManager.setTeamA(name, _uiState.value.teamAColor)
    }

    fun setTeamBName(name: String) {
        _uiState.update { it.copy(teamBNameInput = name) }
        sportsStateManager.setTeamB(name, _uiState.value.teamBColor)
    }

    fun setTeamAColor(color: Long) {
        _uiState.update { it.copy(teamAColor = color) }
        sportsStateManager.setTeamA(_uiState.value.teamANameInput, color)
    }

    fun setTeamBColor(color: Long) {
        _uiState.update { it.copy(teamBColor = color) }
        sportsStateManager.setTeamB(_uiState.value.teamBNameInput, color)
    }

    fun incrementScoreA() {
        sportsStateManager.incrementScoreA()
        addEvent(GameEventType.GOAL, _uiState.value.teamANameInput)
    }

    fun decrementScoreA() = sportsStateManager.decrementScoreA()

    fun incrementScoreB() {
        sportsStateManager.incrementScoreB()
        addEvent(GameEventType.GOAL, _uiState.value.teamBNameInput)
    }

    fun decrementScoreB() = sportsStateManager.decrementScoreB()

    fun startClock() = sportsStateManager.startClock()
    fun stopClock() = sportsStateManager.stopClock()
    fun resetClock() = sportsStateManager.resetClock()

    fun setPeriod(period: String) {
        sportsStateManager.setPeriod(period)
        addEvent(GameEventType.PERIOD_END, period)
    }

    fun nextPeriod() {
        sportsStateManager.nextPeriod()
        addEvent(GameEventType.PERIOD_END, "Next Period")
    }

    fun logFoul(teamName: String) = addEvent(GameEventType.FOUL, teamName)
    fun logTimeout(teamName: String) = addEvent(GameEventType.TIMEOUT, teamName)
    fun logCustomEvent(teamName: String) = addEvent(GameEventType.OTHER, teamName)

    fun clearEventLog() {
        _uiState.update { it.copy(gameEvents = emptyList()) }
    }

    fun toggleEventLog() {
        _uiState.update { it.copy(showEventLog = !it.showEventLog) }
    }

    fun resetAll() {
        sportsStateManager.resetAll()
        _uiState.update { it.copy(gameEvents = emptyList()) }
    }

    private fun addEvent(type: GameEventType, team: String) {
        val event = GameEvent(
            type = type,
            teamLabel = team,
            clock = sportsStateManager.formatClock()
        )
        _uiState.update { it.copy(gameEvents = listOf(event) + it.gameEvents) }
    }
}

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SportsModeScreen(
    onBack: () -> Unit,
    viewModel: SportsModeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Sports Mode",
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
                    IconButton(onClick = { viewModel.toggleEventLog() }) {
                        Icon(
                            Icons.Default.FormatListBulleted,
                            contentDescription = "Event Log",
                            tint = if (state.showEventLog) CameraRed else OnSurface
                        )
                    }
                    IconButton(onClick = { viewModel.resetAll() }) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset All", tint = OnSurface)
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
        containerColor = Color(0xFF0D0D0D)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.showEventLog) {
                // Show event log panel
                EventLogPanel(
                    events = state.gameEvents,
                    onClear = viewModel::clearEventLog,
                    onClose = viewModel::toggleEventLog,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Sport type selector
                    SportTypeSelector(
                        selectedType = state.sportState.sportType,
                        onSelect = viewModel::setSportType
                    )

                    // Live scoreboard preview
                    ScoreboardPreview(
                        state = state.sportState,
                        formattedClock = state.formattedClock,
                        teamAColor = state.teamAColor,
                        teamBColor = state.teamBColor
                    )

                    // Score controls
                    ScoreControls(
                        teamAName = state.sportState.teamA.name,
                        teamAScore = state.sportState.teamA.score,
                        teamAColor = state.teamAColor,
                        teamBName = state.sportState.teamB.name,
                        teamBScore = state.sportState.teamB.score,
                        teamBColor = state.teamBColor,
                        onIncrementA = viewModel::incrementScoreA,
                        onDecrementA = viewModel::decrementScoreA,
                        onIncrementB = viewModel::incrementScoreB,
                        onDecrementB = viewModel::decrementScoreB
                    )

                    // Game clock
                    GameClockControls(
                        formattedClock = state.formattedClock,
                        isRunning = state.sportState.isClockRunning,
                        onStart = viewModel::startClock,
                        onStop = viewModel::stopClock,
                        onReset = viewModel::resetClock
                    )

                    // Period selector
                    PeriodSelector(
                        periods = state.sportState.sportType.periods,
                        currentPeriod = state.sportState.period,
                        onSelectPeriod = viewModel::setPeriod,
                        onNextPeriod = viewModel::nextPeriod
                    )

                    // Team editors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TeamEditor(
                            label = "Team A",
                            name = state.teamANameInput,
                            color = state.teamAColor,
                            onNameChange = viewModel::setTeamAName,
                            onColorChange = viewModel::setTeamAColor,
                            modifier = Modifier.weight(1f)
                        )
                        TeamEditor(
                            label = "Team B",
                            name = state.teamBNameInput,
                            color = state.teamBColor,
                            onNameChange = viewModel::setTeamBName,
                            onColorChange = viewModel::setTeamBColor,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Quick event buttons
                    QuickEventButtons(
                        teamAName = state.sportState.teamA.name,
                        teamBName = state.sportState.teamB.name,
                        onFoulA = { viewModel.logFoul(state.sportState.teamA.name) },
                        onFoulB = { viewModel.logFoul(state.sportState.teamB.name) },
                        onTimeoutA = { viewModel.logTimeout(state.sportState.teamA.name) },
                        onTimeoutB = { viewModel.logTimeout(state.sportState.teamB.name) }
                    )

                    // Recent events preview
                    if (state.gameEvents.isNotEmpty()) {
                        RecentEventsPreview(
                            events = state.gameEvents.take(3),
                            onSeeAll = viewModel::toggleEventLog
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Sport Type Selector
// ---------------------------------------------------------------------------

@Composable
private fun SportTypeSelector(
    selectedType: SportType,
    onSelect: (SportType) -> Unit
) {
    Column {
        Text(
            "Sport Type",
            style = MaterialTheme.typography.labelMedium,
            color = OnSurfaceMuted,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(SportType.entries.toList()) { type ->
                val selected = selectedType == type
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onSelect(type) },
                    color = if (selected) CameraRed else Color(0xFF1A1A1A),
                    shape = RoundedCornerShape(20.dp),
                    border = if (selected) null else BorderStroke(1.dp, Color(0xFF333333))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = sportTypeIcon(type),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (selected) Color.White else OnSurfaceMuted
                        )
                        Text(
                            type.displayName,
                            fontSize = 13.sp,
                            color = if (selected) Color.White else OnSurfaceMuted,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Scoreboard Preview
// ---------------------------------------------------------------------------

@Composable
private fun ScoreboardPreview(
    state: SportState,
    formattedClock: String,
    teamAColor: Long,
    teamBColor: Long
) {
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF080818),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF2A2A4A))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "LIVE PREVIEW",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceMuted,
                    letterSpacing = 2.sp,
                    fontSize = 9.sp
                )
                if (state.isClockRunning) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(CameraRed.copy(alpha = pulseAlpha))
                        )
                        Text(
                            "LIVE",
                            fontSize = 9.sp,
                            color = CameraRed,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Scoreboard widget
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF1A1A3E), Color(0xFF0D0D1E))
                        ),
                        RoundedCornerShape(12.dp)
                    )
                    .border(1.dp, Color(0xFF2A2A5A), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Period badge + clock
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = CameraRed.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                state.period,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = CameraRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            formattedClock,
                            style = MaterialTheme.typography.titleLarge,
                            color = if (state.isClockRunning) Color(0xFF4CAF50) else OnSurface,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Teams and scores
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Team A
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(teamAColor))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                state.teamA.name,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(teamAColor),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Score
                        Row(
                            modifier = Modifier.weight(1.5f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "${state.teamA.score}",
                                style = MaterialTheme.typography.displayMedium,
                                color = Color(teamAColor),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 52.sp
                            )
                            Text(
                                "  –  ",
                                style = MaterialTheme.typography.headlineMedium,
                                color = OnSurfaceMuted,
                                fontWeight = FontWeight.Light
                            )
                            Text(
                                "${state.teamB.score}",
                                style = MaterialTheme.typography.displayMedium,
                                color = Color(teamBColor),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 52.sp
                            )
                        }

                        // Team B
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(teamBColor))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                state.teamB.name,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(teamBColor),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                "This is how the overlay appears on your stream",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceMuted,
                fontSize = 10.sp
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Team Editor
// ---------------------------------------------------------------------------

private val teamColorOptions = listOf(
    0xFF1E88E5L, 0xFFE53935L, 0xFF43A047L, 0xFFFDD835L,
    0xFFFF6F00L, 0xFF8E24AAL, 0xFF00ACC1L, 0xFFFFFFFFL,
    0xFF5C6BC0L, 0xFFEC407AL, 0xFF26A69AL, 0xFF78909CL
)

@Composable
private fun TeamEditor(
    label: String,
    name: String,
    color: Long,
    onNameChange: (String) -> Unit,
    onColorChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(color).copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = Color(color),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Name", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(color),
                    unfocusedBorderColor = Color(0xFF333333),
                    focusedTextColor = OnSurface,
                    unfocusedTextColor = OnSurface,
                    focusedLabelColor = Color(color),
                    unfocusedLabelColor = OnSurfaceMuted
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Color", style = MaterialTheme.typography.labelSmall, color = OnSurfaceMuted)
            Spacer(modifier = Modifier.height(4.dp))

            val rows = teamColorOptions.chunked(6)
            rows.forEach { rowColors ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    rowColors.forEach { colorOption ->
                        val isSelected = color == colorOption
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 26.dp else 22.dp)
                                .clip(CircleShape)
                                .background(Color(colorOption))
                                .then(
                                    if (isSelected) Modifier.border(2.dp, Color.White, CircleShape)
                                    else Modifier.border(1.dp, Color(0xFF333333), CircleShape)
                                )
                                .clickable { onColorChange(colorOption) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Score Controls
// ---------------------------------------------------------------------------

@Composable
private fun ScoreControls(
    teamAName: String,
    teamAScore: Int,
    teamAColor: Long,
    teamBName: String,
    teamBScore: Int,
    teamBColor: Long,
    onIncrementA: () -> Unit,
    onDecrementA: () -> Unit,
    onIncrementB: () -> Unit,
    onDecrementB: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScoreColumn(
            teamName = teamAName,
            score = teamAScore,
            color = teamAColor,
            onIncrement = onIncrementA,
            onDecrement = onDecrementA,
            modifier = Modifier.weight(1f)
        )
        ScoreColumn(
            teamName = teamBName,
            score = teamBScore,
            color = teamBColor,
            onIncrement = onIncrementB,
            onDecrement = onDecrementB,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ScoreColumn(
    teamName: String,
    score: Int,
    color: Long,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF2A2A2A))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                teamName,
                style = MaterialTheme.typography.labelLarge,
                color = Color(color),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            AnimatedContent(
                targetState = score,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInVertically { -it } + fadeIn() togetherWith
                            slideOutVertically { it } + fadeOut()
                    } else {
                        slideInVertically { it } + fadeIn() togetherWith
                            slideOutVertically { -it } + fadeOut()
                    }
                },
                label = "scoreAnim"
            ) { animScore ->
                Text(
                    "$animScore",
                    style = MaterialTheme.typography.displayMedium,
                    color = OnSurface,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 56.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = onDecrement,
                    modifier = Modifier.size(52.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF242424)
                    )
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Decrease",
                        modifier = Modifier.size(26.dp),
                        tint = CameraRed
                    )
                }

                Button(
                    onClick = onIncrement,
                    modifier = Modifier.size(52.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(color)
                    )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Increase",
                        modifier = Modifier.size(26.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Game Clock Controls
// ---------------------------------------------------------------------------

@Composable
private fun GameClockControls(
    formattedClock: String,
    isRunning: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onReset: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF2A2A2A))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "GAME CLOCK",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceMuted,
                letterSpacing = 2.sp,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                formattedClock,
                fontSize = 52.sp,
                color = if (isRunning) Color(0xFF4CAF50) else OnSurface,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { if (isRunning) onStop() else onStart() },
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) CameraRed else Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (isRunning) "Stop" else "Start",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF333333))
                ) {
                    Icon(
                        Icons.Default.RestartAlt,
                        contentDescription = "Reset",
                        tint = OnSurfaceMuted,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset", color = OnSurfaceMuted)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Period Selector
// ---------------------------------------------------------------------------

@Composable
private fun PeriodSelector(
    periods: List<String>,
    currentPeriod: String,
    onSelectPeriod: (String) -> Unit,
    onNextPeriod: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF2A2A2A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "PERIOD",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceMuted,
                    letterSpacing = 2.sp,
                    fontSize = 10.sp
                )
                TextButton(
                    onClick = onNextPeriod,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Next", color = CameraRed, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = CameraRed,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(periods) { period ->
                    val selected = currentPeriod == period
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onSelectPeriod(period) },
                        color = if (selected) CameraRed else Color(0xFF242424),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            period,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            fontSize = 13.sp,
                            color = if (selected) Color.White else OnSurfaceMuted,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Quick Event Buttons
// ---------------------------------------------------------------------------

@Composable
private fun QuickEventButtons(
    teamAName: String,
    teamBName: String,
    onFoulA: () -> Unit,
    onFoulB: () -> Unit,
    onTimeoutA: () -> Unit,
    onTimeoutB: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF2A2A2A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "QUICK EVENTS",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceMuted,
                letterSpacing = 2.sp,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Foul buttons
                QuickEventButton(
                    label = "Foul",
                    teamName = teamAName,
                    icon = Icons.Default.GppBad,
                    color = Color(0xFFFF9800),
                    onClick = onFoulA,
                    modifier = Modifier.weight(1f)
                )
                QuickEventButton(
                    label = "Foul",
                    teamName = teamBName,
                    icon = Icons.Default.GppBad,
                    color = Color(0xFFFF9800),
                    onClick = onFoulB,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Timeout buttons
                QuickEventButton(
                    label = "Timeout",
                    teamName = teamAName,
                    icon = Icons.Default.PauseCircle,
                    color = Color(0xFF2196F3),
                    onClick = onTimeoutA,
                    modifier = Modifier.weight(1f)
                )
                QuickEventButton(
                    label = "Timeout",
                    teamName = teamBName,
                    icon = Icons.Default.PauseCircle,
                    color = Color(0xFF2196F3),
                    onClick = onTimeoutB,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QuickEventButton(
    label: String,
    teamName: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = color.copy(alpha = 0.08f)
        )
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(label, fontSize = 10.sp, color = color, fontWeight = FontWeight.SemiBold)
            Text(teamName, fontSize = 9.sp, color = OnSurfaceMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// ---------------------------------------------------------------------------
// Recent Events Preview
// ---------------------------------------------------------------------------

@Composable
private fun RecentEventsPreview(
    events: List<GameEvent>,
    onSeeAll: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF2A2A2A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "RECENT EVENTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceMuted,
                    letterSpacing = 2.sp,
                    fontSize = 10.sp
                )
                TextButton(
                    onClick = onSeeAll,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("See All", color = CameraRed, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            events.forEach { event ->
                EventRow(event = event)
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Event Log Panel
// ---------------------------------------------------------------------------

@Composable
private fun EventLogPanel(
    events: List<GameEvent>,
    onClear: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A1A))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "GAME EVENT LOG",
                style = MaterialTheme.typography.labelMedium,
                color = OnSurface,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Row {
                if (events.isNotEmpty()) {
                    TextButton(onClick = onClear) {
                        Text("Clear", color = OnSurfaceMuted, fontSize = 13.sp)
                    }
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = OnSurface)
                }
            }
        }

        HorizontalDivider(color = Color(0xFF2A2A2A))

        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.SportsSoccer,
                        contentDescription = null,
                        tint = OnSurfaceMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No events yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceMuted
                    )
                    Text(
                        "Events are logged when you score goals, fouls, and timeouts",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(events, key = { it.id }) { event ->
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInHorizontally { -it } + fadeIn()
                    ) {
                        EventRow(event = event, showTime = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun EventRow(
    event: GameEvent,
    showTime: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(event.type.color.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(event.type.color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                event.type.icon,
                contentDescription = null,
                tint = event.type.color,
                modifier = Modifier.size(16.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${event.type.label} — ${event.teamLabel}",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurface,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Clock: ${event.clock}",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceMuted,
                fontSize = 10.sp
            )
        }
        if (showTime) {
            Text(
                SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(event.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceMuted,
                fontSize = 10.sp
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

private fun sportTypeIcon(type: SportType): ImageVector = when (type) {
    SportType.SOCCER -> Icons.Default.SportsSoccer
    SportType.BASKETBALL -> Icons.Default.SportsBasketball
    SportType.BASEBALL -> Icons.Default.SportsBaseball
    SportType.BOXING -> Icons.Default.SportsMma
    SportType.GENERIC -> Icons.Default.EmojiEvents
}
