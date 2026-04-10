package tech.estacionkus.camerastream.streaming

import android.util.Log
import io.github.thibaultbee.srtdroid.core.Srt
import io.github.thibaultbee.srtdroid.core.enums.SockOpt
import io.github.thibaultbee.srtdroid.core.enums.Transtype
import io.github.thibaultbee.srtdroid.core.models.SrtSocket
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
 */
@Singleton
class SrtCallerManager @Inject constructor() {
    private val TAG = "SrtCallerManager"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _state = MutableStateFlow(StreamState.IDLE)
    val state: StateFlow<StreamState> = _state.asStateFlow()
    private val _rttMs = MutableStateFlow(0)
    val rttMs: StateFlow<Int> = _rttMs.asStateFlow()
    private val _bitrateKbps = MutableStateFlow(0)
    val bitrateKbps: StateFlow<Int> = _bitrateKbps.asStateFlow()
    private val _lostPct = MutableStateFlow(0f)
    val lostPct: StateFlow<Float> = _lostPct.asStateFlow()

    private var socket: SrtSocket? = null
    private var callJob: Job? = null
    var onTsData: ((ByteArray, Int) -> Unit)? = null

    data class SrtConfig(
        val host: String,
        val port: Int,
        val streamId: String = "",
        val latencyMs: Int = 200,      // auto-calculated RTT×4 if 0
        val maxBandwidthKbps: Int = 0, // 0 = unlimited
        val pbkeylen: Int = 0,         // 0=none, 16=AES-128, 32=AES-256
        val passphrase: String = ""
    )

    fun connect(config: SrtConfig) {
        if (_state.value == StreamState.LIVE) return
        callJob = scope.launch {
            try {
                Srt.startUp()
                socket = SrtSocket().apply {
                    setSockFlag(SockOpt.TRANSTYPE, Transtype.LIVE)
                    setSockFlag(SockOpt.RCVSYN, false)
                    if (config.latencyMs > 0) setSockFlag(SockOpt.LATENCY, config.latencyMs)
                    if (config.streamId.isNotBlank()) setSockFlag(SockOpt.STREAMID, config.streamId)
                    if (config.passphrase.isNotBlank()) {
                        setSockFlag(SockOpt.PBKEYLEN, config.pbkeylen.coerceAtLeast(16))
                        setSockFlag(SockOpt.PASSPHRASE, config.passphrase)
                    }
                    if (config.maxBandwidthKbps > 0) setSockFlag(SockOpt.MAXBW, config.maxBandwidthKbps * 1000L)
                }
                _state.value = StreamState.CONNECTING
                socket!!.connect(InetSocketAddress(config.host, config.port))
                _state.value = StreamState.LIVE
                Log.i(TAG, "SRT connected to ${config.host}:${config.port}")
                receiveLoop()
            } catch (e: Exception) {
                Log.e(TAG, "SRT error: ${e.message}")
                _state.value = StreamState.ERROR
            } finally {
                socket?.close()
                _state.value = StreamState.IDLE
                try { Srt.cleanUp() } catch (_: Exception) {}
            }
        }
    }

    private suspend fun receiveLoop() {
        val buf = ByteArray(1316) // MPEG-TS standard packet size
        var bytesTotal = 0L
        var pktTotal = 0L
        var lostTotal = 0L
        var lastMs = System.currentTimeMillis()
        while (callJob?.isActive == true) {
            val n = socket?.recv(buf) ?: break
            if (n <= 0) break
            bytesTotal += n
            pktTotal++
            onTsData?.invoke(buf, n)
            val now = System.currentTimeMillis()
            if (now - lastMs >= 1000) {
                _bitrateKbps.value = (bytesTotal * 8 / 1000).toInt()
                try {
                    // SRT stats — RTT and loss
                    val stats = socket?.bistats(clear = true)
                    stats?.let {
                        _rttMs.value = it.msRTT.toInt()
                        val lost = it.pktRcvLoss
                        if (pktTotal > 0) _lostPct.value = lost.toFloat() / pktTotal * 100f
                        lostTotal += lost
                    }
                } catch (_: Exception) {}
                bytesTotal = 0
                lastMs = now
            }
        }
    }

    fun disconnect() {
        callJob?.cancel()
        socket?.close()
        _state.value = StreamState.IDLE
        _rttMs.value = 0
        _bitrateKbps.value = 0
    }

    /** Auto-calculate recommended latency: RTT × 4 (Haivision standard for sports) */
    fun recommendedLatency(): Int = (_rttMs.value * 4).coerceAtLeast(120)
}
