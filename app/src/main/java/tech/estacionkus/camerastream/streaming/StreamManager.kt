package tech.estacionkus.camerastream.streaming

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import tech.estacionkus.camerastream.data.settings.StreamSettings

data class StreamStats(
    val isLive: Boolean = false,
    val durationSeconds: Long = 0,
    val bitrateBps: Int = 0,
    val droppedFrames: Int = 0,
    val srtConnected: Boolean = false,
    val rtmpConnections: Map<String, Boolean> = emptyMap()
)

class StreamManager(private val context: Context) {
    private val _stats = MutableStateFlow(StreamStats())
    val stats: StateFlow<StreamStats> = _stats

    private val rtmpOutput = RTMPOutput(context)
    private val srtOutput = SRTOutput()
    private var timerJob: Job? = null
    private var startTime = 0L

    fun startStream(settings: StreamSettings) {
        val destinations = mutableListOf<RTMPDestination>()
        if (settings.enableYoutube && settings.youtubeKey.isNotBlank())
            destinations += RTMPDestination("rtmp://a.rtmp.youtube.com/live2", settings.youtubeKey, "YouTube")
        if (settings.enableTwitch && settings.twitchKey.isNotBlank())
            destinations += RTMPDestination("rtmp://live.twitch.tv/app", settings.twitchKey, "Twitch")
        if (settings.enableFacebook && settings.facebookKey.isNotBlank())
            destinations += RTMPDestination("rtmps://live-api-s.facebook.com:443/rtmp", settings.facebookKey, "Facebook")
        if (settings.enableKick && settings.kickKey.isNotBlank())
            destinations += RTMPDestination("rtmps://fa723fc1b171.global-contribute.live-video.net/app", settings.kickKey, "Kick")
        if (settings.enableTiktok && settings.tiktokKey.isNotBlank())
            destinations += RTMPDestination("rtmp://push.tiktok.com/live", settings.tiktokKey, "TikTok")

        val (width, height) = parseResolution(settings.resolution)
        rtmpOutput.connect(destinations, width, height, settings.fps, settings.videoBitrateKbps)

        var srtConnected = false
        if (settings.enableSrt && settings.srtHost.isNotBlank()) {
            srtConnected = srtOutput.connect(settings.srtHost, settings.srtPort, settings.srtLatencyMs)
        }

        startTime = System.currentTimeMillis()
        timerJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                delay(1000)
                _stats.value = StreamStats(
                    isLive = true,
                    durationSeconds = (System.currentTimeMillis() - startTime) / 1000,
                    srtConnected = srtOutput.isConnected,
                    rtmpConnections = destinations.associate { it.label to rtmpOutput.isConnected(it.label) }
                )
            }
        }
    }

    fun stopStream() {
        timerJob?.cancel()
        rtmpOutput.disconnect()
        srtOutput.disconnect()
        _stats.value = StreamStats()
    }

    private fun parseResolution(res: String): Pair<Int, Int> {
        val parts = res.split("x")
        return if (parts.size == 2) Pair(parts[0].toIntOrNull() ?: 1920, parts[1].toIntOrNull() ?: 1080)
        else Pair(1920, 1080)
    }
}
