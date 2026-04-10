package tech.estacionkus.camerastream.streaming

import android.content.Context
import android.view.SurfaceView
import cn.nodemedia.NodePublisher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class StreamState { IDLE, CONNECTING, LIVE, RECONNECTING, ERROR }

data class StreamStats(
    val bitrateKbps: Int = 0,
    val fps: Int = 0,
    val droppedFrames: Int = 0,
    val elapsedSeconds: Long = 0
)

@Singleton
class RtmpStreamManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var publisher: NodePublisher? = null

    private val _state = MutableStateFlow(StreamState.IDLE)
    val state: StateFlow<StreamState> = _state.asStateFlow()

    private val _stats = MutableStateFlow(StreamStats())
    val stats: StateFlow<StreamStats> = _stats.asStateFlow()

    private var startTimeMs = 0L

    fun init(surfaceView: SurfaceView, bitrateKbps: Int, fps: Int, width: Int, height: Int) {
        publisher = NodePublisher(context, "").apply {
            setVideoParam(width, height, fps, bitrateKbps * 1000)
            setAudioParam(44100, 2, 64000)
            setVideoOrientation(0)
            setHardwareAcceleration(true)
            setAdaptiveBitrate(false)
            setSurface(surfaceView)
            setOnNodePublisherEventListener { event, msg ->
                when (event) {
                    NodePublisher.EVENT_RTMP_CONNECTED -> {
                        _state.value = StreamState.LIVE
                        startTimeMs = System.currentTimeMillis()
                    }
                    NodePublisher.EVENT_RTMP_DISCONNECT -> _state.value = StreamState.IDLE
                    NodePublisher.EVENT_RTMP_ERROR -> _state.value = StreamState.ERROR
                    NodePublisher.EVENT_BUFFER_STATUS -> {
                        val elapsed = (System.currentTimeMillis() - startTimeMs) / 1000
                        _stats.value = _stats.value.copy(elapsedSeconds = elapsed)
                    }
                }
            }
        }
    }

    fun startStream(rtmpUrl: String, streamKey: String) {
        _state.value = StreamState.CONNECTING
        publisher?.startPublish("${rtmpUrl.trimEnd('/')}/${streamKey.trim()}")
    }

    fun stopStream() {
        publisher?.stopPublish()
        _state.value = StreamState.IDLE
        startTimeMs = 0L
        _stats.value = StreamStats()
    }

    fun flipCamera() { publisher?.switchCamera() }
    fun setMute(muted: Boolean) { publisher?.setAudioMuted(muted) }
    fun setBitrate(kbps: Int) { publisher?.setVideoBitrate(kbps * 1000) }

    fun startRecording(outputPath: String) { publisher?.startRecord(outputPath) }
    fun stopRecording() { publisher?.stopRecord() }

    fun release() {
        publisher?.stopPublish()
        publisher = null
    }
}
