package tech.estacionkus.camerastream.streaming

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.InetSocketAddress
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SRT Caller mode — connects to an external SRT listener (sports encoder, OBS server, etc.)
 * Optimized for sports/broadcast:
 *   - latency = RTT x 4 (recommended by Haivision for live events)
 *   - CBR mode for stable bitrate on mobile networks
 *   - MPEG-TS payload
 *
 * NOTE: SrtSocketWrapper isolates JNI/Android deps so core logic is unit-testable on JVM.
 */
@Singleton
class SrtCallerManager @Inject constructor(
    private val socketWrapper: SrtSocketWrapper = SrtSocketWrapper.real()
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _state = MutableStateFlow(StreamState.IDLE)
    val state: StateFlow<StreamState> = _state.asStateFlow()

    private val _rttMs = MutableStateFlow(0)
    val rttMs: StateFlow<Int> = _rttMs.asStateFlow()

    private val _bitrateKbps = MutableStateFlow(0)
    val bitrateKbps: StateFlow<Int> = _bitrateKbps.asStateFlow()

    private val _lostPct = MutableStateFlow(0f)
    val lostPct: StateFlow<Float> = _lostPct.asStateFlow()

    private var callJob: Job? = null
    var onTsData: ((ByteArray, Int) -> Unit)? = null

    data class SrtConfig(
        val host: String,
        val port: Int,
        val streamId: String = "",
        val latencyMs: Int = 200,
        val maxBandwidthKbps: Int = 0,
        val pbkeylen: Int = 0,
        val passphrase: String = ""
    )

    fun connect(config: SrtConfig) {
        if (_state.value == StreamState.LIVE) return
        callJob = scope.launch {
            try {
                socketWrapper.startup()
                socketWrapper.configure(config)
                _state.value = StreamState.CONNECTING
                socketWrapper.connect(InetSocketAddress(config.host, config.port))
                _state.value = StreamState.LIVE
                receiveLoop()
            } catch (e: Exception) {
                socketWrapper.log("SRT error: ${e.message}")
                _state.value = StreamState.ERROR
            } finally {
                socketWrapper.close()
                _state.value = StreamState.IDLE
                socketWrapper.cleanup()
            }
        }
    }

    private suspend fun receiveLoop() {
        val buf = ByteArray(1316)
        var bytesTotal = 0L
        var pktTotal = 0L
        var lastMs = System.currentTimeMillis()
        while (callJob?.isActive == true) {
            val n = socketWrapper.recv(buf)
            if (n <= 0) break
            bytesTotal += n
            pktTotal++
            onTsData?.invoke(buf, n)
            val now = System.currentTimeMillis()
            if (now - lastMs >= 1000) {
                _bitrateKbps.value = (bytesTotal * 8 / 1000).toInt()
                socketWrapper.readStats()?.let { stats ->
                    _rttMs.value = stats.rttMs
                    if (pktTotal > 0) _lostPct.value = stats.lostPkts.toFloat() / pktTotal * 100f
                }
                bytesTotal = 0
                lastMs = now
            }
        }
    }

    fun disconnect() {
        callJob?.cancel()
        socketWrapper.close()
        _state.value = StreamState.IDLE
        _rttMs.value = 0
        _bitrateKbps.value = 0
    }

    /** Auto-calculate recommended latency: RTT × 4 (Haivision standard for sports) */
    fun recommendedLatency(): Int = (_rttMs.value * 4).coerceAtLeast(120)
}
