package tech.estacionkus.camerastream.ui.screens.radio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.NoiseSuppressor
import android.media.audiofx.AutomaticGainControl
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.nodemedia.NodePublisher
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.estacionkus.camerastream.billing.PlanTier
import tech.estacionkus.camerastream.billing.StripeManager
import tech.estacionkus.camerastream.streaming.SrtCallerManager
import tech.estacionkus.camerastream.streaming.StreamState
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.sqrt

@HiltViewModel
class RadioBroadcastViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val stripeManager: StripeManager,
    private val srtCallerManager: SrtCallerManager
) : ViewModel() {

    private val TAG = "RadioBroadcastVM"

    val isAgency: StateFlow<Boolean> = stripeManager.currentPlan.map {
        it == PlanTier.AGENCY
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _uiState = MutableStateFlow(RadioUiState())
    val uiState: StateFlow<RadioUiState> = _uiState.asStateFlow()

    private var publisher: NodePublisher? = null
    private var audioRecord: AudioRecord? = null
    private var audioLevelJob: Job? = null
    private var noiseSuppressor: NoiseSuppressor? = null
    private var gainControl: AutomaticGainControl? = null

    fun toggleBroadcast() {
        val current = _uiState.value
        if (current.isLive) {
            stopBroadcast()
        } else {
            startBroadcast()
        }
    }

    private fun startBroadcast() {
        val state = _uiState.value
        if (state.rtmpUrl.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Configure RTMP URL first")
            return
        }

        when (state.protocol) {
            RadioProtocol.RTMP -> startRtmpBroadcast()
            RadioProtocol.SRT -> startSrtBroadcast()
        }
    }

    private fun startRtmpBroadcast() {
        val state = _uiState.value
        try {
            val fullUrl = if (state.streamKey.isNotBlank()) {
                "${state.rtmpUrl.trimEnd('/')}/${state.streamKey}"
            } else {
                state.rtmpUrl
            }

            publisher = NodePublisher(context, "").apply {
                setHWAccelEnable(true)
                // Audio-only: configure audio codec, no video
                setAudioCodecParam(
                    NodePublisher.NMC_CODEC_ID_AAC,
                    NodePublisher.NMC_PROFILE_AAC_LC,
                    state.sampleRate,
                    if (state.stereo) 2 else 1,
                    state.audioBitrateKbps * 1000
                )
                // Minimal video to satisfy RTMP (1x1 black frame at 1fps)
                setVideoCodecParam(
                    NodePublisher.NMC_CODEC_ID_H264,
                    NodePublisher.NMC_PROFILE_H264_BASELINE,
                    2, 2, 1, 10000
                )
                setOnNodePublisherEventListener { _, event, msg ->
                    Log.d(TAG, "RTMP Event: $event msg: $msg")
                    when (event) {
                        2000 -> {
                            _uiState.value = _uiState.value.copy(isLive = true, connectionState = StreamState.LIVE)
                        }
                        2001 -> {
                            _uiState.value = _uiState.value.copy(isLive = false, connectionState = StreamState.IDLE)
                        }
                        2002 -> {
                            _uiState.value = _uiState.value.copy(
                                connectionState = StreamState.ERROR,
                                errorMessage = "RTMP connection error: $msg"
                            )
                        }
                    }
                }
            }

            _uiState.value = _uiState.value.copy(
                isLive = true,
                connectionState = StreamState.CONNECTING,
                errorMessage = null
            )
            publisher?.start(fullUrl)
            startRealAudioLevelMonitoring()
            Log.d(TAG, "Radio RTMP broadcast started: $fullUrl")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start RTMP broadcast: ${e.message}")
            _uiState.value = _uiState.value.copy(
                isLive = false,
                connectionState = StreamState.ERROR,
                errorMessage = "Failed to start: ${e.message}"
            )
        }
    }

    private fun startSrtBroadcast() {
        val state = _uiState.value
        try {
            // Parse SRT URL: srt://host:port?streamid=X&latency=Y&passphrase=Z
            val srtUrl = state.rtmpUrl.trim()
            val parsed = parseSrtUrl(srtUrl)
            if (parsed == null) {
                _uiState.value = state.copy(errorMessage = "Invalid SRT URL. Format: srt://host:port?streamid=X&latency=Y")
                return
            }

            _uiState.value = state.copy(
                isLive = true,
                connectionState = StreamState.CONNECTING,
                errorMessage = null
            )

            srtCallerManager.connect(parsed)
            startRealAudioLevelMonitoring()

            // Monitor SRT state
            viewModelScope.launch {
                srtCallerManager.state.collect { srtState ->
                    _uiState.value = _uiState.value.copy(
                        connectionState = srtState,
                        isLive = srtState == StreamState.LIVE || srtState == StreamState.CONNECTING
                    )
                    if (srtState == StreamState.ERROR || srtState == StreamState.IDLE) {
                        if (_uiState.value.isLive) stopBroadcast()
                    }
                }
            }

            Log.d(TAG, "Radio SRT broadcast started: $srtUrl")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start SRT broadcast: ${e.message}")
            _uiState.value = _uiState.value.copy(
                isLive = false,
                connectionState = StreamState.ERROR,
                errorMessage = "SRT error: ${e.message}"
            )
        }
    }

    private fun parseSrtUrl(url: String): SrtCallerManager.SrtConfig? {
        try {
            val cleaned = url.replace("srt://", "")
            val parts = cleaned.split("?", limit = 2)
            val hostPort = parts[0].split(":")
            if (hostPort.size < 2) return null

            val host = hostPort[0]
            val port = hostPort[1].toIntOrNull() ?: return null

            var streamId = ""
            var latency = 200
            var passphrase = ""

            if (parts.size > 1) {
                val params = parts[1].split("&")
                for (param in params) {
                    val kv = param.split("=", limit = 2)
                    if (kv.size == 2) {
                        when (kv[0].lowercase()) {
                            "streamid" -> streamId = kv[1]
                            "latency" -> latency = kv[1].toIntOrNull() ?: 200
                            "passphrase" -> passphrase = kv[1]
                        }
                    }
                }
            }

            return SrtCallerManager.SrtConfig(
                host = host,
                port = port,
                streamId = streamId,
                latencyMs = latency,
                passphrase = passphrase
            )
        } catch (e: Exception) {
            return null
        }
    }

    private fun stopBroadcast() {
        audioLevelJob?.cancel()
        audioLevelJob = null

        // Stop audio effects
        try { noiseSuppressor?.release() } catch (_: Exception) {}
        try { gainControl?.release() } catch (_: Exception) {}
        noiseSuppressor = null
        gainControl = null

        // Stop audio capture
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null

        // Stop RTMP
        try { publisher?.stop() } catch (_: Exception) {}
        publisher = null

        // Stop SRT
        if (_uiState.value.protocol == RadioProtocol.SRT) {
            srtCallerManager.disconnect()
        }

        _uiState.value = _uiState.value.copy(
            isLive = false,
            connectionState = StreamState.IDLE,
            audioLevelLeft = 0f,
            audioLevelRight = 0f
        )
        Log.d(TAG, "Radio broadcast stopped")
    }

    private fun startRealAudioLevelMonitoring() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            _uiState.value = _uiState.value.copy(errorMessage = "Microphone permission required")
            return
        }

        audioLevelJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val sampleRate = _uiState.value.sampleRate
                val channelConfig = if (_uiState.value.stereo)
                    AudioFormat.CHANNEL_IN_STEREO
                else
                    AudioFormat.CHANNEL_IN_MONO
                val bufferSize = AudioRecord.getMinBufferSize(
                    sampleRate,
                    channelConfig,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize * 2
                )

                val sessionId = audioRecord!!.audioSessionId

                // Apply audio effects
                if (_uiState.value.noiseGateEnabled && NoiseSuppressor.isAvailable()) {
                    noiseSuppressor = NoiseSuppressor.create(sessionId)
                    noiseSuppressor?.enabled = true
                }
                if (_uiState.value.autoGainEnabled && AutomaticGainControl.isAvailable()) {
                    gainControl = AutomaticGainControl.create(sessionId)
                    gainControl?.enabled = true
                }

                audioRecord?.startRecording()

                val buffer = ShortArray(bufferSize / 2)
                while (isActive && _uiState.value.isLive) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                    if (read > 0) {
                        val isMuted = _uiState.value.isMuted

                        if (isMuted) {
                            withContext(Dispatchers.Main) {
                                _uiState.value = _uiState.value.copy(
                                    audioLevelLeft = 0f,
                                    audioLevelRight = 0f
                                )
                            }
                        } else {
                            // Calculate RMS for left and right channels
                            var sumLeft = 0.0
                            var sumRight = 0.0
                            val isStereo = _uiState.value.stereo

                            if (isStereo) {
                                for (i in 0 until read step 2) {
                                    sumLeft += buffer[i].toDouble() * buffer[i].toDouble()
                                    if (i + 1 < read) {
                                        sumRight += buffer[i + 1].toDouble() * buffer[i + 1].toDouble()
                                    }
                                }
                                val countL = read / 2
                                val rmsLeft = sqrt(sumLeft / countL.coerceAtLeast(1))
                                val rmsRight = sqrt(sumRight / countL.coerceAtLeast(1))
                                val levelLeft = rmsToLevel(rmsLeft)
                                val levelRight = rmsToLevel(rmsRight)

                                // Apply gain
                                val gain = _uiState.value.gainDb
                                val gainMultiplier = if (gain != 0f) Math.pow(10.0, gain / 20.0).toFloat() else 1f

                                withContext(Dispatchers.Main) {
                                    _uiState.value = _uiState.value.copy(
                                        audioLevelLeft = (levelLeft * gainMultiplier).coerceIn(0f, 1f),
                                        audioLevelRight = (levelRight * gainMultiplier).coerceIn(0f, 1f)
                                    )
                                }
                            } else {
                                for (i in 0 until read) {
                                    sumLeft += buffer[i].toDouble() * buffer[i].toDouble()
                                }
                                val rms = sqrt(sumLeft / read.coerceAtLeast(1))
                                val level = rmsToLevel(rms)
                                val gain = _uiState.value.gainDb
                                val gainMultiplier = if (gain != 0f) Math.pow(10.0, gain / 20.0).toFloat() else 1f

                                withContext(Dispatchers.Main) {
                                    _uiState.value = _uiState.value.copy(
                                        audioLevelLeft = (level * gainMultiplier).coerceIn(0f, 1f),
                                        audioLevelRight = (level * gainMultiplier).coerceIn(0f, 1f)
                                    )
                                }
                            }
                        }
                    }
                    delay(50) // ~20fps level updates
                }
            } catch (e: Exception) {
                Log.e(TAG, "Audio level monitoring error: ${e.message}")
            }
        }
    }

    private fun rmsToLevel(rms: Double): Float {
        if (rms <= 0) return 0f
        // Convert to dB relative to max 16-bit value (32768)
        val db = 20 * log10(rms / 32768.0)
        // Map -60dB..0dB to 0..1
        return ((db + 60) / 60.0).toFloat().coerceIn(0f, 1f)
    }

    fun setNowPlaying(value: String) {
        _uiState.value = _uiState.value.copy(nowPlaying = value)
    }

    fun setCurrentShow(value: String) {
        _uiState.value = _uiState.value.copy(currentShow = value)
    }

    fun setNextShow(value: String) {
        _uiState.value = _uiState.value.copy(nextShow = value)
    }

    fun setRtmpUrl(value: String) {
        _uiState.value = _uiState.value.copy(rtmpUrl = value)
    }

    fun setStreamKey(value: String) {
        _uiState.value = _uiState.value.copy(streamKey = value)
    }

    fun setMode(mode: RadioMode) {
        _uiState.value = _uiState.value.copy(mode = mode)
    }

    fun setProtocol(protocol: RadioProtocol) {
        _uiState.value = _uiState.value.copy(protocol = protocol)
    }

    fun toggleMute() {
        val muted = !_uiState.value.isMuted
        _uiState.value = _uiState.value.copy(isMuted = muted)
        publisher?.setVolume(if (muted) 0f else 1f)
    }

    fun setAudioBitrate(kbps: Int) {
        _uiState.value = _uiState.value.copy(audioBitrateKbps = kbps)
    }

    fun setGain(db: Float) {
        _uiState.value = _uiState.value.copy(gainDb = db.coerceIn(-12f, 12f))
    }

    fun toggleNoiseGate() {
        val enabled = !_uiState.value.noiseGateEnabled
        _uiState.value = _uiState.value.copy(noiseGateEnabled = enabled)
        noiseSuppressor?.enabled = enabled
    }

    fun toggleAutoGain() {
        val enabled = !_uiState.value.autoGainEnabled
        _uiState.value = _uiState.value.copy(autoGainEnabled = enabled)
        gainControl?.enabled = enabled
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        stopBroadcast()
    }
}
