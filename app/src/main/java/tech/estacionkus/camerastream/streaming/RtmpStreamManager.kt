package tech.estacionkus.camerastream.streaming

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import cn.nodemedia.NodePublisher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class StreamStats(val droppedFrames: Int = 0, val bitrateKbps: Int = 0, val fps: Int = 0)

@Singleton
class RtmpStreamManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "RtmpStreamManager"
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _state = MutableStateFlow(StreamState.IDLE)
    val state: StateFlow<StreamState> = _state.asStateFlow()
    val stats = MutableStateFlow(StreamStats())

    private var publisher: NodePublisher? = null
    private var bitrateKbps = 2500
    private var currentUrl: String? = null
    private var reconnectJob: Job? = null
    private var reconnectAttempts = 0
    private val MAX_RECONNECT_ATTEMPTS = 5
    private var lastWidth = 1280
    private var lastHeight = 720
    private var lastFps = 30

    fun configure(width: Int, height: Int, fps: Int, bitrate: Int) {
        bitrateKbps = bitrate
        lastWidth = width
        lastHeight = height
        lastFps = fps
        release()
        publisher = NodePublisher(context, "").apply {
            setHWAccelEnable(true)
            // Video config: H264 Main profile
            setVideoCodecParam(
                NodePublisher.NMC_CODEC_ID_H264,
                NodePublisher.NMC_PROFILE_H264_MAIN,
                width, height, fps, bitrate * 1000
            )
            // Audio config: AAC-LC, 44.1kHz, stereo, 128kbps
            setAudioCodecParam(
                NodePublisher.NMC_CODEC_ID_AAC,
                NodePublisher.NMC_PROFILE_AAC_LC,
                44100, 2, 128000
            )
            // Event listener for connection status
            setOnNodePublisherEventListener { _, event, msg ->
                Log.d(TAG, "Event: $event msg: $msg")
                handlePublisherEvent(event, msg)
            }
        }
        Log.d(TAG, "Configured: ${width}x${height} @ ${fps}fps, ${bitrate}kbps")
    }

    private fun handlePublisherEvent(event: Int, msg: String) {
        when (event) {
            2000 -> {
                // Connected successfully
                _state.value = StreamState.LIVE
                reconnectAttempts = 0
                Log.d(TAG, "RTMP connected and streaming")
            }
            2001 -> {
                // Disconnected
                Log.w(TAG, "RTMP disconnected: $msg")
                if (_state.value == StreamState.LIVE || _state.value == StreamState.RECONNECTING) {
                    attemptReconnect()
                } else {
                    _state.value = StreamState.IDLE
                }
            }
            2002 -> {
                // Connection error
                Log.e(TAG, "RTMP error: $msg")
                if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS && currentUrl != null) {
                    attemptReconnect()
                } else {
                    _state.value = StreamState.ERROR
                }
            }
            2100 -> {
                // Video/audio stats available — parse bitrate info
                val bitrateMatch = Regex("(\\d+)").find(msg)
                val kbps = bitrateMatch?.value?.toIntOrNull()
                if (kbps != null && kbps > 0) {
                    stats.value = stats.value.copy(bitrateKbps = kbps)
                }
            }
        }
    }

    private fun attemptReconnect() {
        if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            _state.value = StreamState.ERROR
            Log.e(TAG, "Max reconnect attempts reached")
            return
        }

        reconnectAttempts++
        _state.value = StreamState.RECONNECTING

        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            // Exponential backoff: 2s, 4s, 8s, 16s, 32s
            val delayMs = (2000L * (1 shl (reconnectAttempts - 1))).coerceAtMost(32000L)
            Log.d(TAG, "Reconnecting in ${delayMs}ms (attempt $reconnectAttempts/$MAX_RECONNECT_ATTEMPTS)")
            delay(delayMs)

            val url = currentUrl ?: return@launch
            try {
                publisher?.stop()
                delay(500)
                publisher?.start(url)
                Log.d(TAG, "Reconnect attempt $reconnectAttempts sent")
            } catch (e: Exception) {
                Log.e(TAG, "Reconnect failed: ${e.message}")
                if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
                    _state.value = StreamState.ERROR
                }
            }
        }
    }

    fun startStream(url: String, key: String) {
        val fullUrl = if (key.isNotBlank()) {
            "${url.trimEnd('/')}/$key"
        } else {
            url
        }

        // Validate URL format
        if (!fullUrl.startsWith("rtmp://") && !fullUrl.startsWith("rtmps://")) {
            Log.e(TAG, "Invalid RTMP URL: $fullUrl")
            _state.value = StreamState.ERROR
            return
        }

        currentUrl = fullUrl
        reconnectAttempts = 0
        _state.value = StreamState.CONNECTING

        if (publisher == null) {
            configure(lastWidth, lastHeight, lastFps, bitrateKbps)
        }

        try {
            // Open camera and start preview before streaming
            publisher?.openCamera(false)
            publisher?.start(fullUrl)
            Log.d(TAG, "Stream starting: $fullUrl")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start stream: ${e.message}")
            _state.value = StreamState.ERROR
        }
    }

    fun stopStream() {
        reconnectJob?.cancel()
        reconnectJob = null
        reconnectAttempts = 0
        try {
            publisher?.stop()
        } catch (_: Exception) {}
        _state.value = StreamState.IDLE
        currentUrl = null
        stats.value = StreamStats()
    }

    fun setBitrate(kbps: Int) {
        bitrateKbps = kbps
        // Reconfigure if currently streaming — NodePublisher needs restart for bitrate change
        if (_state.value == StreamState.LIVE && currentUrl != null) {
            val url = currentUrl!!
            scope.launch {
                publisher?.stop()
                delay(200)
                configure(lastWidth, lastHeight, lastFps, kbps)
                publisher?.openCamera(false)
                publisher?.start(url)
            }
        }
    }

    fun attachView(viewGroup: ViewGroup) {
        publisher?.attachView(viewGroup)
    }

    fun detachView() {
        publisher?.detachView()
    }

    fun openCamera(front: Boolean = false) {
        publisher?.openCamera(front)
    }

    fun closeCamera() {
        publisher?.closeCamera()
    }

    fun flipCamera() { publisher?.switchCamera() }

    fun setMute(muted: Boolean) {
        publisher?.setVolume(if (muted) 0f else 1f)
    }

    fun getPublisher(): NodePublisher? = publisher

    fun release() {
        reconnectJob?.cancel()
        try { publisher?.stop() } catch (_: Exception) {}
        try { publisher?.closeCamera() } catch (_: Exception) {}
        try { publisher?.detachView() } catch (_: Exception) {}
        publisher = null
    }

    fun isConfigured(): Boolean = publisher != null

    companion object {
        /** Bitrate presets in kbps */
        const val BITRATE_LOW = 1000
        const val BITRATE_MEDIUM = 2500
        const val BITRATE_HIGH = 4000
        const val BITRATE_ULTRA = 8000
    }
}
