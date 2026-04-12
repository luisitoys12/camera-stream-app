package tech.estacionkus.camerastream.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class StreamTarget(
    val id: String,
    val name: String,
    val rtmpUrl: String,
    val streamKey: String,
    val isEnabled: Boolean = true
) {
    val fullUrl get() = "${rtmpUrl.trimEnd('/')}/$streamKey"
}

enum class Platform(val displayName: String, val rtmpBase: String) {
    TWITCH("Twitch", "rtmp://live.twitch.tv/app/"),
    YOUTUBE("YouTube", "rtmp://a.rtmp.youtube.com/live2/"),
    KICK("Kick", "rtmps://fa723fc1b171.global-contribute.live-video.net/app/"),
    TIKTOK("TikTok", "rtmp://push.tiktok.com/live/"),
    FACEBOOK("Facebook", "rtmps://live-api-s.facebook.com:443/rtmp/"),
    CUSTOM("Custom", "")
}
