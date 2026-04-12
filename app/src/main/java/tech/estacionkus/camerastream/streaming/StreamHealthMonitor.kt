package tech.estacionkus.camerastream.streaming

import android.os.Process
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

data class HealthSnapshot(
    val timestampMs: Long = System.currentTimeMillis(),
    val bitrateKbps: Int = 0,
    val fps: Int = 0,
    val droppedFrames: Int = 0,
    val rttMs: Int = 0,
    val cpuPercent: Float = 0f,
    val networkLatencyMs: Int = 0
)

enum class HealthGrade(val label: String, val color: Long) {
    EXCELLENT("Excellent", 0xFF4CAF50),
    GOOD("Good", 0xFF8BC34A),
    FAIR("Fair", 0xFFFF9800),
    POOR("Poor", 0xFFF44336)
}

data class StreamSummary(
    val totalDurationMs: Long = 0,
    val avgBitrateKbps: Int = 0,
    val maxBitrateKbps: Int = 0,
    val minBitrateKbps: Int = 0,
    val peakBitrateKbps: Int = 0,
    val avgFps: Int = 0,
    val maxFps: Int = 0,
    val minFps: Int = 0,
    val totalDroppedFrames: Int = 0,
    val avgRttMs: Int = 0,
    val maxRttMs: Int = 0,
    val minRttMs: Int = 0,
    val avgCpuPercent: Float = 0f,
    val peakCpuPercent: Float = 0f,
    val avgNetworkLatencyMs: Int = 0,
    val overallGrade: HealthGrade = HealthGrade.GOOD,
    val bitrateHistory: List<Int> = emptyList(),
    val fpsHistory: List<Int> = emptyList(),
    val dropHistory: List<Int> = emptyList(),
    val rttHistory: List<Int> = emptyList(),
    val cpuHistory: List<Float> = emptyList()
)

@Singleton
class StreamHealthMonitor @Inject constructor() {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // -------------------------------------------------------------------------
    // Public state flows
    // -------------------------------------------------------------------------

    private val _currentHealth = MutableStateFlow(HealthSnapshot())
    val currentHealth: StateFlow<HealthSnapshot> = _currentHealth.asStateFlow()

    private val _healthGrade = MutableStateFlow(HealthGrade.GOOD)
    val healthGrade: StateFlow<HealthGrade> = _healthGrade.asStateFlow()

    private val _bitrateHistory = MutableStateFlow<List<Int>>(emptyList())
    val bitrateHistory: StateFlow<List<Int>> = _bitrateHistory.asStateFlow()

    private val _fpsHistory = MutableStateFlow<List<Int>>(emptyList())
    val fpsHistory: StateFlow<List<Int>> = _fpsHistory.asStateFlow()

    private val _dropHistory = MutableStateFlow<List<Int>>(emptyList())
    val dropHistory: StateFlow<List<Int>> = _dropHistory.asStateFlow()

    private val _rttHistory = MutableStateFlow<List<Int>>(emptyList())
    val rttHistory: StateFlow<List<Int>> = _rttHistory.asStateFlow()

    private val _cpuHistory = MutableStateFlow<List<Float>>(emptyList())
    val cpuHistory: StateFlow<List<Float>> = _cpuHistory.asStateFlow()

    private val _summary = MutableStateFlow<StreamSummary?>(null)
    val summary: StateFlow<StreamSummary?> = _summary.asStateFlow()

    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()

    // Per-second sampling stats (peak/avg/min computed from history)
    private val _peakBitrateKbps = MutableStateFlow(0)
    val peakBitrateKbps: StateFlow<Int> = _peakBitrateKbps.asStateFlow()

    private val _avgBitrateKbps = MutableStateFlow(0)
    val avgBitrateKbps: StateFlow<Int> = _avgBitrateKbps.asStateFlow()

    private val _minBitrateKbps = MutableStateFlow(0)
    val minBitrateKbps: StateFlow<Int> = _minBitrateKbps.asStateFlow()

