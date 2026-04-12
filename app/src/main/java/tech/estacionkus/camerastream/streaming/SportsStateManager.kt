package tech.estacionkus.camerastream.streaming

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tech.estacionkus.camerastream.domain.model.SportState
import tech.estacionkus.camerastream.domain.model.SportType
import tech.estacionkus.camerastream.domain.model.TeamInfo
import javax.inject.Inject
import javax.inject.Singleton

// -------------------------------------------------------------------------
// Game event model
// -------------------------------------------------------------------------

enum class GameEventType {
    GOAL, FOUL, YELLOW_CARD, RED_CARD, TIMEOUT, SUBSTITUTION,
    PERIOD_START, PERIOD_END, PENALTY_AWARDED, PENALTY_SCORED, PENALTY_MISSED,
    TECHNICAL_FOUL, FLAGRANT_FOUL, NOTE
}

enum class EventTeam { A, B, NONE }

data class GameEvent(
    val id: Long = System.currentTimeMillis(),
    val type: GameEventType,
    val team: EventTeam = EventTeam.NONE,
    val period: String = "",
    val clockMs: Long = 0L,
    val description: String = "",
    val playerName: String = ""
)

data class GoalRecord(
    val team: EventTeam,
    val period: String,
    val clockMs: Long,
    val scorerName: String = ""
)

data class PenaltyRecord(
    val team: EventTeam,
    val period: String,
    val clockMs: Long,
    val shooterName: String = "",
    val scored: Boolean
)

data class PeriodRecord(
    val period: String,
    val startClockMs: Long,
    val endClockMs: Long? = null,
    val teamAScoreAtStart: Int = 0,
    val teamBScoreAtStart: Int = 0
)

@Singleton
class SportsStateManager @Inject constructor() {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // -------------------------------------------------------------------------
    // Public state flows
    // -------------------------------------------------------------------------

    private val _state = MutableStateFlow(SportState())
    val state: StateFlow<SportState> = _state.asStateFlow()

    /** Full chronological game event log */
    private val _eventLog = MutableStateFlow<List<GameEvent>>(emptyList())
    val eventLog: StateFlow<List<GameEvent>> = _eventLog.asStateFlow()

    /** Goal history with timestamps */
    private val _goalHistory = MutableStateFlow<List<GoalRecord>>(emptyList())
    val goalHistory: StateFlow<List<GoalRecord>> = _goalHistory.asStateFlow()

    /** Penalty history */
    private val _penaltyHistory = MutableStateFlow<List<PenaltyRecord>>(emptyList())
    val penaltyHistory: StateFlow<List<PenaltyRecord>> = _penaltyHistory.asStateFlow()

    /** Period transition log */
    private val _periodHistory = MutableStateFlow<List<PeriodRecord>>(emptyList())
    val periodHistory: StateFlow<List<PeriodRecord>> = _periodHistory.asStateFlow()

    /** Team A penalty count (for sports that track it) */
    private val _teamAPenalties = MutableStateFlow(0)
    val teamAPenalties: StateFlow<Int> = _teamAPenalties.asStateFlow()

    /** Team B penalty count */
    private val _teamBPenalties = MutableStateFlow(0)
    val teamBPenalties: StateFlow<Int> = _teamBPenalties.asStateFlow()

    // -------------------------------------------------------------------------
    // Internal
    // -------------------------------------------------------------------------

    private var clockJob: Job? = null

    // -------------------------------------------------------------------------
    // Sport / team configuration
    // -------------------------------------------------------------------------

    fun setSportType(type: SportType) {
        _state.value = _state.value.copy(
            sportType = type,
            period = type.periods.firstOrNull() ?: "1"
        )
    }

    fun setTeamA(name: String, color: Long = 0xFF1E88E5) {
        _state.value = _state.value.copy(teamA = _state.value.teamA.copy(name = name, color = color))
    }

