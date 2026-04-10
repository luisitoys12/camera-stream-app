package tech.estacionkus.camerastream.streaming

import android.util.Log
import cn.nodemedia.NodePublisher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

// SRT Caller mode: app connects TO a remote SRT server (OBS, Nimble, etc.)
@Singleton
class SrtCallerManager @Inject constructor() {
    private val TAG = "SrtCallerManager"

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // NodeMedia supports SRT natively via srt:// scheme
    // Format: srt://host:port?streamid=live/xxx&latency=200
    fun buildSrtUrl(host: String, port: Int, streamId: String? = null, latencyMs: Int = 200): String {
        val base = "srt://$host:$port?latency=$latencyMs&mode=caller"
        return if (streamId != null) "$base&streamid=$streamId" else base
    }

    // The NodePublisher accepts srt:// URLs directly — no extra library needed
    fun getSrtUrl(host: String, port: Int, streamId: String? = null) =
        buildSrtUrl(host, port, streamId)
}
