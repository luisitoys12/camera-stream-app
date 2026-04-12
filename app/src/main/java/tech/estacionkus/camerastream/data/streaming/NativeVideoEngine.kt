package tech.estacionkus.camerastream.data.streaming

import android.content.Context
import android.media.*
import android.util.Log
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

/**
 * Low-level MediaCodec video encoding engine.
 * For RTMP streaming, use RtmpStreamManager (NodePublisher) instead — it handles
 * camera capture, encoding, and RTMP publishing natively.
 * This engine is for advanced use cases like custom SRT output or local recording.
 */
@Singleton
class NativeVideoEngine @Inject constructor(
    private val context: Context
) {
    private val TAG = "NativeVideoEngine"
    private val _state = MutableStateFlow(EngineState())
    val state: StateFlow<EngineState> = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var mediaCodecVideo: MediaCodec? = null
    private var mediaCodecAudio: MediaCodec? = null
    private var audioRecord: AudioRecord? = null
    private var config = VideoConfig()
    private var isMuted = false

    fun configure(newConfig: VideoConfig) { config = newConfig }

    fun getEncoderSurface(): android.view.Surface? {
        return mediaCodecVideo?.createInputSurface()
    }

    fun startEncoding() {
        scope.launch {
            try {
                _state.value = _state.value.copy(isStreaming = true, errorMessage = null)
                setupVideoEncoder()
                setupAudioEncoder()
                startAudioCapture()
                if (config.adaptiveBitrate) startBitrateMonitor()
            } catch (e: Exception) {
                Log.e(TAG, "Encoding error: ${e.message}", e)
                _state.value = _state.value.copy(isStreaming = false, errorMessage = e.message)
            }
        }
    }

    fun stopEncoding() {
        scope.launch {
            releaseMediaCodec()
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            _state.value = EngineState()
        }
    }

    fun setMuted(muted: Boolean) { isMuted = muted }

    fun updateBitrate(bitrateKbps: Int) {
        try {
            mediaCodecVideo?.setParameters(android.os.Bundle().apply {
                putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, bitrateKbps * 1000)
            })
            config = config.copy(videoBitrateKbps = bitrateKbps)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update bitrate: ${e.message}")
        }
    }

    private fun setupVideoEncoder() {
        mediaCodecVideo = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC).apply {
            val format = MediaFormat.createVideoFormat(
                MediaFormat.MIMETYPE_VIDEO_AVC, config.width, config.height
            ).apply {
                setInteger(MediaFormat.KEY_BIT_RATE, config.videoBitrateKbps * 1000)
                setInteger(MediaFormat.KEY_FRAME_RATE, config.fps)
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
            }
            configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        }
    }

    private fun setupAudioEncoder() {
        mediaCodecAudio = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC).apply {
            val format = MediaFormat.createAudioFormat(
                MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 2
            ).apply {
                setInteger(MediaFormat.KEY_BIT_RATE, config.audioBitrateKbps * 1000)
                setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            }
            configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            start()
        }
    }

    private fun startAudioCapture() {
        val bufferSize = AudioRecord.getMinBufferSize(
            44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT
        )
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC, 44100,
            AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSize
        ).apply { startRecording() }
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
            NetworkQuality.GOOD -> (config.videoBitrateKbps * 0.85).toInt()
            NetworkQuality.FAIR -> (config.videoBitrateKbps * 0.65).toInt()
            NetworkQuality.POOR -> (config.videoBitrateKbps * 0.40).toInt()
            NetworkQuality.CRITICAL -> (config.videoBitrateKbps * 0.20).toInt()
        }
        updateBitrate(newBitrate)
    }

    private fun releaseMediaCodec() {
        try { mediaCodecVideo?.stop() } catch (_: Exception) {}
        try { mediaCodecVideo?.release() } catch (_: Exception) {}
        mediaCodecVideo = null
        try { mediaCodecAudio?.stop() } catch (_: Exception) {}
        try { mediaCodecAudio?.release() } catch (_: Exception) {}
        mediaCodecAudio = null
    }
}
