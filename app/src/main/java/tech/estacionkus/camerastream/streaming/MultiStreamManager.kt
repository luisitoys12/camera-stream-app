package tech.estacionkus.camerastream.streaming

import android.content.Context
import android.view.SurfaceView
import cn.nodemedia.NodePublisher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tech.estacionkus.camerastream.domain.model.StreamTarget
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MultiStreamManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val publishers = mutableMapOf<String, NodePublisher>()
    private val _states = MutableStateFlow<Map<String, StreamState>>(emptyMap())
    val states: StateFlow<Map<String, StreamState>> = _states.asStateFlow()

    fun startAll(targets: List<StreamTarget>, surfaceView: SurfaceView, bitrateKbps: Int, fps: Int, width: Int, height: Int) {
        stopAll()
        targets.filter { it.isEnabled }.forEachIndexed { index, target ->
            val pub = NodePublisher(context, "").apply {
                setVideoParam(width, height, fps, bitrateKbps * 1000)
                setAudioParam(44100, 2, 64000)
                setHardwareAcceleration(true)
                if (index == 0) setSurface(surfaceView)
                setOnNodePublisherEventListener { event, _ ->
                    val s = when (event) {
                        NodePublisher.EVENT_RTMP_CONNECTED -> StreamState.LIVE
                        NodePublisher.EVENT_RTMP_DISCONNECT -> StreamState.IDLE
                        NodePublisher.EVENT_RTMP_ERROR -> StreamState.ERROR
                        else -> _states.value[target.id] ?: StreamState.IDLE
                    }
                    _states.value = _states.value + (target.id to s)
                }
            }
            pub.startPublish(target.fullUrl)
            publishers[target.id] = pub
        }
    }

    fun stopAll() {
        publishers.values.forEach { it.stopPublish() }
        publishers.clear()
        _states.value = emptyMap()
    }

    fun flipCamera() { publishers.values.firstOrNull()?.switchCamera() }
    fun setMute(muted: Boolean) { publishers.values.forEach { it.setAudioMuted(muted) } }
    fun setBitrate(kbps: Int) { publishers.values.forEach { it.setVideoBitrate(kbps * 1000) } }
}
