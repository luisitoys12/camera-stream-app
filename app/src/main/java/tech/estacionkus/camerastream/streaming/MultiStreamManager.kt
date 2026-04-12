package tech.estacionkus.camerastream.streaming

import android.content.Context
import android.util.Log
import cn.nodemedia.NodePublisher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tech.estacionkus.camerastream.domain.model.StreamTarget
import javax.inject.Inject
import javax.inject.Singleton

/** Per-target health derived from recent event codes */
enum class TargetHealth { GOOD, FAIR, POOR }

/** Runtime status for a single stream target */
data class TargetStatus(
    val state: StreamState = StreamState.IDLE,
    val health: TargetHealth = TargetHealth.GOOD,
    val bitrateKbps: Int = 0,
    val reconnectAttempts: Int = 0,
    val lastEventCode: Int = -1,
    val errorCount: Int = 0
)

@Singleton
class MultiStreamManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "MultiStreamManager"
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val MAX_RECONNECT_ATTEMPTS = 3
    private val RECONNECT_DELAY_MS = 3_000L

    private val publishers = mutableMapOf<String, NodePublisher>()
    private val reconnectJobs = mutableMapOf<String, Job>()
    private val targetConfigs = mutableMapOf<String, StreamTarget>()
    private var lastBitrateKbps = 2500
    private var lastFps = 30
    private var lastWidth = 1280
    private var lastHeight = 720

    /** Combined per-target state map (state + health + bitrate) */
    private val _statuses = MutableStateFlow<Map<String, TargetStatus>>(emptyMap())
    val statuses: StateFlow<Map<String, TargetStatus>> = _statuses.asStateFlow()

    /** Legacy compatibility: map of target id -> StreamState only */
    private val _states = MutableStateFlow<Map<String, StreamState>>(emptyMap())
    val states: StateFlow<Map<String, StreamState>> = _states.asStateFlow()

    /** Per-target bitrate tracking */
    private val _bitrateMap = MutableStateFlow<Map<String, Int>>(emptyMap())
    val bitrateMap: StateFlow<Map<String, Int>> = _bitrateMap.asStateFlow()

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    fun startAll(
        targets: List<StreamTarget>,
        bitrateKbps: Int,
        fps: Int,
        width: Int,
        height: Int
    ) {
        lastBitrateKbps = bitrateKbps
        lastFps = fps
        lastWidth = width
        lastHeight = height
        stopAll()
        targets.filter { it.isEnabled }.forEach { target ->
            targetConfigs[target.id] = target
            createAndStartPublisher(target, bitrateKbps, fps, width, height, reconnectAttempts = 0)
        }
    }

    /**
     * Add a single new target to an already-running multi-stream session.
     * Starts publishing immediately with the last-used video parameters.
     */
    fun startTarget(target: StreamTarget) {
        if (publishers.containsKey(target.id)) {
            Log.w(TAG, "Target ${target.id} already active, ignoring startTarget")
            return
        }
        targetConfigs[target.id] = target
        createAndStartPublisher(
            target,
            lastBitrateKbps, lastFps, lastWidth, lastHeight,
            reconnectAttempts = 0
        )
    }

    /**
     * Remove a single target from the active multi-stream session and stop it.
     */
    fun stopTarget(targetId: String) {
        reconnectJobs.remove(targetId)?.cancel()
        publishers.remove(targetId)?.let {
            try { it.stop() } catch (_: Exception) {}
        }
        targetConfigs.remove(targetId)
        _statuses.value = _statuses.value - targetId
        _states.value = _states.value - targetId
        _bitrateMap.value = _bitrateMap.value - targetId
    }

    fun stopAll() {
        reconnectJobs.values.forEach { it.cancel() }
        reconnectJobs.clear()
        publishers.values.forEach { try { it.stop() } catch (_: Exception) {} }
        publishers.clear()
        targetConfigs.clear()
        _statuses.value = emptyMap()
        _states.value = emptyMap()
        _bitrateMap.value = emptyMap()
    }

    fun flipCamera() {
        publishers.values.firstOrNull()?.switchCamera()
    }

    fun setMute(muted: Boolean) {
        publishers.values.forEach { it.setVolume(if (muted) 0f else 1f) }
    }

    /** Update bitrate for all active publishers on-the-fly (if supported) */
    fun updateBitrate(bitrateKbps: Int) {
        lastBitrateKbps = bitrateKbps
        _bitrateMap.value = _bitrateMap.value.mapValues { bitrateKbps }
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private fun createAndStartPublisher(
        target: StreamTarget,
        bitrateKbps: Int,
        fps: Int,
        width: Int,
        height: Int,
        reconnectAttempts: Int
    ) {
        val pub = NodePublisher(context, "").apply {
            setHWAccelEnable(true)
            setVideoCodecParam(
                NodePublisher.NMC_CODEC_ID_H264,
                NodePublisher.NMC_PROFILE_H264_MAIN,
                width, height, fps, bitrateKbps * 1000
            )
            setAudioCodecParam(
                NodePublisher.NMC_CODEC_ID_AAC,
                NodePublisher.NMC_PROFILE_AAC_LC,
                44100, 2, 64000
            )
            setOnNodePublisherEventListener { _, event, msg ->
                handleEvent(target.id, event, msg, bitrateKbps, fps, width, height, reconnectAttempts)
            }
        }
        pub.start(target.fullUrl)
        publishers[target.id] = pub
        updateStatus(target.id) { copy(state = StreamState.CONNECTING, reconnectAttempts = reconnectAttempts) }
    }

    private fun handleEvent(
        targetId: String,
        event: Int,
        msg: String,
        bitrateKbps: Int,
        fps: Int,
        width: Int,
        height: Int,
        reconnectAttempts: Int
    ) {
        Log.d(TAG, "Target $targetId event=$event msg=$msg attempts=$reconnectAttempts")

        val newState = when (event) {
            2000 -> StreamState.LIVE
            2001 -> StreamState.IDLE
            2002 -> StreamState.ERROR
            else -> _statuses.value[targetId]?.state ?: StreamState.IDLE
        }

        val health = deriveHealth(event, _statuses.value[targetId])
        val bitrate = parseBitrateFromMsg(msg, _bitrateMap.value[targetId] ?: bitrateKbps)

        updateStatus(targetId) {
            copy(
                state = newState,
                health = health,
                bitrateKbps = bitrate,
                lastEventCode = event,
                errorCount = if (event == 2002) errorCount + 1 else errorCount
            )
        }

        _bitrateMap.value = _bitrateMap.value + (targetId to bitrate)
        _states.value = _states.value + (targetId to newState)

        // Auto-reconnect on disconnect/error
        if ((event == 2001 || event == 2002) && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
            val target = targetConfigs[targetId] ?: return
            scheduleReconnect(target, bitrateKbps, fps, width, height, reconnectAttempts + 1)
        }
    }

    private fun scheduleReconnect(
        target: StreamTarget,
        bitrateKbps: Int,
        fps: Int,
        width: Int,
        height: Int,
        attempt: Int
    ) {
        reconnectJobs.remove(target.id)?.cancel()
        updateStatus(target.id) { copy(state = StreamState.RECONNECTING, reconnectAttempts = attempt) }

        reconnectJobs[target.id] = scope.launch {
            val backoffMs = RECONNECT_DELAY_MS * attempt
            Log.d(TAG, "Reconnecting target ${target.id} in ${backoffMs}ms (attempt $attempt)")
            delay(backoffMs)

            // Remove old publisher before creating a new one
            publishers.remove(target.id)?.let {
                try { it.stop() } catch (_: Exception) {}
            }

            createAndStartPublisher(target, bitrateKbps, fps, width, height, attempt)
        }
    }

    private fun deriveHealth(event: Int, current: TargetStatus?): TargetHealth {
        val errors = (current?.errorCount ?: 0) + (if (event == 2002) 1 else 0)
        return when {
            errors == 0 && event != 2002 -> TargetHealth.GOOD
            errors <= 2 -> TargetHealth.FAIR
            else -> TargetHealth.POOR
        }
    }

    /** Attempt to extract bitrate (kbps) from NodeMedia event message, fall back to last known value */
    private fun parseBitrateFromMsg(msg: String, fallback: Int): Int {
        // NodeMedia sometimes includes "kbps:1234" or similar in messages
        val match = Regex("(?:kbps|bitrate)[=:]\\s*(\\d+)", RegexOption.IGNORE_CASE)
            .find(msg)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: fallback
    }

    private fun updateStatus(targetId: String, transform: TargetStatus.() -> TargetStatus) {
        val current = _statuses.value[targetId] ?: TargetStatus()
        _statuses.value = _statuses.value + (targetId to current.transform())
    }
}