    fun setTeamB(name: String, color: Long = 0xFFE53935) {
        _state.value = _state.value.copy(teamB = _state.value.teamB.copy(name = name, color = color))
    }

    // -------------------------------------------------------------------------
    // Score management
    // -------------------------------------------------------------------------

    fun incrementScoreA(scorerName: String = "") {
        _state.value = _state.value.copy(
            teamA = _state.value.teamA.copy(score = _state.value.teamA.score + 1)
        )
        val goal = GoalRecord(EventTeam.A, _state.value.period, _state.value.gameClockMs, scorerName)
        _goalHistory.value = _goalHistory.value + goal
        logEvent(GameEventType.GOAL, EventTeam.A, scorerName, "Goal: ${_state.value.teamA.name}")
    }

    fun decrementScoreA() {
        _state.value = _state.value.copy(
            teamA = _state.value.teamA.copy(score = (_state.value.teamA.score - 1).coerceAtLeast(0))
        )
        logEvent(GameEventType.NOTE, EventTeam.A, description = "Score correction: ${_state.value.teamA.name}")
    }

    fun incrementScoreB(scorerName: String = "") {
        _state.value = _state.value.copy(
            teamB = _state.value.teamB.copy(score = _state.value.teamB.score + 1)
        )
        val goal = GoalRecord(EventTeam.B, _state.value.period, _state.value.gameClockMs, scorerName)
        _goalHistory.value = _goalHistory.value + goal
        logEvent(GameEventType.GOAL, EventTeam.B, scorerName, "Goal: ${_state.value.teamB.name}")
    }

    fun decrementScoreB() {
        _state.value = _state.value.copy(
            teamB = _state.value.teamB.copy(score = (_state.value.teamB.score - 1).coerceAtLeast(0))
        )
        logEvent(GameEventType.NOTE, EventTeam.B, description = "Score correction: ${_state.value.teamB.name}")
    }

    // -------------------------------------------------------------------------
    // Period management
    // -------------------------------------------------------------------------

    fun setPeriod(period: String) {
        closePeriod(_state.value.period)
        _state.value = _state.value.copy(period = period)
        openPeriod(period)
    }

    fun nextPeriod() {
        val periods = _state.value.sportType.periods
        val currentIdx = periods.indexOf(_state.value.period)
        if (currentIdx < periods.size - 1) {
            val next = periods[currentIdx + 1]
            closePeriod(_state.value.period)
            _state.value = _state.value.copy(period = next)
            openPeriod(next)
        }
    }

    private fun openPeriod(period: String) {
        val record = PeriodRecord(
            period = period,
            startClockMs = _state.value.gameClockMs,
            teamAScoreAtStart = _state.value.teamA.score,
            teamBScoreAtStart = _state.value.teamB.score
        )
        _periodHistory.value = _periodHistory.value + record
        logEvent(GameEventType.PERIOD_START, description = "Period started: $period")
    }

    private fun closePeriod(period: String) {
        val updated = _periodHistory.value.toMutableList()
        val idx = updated.indexOfLast { it.period == period && it.endClockMs == null }
        if (idx >= 0) {
            updated[idx] = updated[idx].copy(endClockMs = _state.value.gameClockMs)
            _periodHistory.value = updated
        }
        if (period.isNotBlank()) {
            logEvent(GameEventType.PERIOD_END, description = "Period ended: $period")
        }
    }

    // -------------------------------------------------------------------------
    // Clock management
    // -------------------------------------------------------------------------

    fun startClock() {
        if (_state.value.isClockRunning) return
        _state.value = _state.value.copy(isClockRunning = true)
        clockJob = scope.launch {
            while (isActive) {
                delay(100)
                _state.value = _state.value.copy(gameClockMs = _state.value.gameClockMs + 100)
            }
        }
    }

    fun stopClock() {
        clockJob?.cancel()
        _state.value = _state.value.copy(isClockRunning = false)
    }

    fun resetClock() {
        clockJob?.cancel()
        _state.value = _state.value.copy(gameClockMs = 0, isClockRunning = false)
    }

