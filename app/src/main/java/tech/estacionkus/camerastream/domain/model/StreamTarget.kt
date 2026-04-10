package tech.estacionkus.camerastream.domain.model

data class StreamTarget(
    val id: String,
    val name: String,
    val rtmpUrl: String,
    val streamKey: String,
    val isEnabled: Boolean = true
) {
    val fullUrl get() = "${rtmpUrl.trimEnd('/')}/$streamKey"
}

enum class Platform { TWITCH, YOUTUBE, KICK, TIKTOK, CUSTOM }