    // -------------------------------------------------------------------------
    // Internal state
    // -------------------------------------------------------------------------

    private var samplingJob: Job? = null
    private var startTimeMs = 0L
    private val snapshots = mutableListOf<HealthSnapshot>()
    private var targetBitrate = 2500
    private var targetFps = 30

    /** Tracks the last CPU stat read (user time in jiffies) */
    private var lastCpuUser = 0L
    private var lastCpuTotal = 0L
    private val MAX_HISTORY = 120  // 2 min at 1/sec

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    fun startMonitoring(targetBitrateKbps: Int = 2500, targetFpsVal: Int = 30) {
        targetBitrate = targetBitrateKbps
        targetFps = targetFpsVal
        startTimeMs = System.currentTimeMillis()
        snapshots.clear()
        _summary.value = null
        _bitrateHistory.value = emptyList()
        _fpsHistory.value = emptyList()
        _dropHistory.value = emptyList()
        _rttHistory.value = emptyList()
        _cpuHistory.value = emptyList()
        _peakBitrateKbps.value = 0
        _avgBitrateKbps.value = 0
        _minBitrateKbps.value = 0
        initCpuBaseline()
        startPerSecondSampling()
    }

    /**
     * Push a new stats snapshot. Can be called from the streaming engine callback.
     * Also called automatically once per second by the internal sampling job
     * (with whatever the last values were) to ensure per-second granularity.
     */
    fun reportStats(bitrateKbps: Int, fps: Int, droppedFrames: Int, rttMs: Int = 0, networkLatencyMs: Int = 0) {
        val cpu = estimateCpuPercent()
        val snapshot = HealthSnapshot(
            bitrateKbps = bitrateKbps,
            fps = fps,
            droppedFrames = droppedFrames,
            rttMs = rttMs,
            cpuPercent = cpu,
            networkLatencyMs = networkLatencyMs
        )
        ingestSnapshot(snapshot)
    }

    fun stopMonitoring(): StreamSummary {
        samplingJob?.cancel()
        samplingJob = null

        val duration = System.currentTimeMillis() - startTimeMs
        val bitrateVals = snapshots.map { it.bitrateKbps }
        val fpsVals = snapshots.map { it.fps }
        val totalDrops = snapshots.sumOf { it.droppedFrames }
        val rttVals = snapshots.map { it.rttMs }
        val cpuVals = snapshots.map { it.cpuPercent }
        val latencyVals = snapshots.map { it.networkLatencyMs }

        val result = StreamSummary(
            totalDurationMs = duration,
            avgBitrateKbps = bitrateVals.averageInt(),
            maxBitrateKbps = bitrateVals.maxOrNull() ?: 0,
            minBitrateKbps = bitrateVals.minOrNull() ?: 0,
            peakBitrateKbps = bitrateVals.maxOrNull() ?: 0,
            avgFps = fpsVals.averageInt(),
            maxFps = fpsVals.maxOrNull() ?: 0,
            minFps = fpsVals.minOrNull() ?: 0,
            totalDroppedFrames = totalDrops,
            avgRttMs = rttVals.averageInt(),
            maxRttMs = rttVals.maxOrNull() ?: 0,
            minRttMs = rttVals.minOrNull() ?: 0,
            avgCpuPercent = if (cpuVals.isEmpty()) 0f else cpuVals.average().toFloat(),
            peakCpuPercent = cpuVals.maxOrNull() ?: 0f,
            avgNetworkLatencyMs = latencyVals.averageInt(),
            overallGrade = _healthGrade.value,
            bitrateHistory = _bitrateHistory.value,
            fpsHistory = _fpsHistory.value,
            dropHistory = _dropHistory.value,
            rttHistory = _rttHistory.value,
            cpuHistory = _cpuHistory.value
        )
        _summary.value = result
        return result
    }

