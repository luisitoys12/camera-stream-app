package tech.estacionkus.camerastream.data.settings

import kotlinx.serialization.Serializable

@Serializable
data class StreamSettings(
    val srtHost: String = "",
    val srtPort: Int = 9998,
    val srtLatencyMs: Int = 200,
    val srtPassphrase: String = "",
    val youtubeKey: String = "",
    val twitchKey: String = "",
    val facebookKey: String = "",
    val kickKey: String = "",
    val tiktokKey: String = "",
    val resolution: String = "1920x1080",
    val fps: Int = 30,
    val videoBitrateKbps: Int = 4000,
    val audioBitrateKbps: Int = 128,
    val enableSrt: Boolean = false,
    val enableYoutube: Boolean = false,
    val enableTwitch: Boolean = false,
    val enableFacebook: Boolean = false,
    val enableKick: Boolean = false,
    val enableTiktok: Boolean = false,
)