    // -------------------------------------------------------------------------
    // Penalty tracking
    // -------------------------------------------------------------------------

    /** Record a foul/penalty event for the given team */
    fun recordFoul(team: EventTeam, playerName: String = "", type: GameEventType = GameEventType.FOUL) {
        when (team) {
            EventTeam.A -> _teamAPenalties.value++
            EventTeam.B -> _teamBPenalties.value++
            EventTeam.NONE -> {}
        }
        logEvent(type, team, playerName)
    }

    /** Record a penalty kick / shootout attempt */
    fun recordPenaltyAttempt(team: EventTeam, shooterName: String = "", scored: Boolean) {
        val record = PenaltyRecord(
            team = team,
            period = _state.value.period,
            clockMs = _state.value.gameClockMs,
            shooterName = shooterName,
            scored = scored
        )
        _penaltyHistory.value = _penaltyHistory.value + record

        val eventType = if (scored) GameEventType.PENALTY_SCORED else GameEventType.PENALTY_MISSED
        logEvent(eventType, team, shooterName,
            if (scored) "Penalty scored" else "Penalty missed")

        // If scored, also increment score
        if (scored) {
            when (team) {
                EventTeam.A -> _state.value = _state.value.copy(
                    teamA = _state.value.teamA.copy(score = _state.value.teamA.score + 1)
                )
                EventTeam.B -> _state.value = _state.value.copy(
                    teamB = _state.value.teamB.copy(score = _state.value.teamB.score + 1)
                )
                EventTeam.NONE -> {}
            }
        }
    }

    // -------------------------------------------------------------------------
    // Generic event logging
    // -------------------------------------------------------------------------

    /** Log a timeout called by a team */
    fun recordTimeout(team: EventTeam) {
        logEvent(GameEventType.TIMEOUT, team, description = "Timeout: ${teamName(team)}")
    }

    /** Log a substitution */
    fun recordSubstitution(team: EventTeam, playerOut: String = "", playerIn: String = "") {
        logEvent(GameEventType.SUBSTITUTION, team,
            description = "Sub: $playerOut -> $playerIn (${teamName(team)})")
    }

    /** Log a free-form note event */
    fun recordNote(description: String) {
        logEvent(GameEventType.NOTE, description = description)
    }

    // -------------------------------------------------------------------------
    // Convenience getters
    // -------------------------------------------------------------------------

    fun getScoreboardText(): String {
        val s = _state.value
        return "${s.teamA.name} ${s.teamA.score} - ${s.teamB.score} ${s.teamB.name}"
    }

    fun formatClock(): String {
        val totalSeconds = _state.value.gameClockMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    /** Returns goals scored in the current period only */
    fun goalsInCurrentPeriod(): List<GoalRecord> {
        val period = _state.value.period
        return _goalHistory.value.filter { it.period == period }
    }

    // -------------------------------------------------------------------------
    // Reset
    // -------------------------------------------------------------------------

    fun resetAll() {
        clockJob?.cancel()
        _state.value = SportState()
        _eventLog.value = emptyList()
        _goalHistory.value = emptyList()
        _penaltyHistory.value = emptyList()
        _periodHistory.value = emptyList()
        _teamAPenalties.value = 0
        _teamBPenalties.value = 0
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private fun logEvent(
        type: GameEventType,
        team: EventTeam = EventTeam.NONE,
        playerName: String = "",
        description: String = ""
    ) {
        val event = GameEvent(
            type = type,
            team = team,
            period = _state.value.period,
            clockMs = _state.value.gameClockMs,
            description = description,
            playerName = playerName
        )
        _eventLog.value = _eventLog.value + event
    }

    private fun teamName(team: EventTeam): String = when (team) {
        EventTeam.A -> _state.value.teamA.name
        EventTeam.B -> _state.value.teamB.name
        EventTeam.NONE -> ""
    }
}