    fun reset() {
        samplingJob?.cancel()
        samplingJob = null
        snapshots.clear()
        _bitrateHistory.value = emptyList()
        _fpsHistory.value = emptyList()
        _dropHistory.value = emptyList()
        _rttHistory.value = emptyList()
        _cpuHistory.value = emptyList()
        _currentHealth.value = HealthSnapshot()
        _healthGrade.value = HealthGrade.GOOD
        _suggestions.value = emptyList()
        _summary.value = null
        _peakBitrateKbps.value = 0
        _avgBitrateKbps.value = 0
        _minBitrateKbps.value = 0
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /** Start a coroutine that samples CPU and republishes the last snapshot every second */
    private fun startPerSecondSampling() {
        samplingJob?.cancel()
        samplingJob = scope.launch {
            while (isActive) {
                delay(1000)
                val last = _currentHealth.value
                if (startTimeMs > 0) {
                    // Re-sample CPU; keep last known bitrate/fps/rtt
                    val cpu = estimateCpuPercent()
                    val snapshot = last.copy(
                        timestampMs = System.currentTimeMillis(),
                        cpuPercent = cpu
                    )
                    ingestSnapshot(snapshot)
                }
            }
        }
    }

    private fun ingestSnapshot(snapshot: HealthSnapshot) {
        _currentHealth.value = snapshot
        snapshots.add(snapshot)

        // Rolling histories (keep last MAX_HISTORY points)
        val newBitrateHistory = (_bitrateHistory.value + snapshot.bitrateKbps).takeLast(MAX_HISTORY)
        val newFpsHistory = (_fpsHistory.value + snapshot.fps).takeLast(MAX_HISTORY)
        val newDropHistory = (_dropHistory.value + snapshot.droppedFrames).takeLast(MAX_HISTORY)
        val newRttHistory = (_rttHistory.value + snapshot.rttMs).takeLast(MAX_HISTORY)
        val newCpuHistory = (_cpuHistory.value + snapshot.cpuPercent).takeLast(MAX_HISTORY)

        _bitrateHistory.value = newBitrateHistory
        _fpsHistory.value = newFpsHistory
        _dropHistory.value = newDropHistory
        _rttHistory.value = newRttHistory
        _cpuHistory.value = newCpuHistory

        // Running aggregate bitrate stats
        if (newBitrateHistory.isNotEmpty()) {
            _peakBitrateKbps.value = newBitrateHistory.max()
            _avgBitrateKbps.value = newBitrateHistory.averageInt()
            _minBitrateKbps.value = newBitrateHistory.min()
        }

        _healthGrade.value = calculateGrade(snapshot.bitrateKbps, snapshot.fps, snapshot.droppedFrames, snapshot.rttMs, snapshot.cpuPercent)
        _suggestions.value = generateSuggestions(snapshot.bitrateKbps, snapshot.fps, snapshot.droppedFrames, snapshot.rttMs, snapshot.cpuPercent, snapshot.networkLatencyMs)
    }

    private fun calculateGrade(bitrate: Int, fps: Int, drops: Int, rtt: Int, cpu: Float): HealthGrade {
        val bitrateRatio = if (targetBitrate > 0) bitrate.toFloat() / targetBitrate else 1f
        val fpsRatio = if (targetFps > 0) fps.toFloat() / targetFps else 1f
        val dropPenalty = (20f - drops.coerceAtMost(20)).coerceAtLeast(0f)
        val rttPenalty = when {
            rtt <= 0 -> 0f
            rtt < 100 -> 0f
            rtt < 300 -> 5f
            rtt < 600 -> 10f
            else -> 15f
        }
        val cpuPenalty = when {
            cpu < 60f -> 0f
            cpu < 80f -> 5f
            else -> 10f
        }
        val score = (bitrateRatio * 40 + fpsRatio * 40 + dropPenalty - rttPenalty - cpuPenalty)
            .coerceIn(0f, 100f)
        return when {
            score >= 90 -> HealthGrade.EXCELLENT
            score >= 70 -> HealthGrade.GOOD
            score >= 50 -> HealthGrade.FAIR
            else -> HealthGrade.POOR
        }
    }

    private fun generateSuggestions(
        bitrate: Int, fps: Int, drops: Int, rtt: Int, cpu: Float, latency: Int
    ): List<String> {
        val list = mutableListOf<String>()

        // Bitrate suggestions
        when {
            bitrate < targetBitrate * 0.3 ->
                list.add("Bitrate is critically low (${bitrate}kbps vs target ${targetBitrate}kbps). Switch to WiFi or reduce resolution to 480p.")
            bitrate < targetBitrate * 0.5 ->
                list.add("Bitrate is very low. Try moving to better WiFi or reduce resolution.")
            bitrate < targetBitrate * 0.75 ->
                list.add("Bitrate below target (${bitrate}kbps). Consider reducing to ${targetBitrate / 2}kbps for stability.")
        }

        // FPS suggestions
        when {
            fps < targetFps * 0.5 ->
                list.add("FPS critically low (${fps}fps). Reduce resolution or disable effects to recover performance.")
            fps < targetFps * 0.7 ->
                list.add("FPS dropping (${fps}fps). Lower resolution or bitrate to reduce CPU load.")
        }

        // Drop suggestions
        when {
            drops > 20 ->
                list.add("Severe frame drops ($drops). Network is unstable — consider a backup connection.")
            drops > 5 ->
                list.add("Frame drops detected ($drops). Check network stability or reduce bitrate.")
        }

        // RTT / latency suggestions
        when {
            rtt > 1000 ->
                list.add("Extremely high latency (${rtt}ms). Ingest server may be unreachable.")
            rtt > 500 ->
                list.add("High latency (${rtt}ms). Consider a closer ingest server or check firewall rules.")
            rtt > 200 ->
                list.add("Elevated RTT (${rtt}ms). Monitor for further degradation.")
        }

        if (latency > 300) {
            list.add("Network latency is high (${latency}ms). Viewer experience may be impacted.")
        }

        // CPU suggestions
        when {
            cpu > 90f ->
                list.add("CPU usage critical (${cpu.roundToInt()}%). Lower resolution or close background apps immediately.")
            cpu > 75f ->
                list.add("CPU usage high (${cpu.roundToInt()}%). Consider reducing encode settings.")
        }

        return list
    }

    // -------------------------------------------------------------------------
    // CPU estimation via /proc/stat
    // -------------------------------------------------------------------------

    private fun initCpuBaseline() {
        readProcStat()?.let { (user, total) ->
            lastCpuUser = user
            lastCpuTotal = total
        }
    }

    private fun estimateCpuPercent(): Float {
        val stat = readProcStat() ?: return 0f
        val (user, total) = stat
        val diffUser = user - lastCpuUser
        val diffTotal = total - lastCpuTotal
        lastCpuUser = user
        lastCpuTotal = total
        if (diffTotal <= 0) return 0f
        return (diffUser.toFloat() / diffTotal * 100f).coerceIn(0f, 100f)
    }

    /** Reads the first line of /proc/stat and returns (activeJiffies, totalJiffies) */
    private fun readProcStat(): Pair<Long, Long>? {
        return try {
            val line = java.io.FileReader("/proc/stat").use { it.readText() }.lines().firstOrNull() ?: return null
            val parts = line.trim().split("\\s+".toRegex()).drop(1).map { it.toLong() }
            if (parts.size < 4) return null
            val idle = parts[3] + parts.getOrElse(4) { 0L }  // idle + iowait
            val total = parts.sum()
            val active = total - idle
            active to total
        } catch (_: Exception) {
            null
        }
    }

    // -------------------------------------------------------------------------
    // Extension helpers
    // -------------------------------------------------------------------------

    private fun List<Int>.averageInt(): Int = if (isEmpty()) 0 else average().roundToInt()
}
