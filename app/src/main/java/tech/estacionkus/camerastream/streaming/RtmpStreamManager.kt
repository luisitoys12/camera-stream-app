package tech.estacionkus.camerastream.streaming

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import cn.nodemedia.NodePublisher
import dagger.hilt.android.qualifiers.ApplicationContext
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

    private val _state = MutableStateFlow(StreamState.IDLE)
    val state: StateFlow<StreamState> = _state.asStateFlow()
    val stats = MutableStateFlow(StreamStats())

    private var publisher: NodePublisher? = null
    private var bitrateKbps = 2500
    private var currentUrl: String? = null

    fun configure(width: Int, height: Int, fps: Int, bitrate: Int) {
        bitrateKbps = bitrate
        release()
        publisher = NodePublisher(context, "").apply {
            setHWAccelEnable(true)
            setVideoCodecParam(
                NodePublisher.NMC_CODEC_ID_H264,
                NodePublisher.NMC_PROFILE_H264_MAIN,
                width, height, fps, bitrate * 1000
            )
            setAudioCodecParam(
                NodePublisher.NMC_CODEC_ID_AAC,
                NodePublisher.NMC_PROFILE_AAC_LC,
                44100, 2, 64000
            )
            setOnNodePublisherEventListener { _, event, msg ->
                Log.d(TAG, "Event: $event msg: $msg")
                when (event) {
                    2000 -> _state.value = StreamState.LIVE        // connected
                    2001 -> _state.value = StreamState.IDLE        // disconnected
                    2002 -> _state.value = StreamState.ERROR       // error
                }
            }
        }
    }

    fun startStream(url: String, key: String) {
        val fullUrl = "${url.trimEnd('/')}/$key"
        currentUrl = fullUrl
        _state.value = StreamState.CONNECTING

        if (publisher == null) {
            configure(1280, 720, 30, bitrateKbps)
        }
        publisher?.start(fullUrl)
    }

    fun stopStream() {
        publisher?.stop()
        _state.value = StreamState.IDLE
        currentUrl = null
    }

    fun setBitrate(kbps: Int) {
        bitrateKbps = kbps
        // NodePublisher doesn't support runtime bitrate change; reconfigure needed
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
        try { publisher?.stop() } catch (_: Exception) {}
        try { publisher?.closeCamera() } catch (_: Exception) {}
        try { publisher?.detachView() } catch (_: Exception) {}
        publisher = null
    }

    fun isConfigured(): Boolean = publisher != null
}
