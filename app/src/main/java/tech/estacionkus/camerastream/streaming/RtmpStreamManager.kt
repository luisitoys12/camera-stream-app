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

enum class StreamState { IDLE, CONNECTING, LIVE, ERROR }
data class StreamStats(val droppedFrames: Int = 0, val bitrateKbps: Int = 0)

@Singleton
class RtmpStreamManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _state = MutableStateFlow(StreamState.IDLE)
    val state: StateFlow<StreamState> = _state.asStateFlow()
    val stats = MutableStateFlow(StreamStats())

    private var publisher: NodePublisher? = null
    private var bitrateKbps = 2500

    fun configure(url: String, bitrate: Int, surfaceView: SurfaceView) {
        bitrateKbps = bitrate
        publisher = NodePublisher(context, "").apply {
            setVideoParam(1280, 720, 30, bitrate * 1000)
            setAudioParam(44100, 2, 64000)
            setHardwareAcceleration(true)
            setSurface(surfaceView)
            setOnNodePublisherEventListener { event, _ ->
                _state.value = when (event) {
                    NodePublisher.EVENT_RTMP_CONNECTED -> StreamState.LIVE
                    NodePublisher.EVENT_RTMP_DISCONNECT -> StreamState.IDLE
                    NodePublisher.EVENT_RTMP_ERROR -> StreamState.ERROR
                    else -> _state.value
                }
            }
        }
    }

    fun startStream(url: String) {
        _state.value = StreamState.CONNECTING
        publisher?.startPublish(url)
    }

    fun stopStream() {
        publisher?.stopPublish()
        _state.value = StreamState.IDLE
    }

    fun setBitrate(kbps: Int) {
        bitrateKbps = kbps
        publisher?.setVideoBitrate(kbps * 1000)
    }

    fun flipCamera() { publisher?.switchCamera() }
    fun setMuted(muted: Boolean) { publisher?.setAudioMuted(muted) }
    fun startRecord(path: String) { publisher?.startRecord(path) }
    fun stopRecord() { publisher?.stopRecord() }
}
