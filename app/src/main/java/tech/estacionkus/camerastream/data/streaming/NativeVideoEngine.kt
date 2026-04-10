package tech.estacionkus.camerastream.data.streaming

import android.content.Context
import android.hardware.camera2.*
import android.media.*
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

data class VideoConfig(
    val width: Int = 1280,
    val height: Int = 720,
    val fps: Int = 30,
    val videoBitrateKbps: Int = 2500,
    val audioBitrateKbps: Int = 128,
    val adaptiveBitrate: Boolean = false
)

data class StreamTarget(
    val type: StreamType,
    val url: String,
    val streamKey: String = ""
)

enum class StreamType { RTMP, RTMPS, SRT_CALLER, SRT_SERVER }

data class EngineState(
    val isStreaming: Boolean = false,
    val isStandby: Boolean = false,
    val bitrateKbps: Int = 0,
    val fps: Int = 0,
    val droppedFrames: Int = 0,
    val networkQuality: NetworkQuality = NetworkQuality.GOOD,
    val activeTargets: List<String> = emptyList(),
    val errorMessage: String? = null
)

enum class NetworkQuality { EXCELLENT, GOOD, FAIR, POOR, CRITICAL }

@Singleton
class NativeVideoEngine @Inject constructor(
    private val context: Context
) {
    private val _state = MutableStateFlow(EngineState())
    val state: StateFlow<EngineState> = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val activePublishers = mutableListOf<RtmpPublisher>()
    private val activeSrtConnections = mutableListOf<SrtConnection>()
    private var mediaCodecVideo: MediaCodec? = null
    private var mediaCodecAudio: MediaCodec? = null
    private var audioRecord: AudioRecord? = null
    private var config = VideoConfig()
    private var isMuted = false
    private var isStandby = false

    fun configure(newConfig: VideoConfig) { config = newConfig }

    fun startStream(targets: List<StreamTarget>) {
        scope.launch {
            try {
                _state.value = _state.value.copy(isStreaming = true, errorMessage = null)
                setupMediaCodec()
                targets.forEach { target ->
                    when (target.type) {
                        StreamType.RTMP, StreamType.RTMPS -> startRtmpPublisher(target)
                        StreamType.SRT_CALLER -> startSrtCaller(target)
                        StreamType.SRT_SERVER -> { /* handled by SrtServerManager */ }
                    }
                }
                startAudioCapture()
                if (config.adaptiveBitrate) startBitrateMonitor()
                _state.value = _state.value.copy(
                    activeTargets = targets.map { it.url }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isStreaming = false, errorMessage = e.message)
            }
        }
    }

    fun stopStream() {
        scope.launch {
            activePublishers.forEach { it.stop() }
            activePublishers.clear()
            activeSrtConnections.forEach { it.close() }
            activeSrtConnections.clear()
            releaseMediaCodec()
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            _state.value = EngineState()
        }
    }

    fun setMuted(muted: Boolean) { isMuted = muted }

    fun setStandby(standby: Boolean) {
        isStandby = standby
        _state.value = _state.value.copy(isStandby = standby)
    }

    private fun setupMediaCodec() {
        mediaCodecVideo = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC).apply {
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, config.width, config.height).apply {
                setInteger(MediaFormat.KEY_BIT_RATE, config.videoBitrateKbps * 1000)
                setInteger(MediaFormat.KEY_FRAME_RATE, config.fps)
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
            }
            configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            start()
        }
        mediaCodecAudio = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC).apply {
            val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 2).apply {
                setInteger(MediaFormat.KEY_BIT_RATE, config.audioBitrateKbps * 1000)
                setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            }
            configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            start()
        }
    }

    private fun startAudioCapture() {
        val bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT)
        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSize).apply {
            startRecording()
        }
    }

    private fun startBitrateMonitor() {
        scope.launch {
            while (_state.value.isStreaming) {
                delay(3000)
                val quality = estimateNetworkQuality()
                if (config.adaptiveBitrate) adjustBitrateForQuality(quality)
                _state.value = _state.value.copy(networkQuality = quality)
            }
        }
    }

    private fun estimateNetworkQuality(): NetworkQuality {
        val currentBitrate = _state.value.bitrateKbps
        val targetBitrate = config.videoBitrateKbps
        return when {
            currentBitrate > targetBitrate * 0.9 -> NetworkQuality.EXCELLENT
            currentBitrate > targetBitrate * 0.7 -> NetworkQuality.GOOD
            currentBitrate > targetBitrate * 0.5 -> NetworkQuality.FAIR
            currentBitrate > targetBitrate * 0.3 -> NetworkQuality.POOR
            else -> NetworkQuality.CRITICAL
        }
    }

    private fun adjustBitrateForQuality(quality: NetworkQuality) {
        val newBitrate = when (quality) {
            NetworkQuality.EXCELLENT -> config.videoBitrateKbps
            NetworkQuality.GOOD      -> (config.videoBitrateKbps * 0.85).toInt()
            NetworkQuality.FAIR      -> (config.videoBitrateKbps * 0.65).toInt()
            NetworkQuality.POOR      -> (config.videoBitrateKbps * 0.40).toInt()
            NetworkQuality.CRITICAL  -> (config.videoBitrateKbps * 0.20).toInt()
        }
        mediaCodecVideo?.setParameters(android.os.Bundle().apply {
            putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, newBitrate * 1000)
        })
    }

    private fun startRtmpPublisher(target: StreamTarget) {
        val publisher = RtmpPublisher("${target.url}/${target.streamKey}")
        publisher.start()
        activePublishers.add(publisher)
    }

    private fun startSrtCaller(target: StreamTarget) {
        val conn = SrtConnection(target.url)
        conn.connect()
        activeSrtConnections.add(conn)
    }

    private fun releaseMediaCodec() {
        mediaCodecVideo?.stop(); mediaCodecVideo?.release(); mediaCodecVideo = null
        mediaCodecAudio?.stop(); mediaCodecAudio?.release(); mediaCodecAudio = null
    }
}

class RtmpPublisher(private val url: String) {
    fun start() { /* NodeMediaClient integration point */ }
    fun stop() { /* NodeMediaClient stop */ }
}

class SrtConnection(private val url: String) {
    fun connect() { /* srtdroid connect as caller */ }
    fun close() { /* srtdroid close */ }
}
