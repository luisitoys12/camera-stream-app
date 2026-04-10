package tech.estacionkus.camerastream.domain

import tech.estacionkus.camerastream.domain.model.PlanFeatures
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeatureGate @Inject constructor() {
    var features: PlanFeatures = PlanFeatures.FREE
        private set

    fun update(f: PlanFeatures) { features = f }

    fun canMultiStream() = features.maxPlatforms > 1
    fun canUseSrt() = features.allowSrt
    fun canUseSrtServer() = features.allowSrtServer
    fun canUseTunnel() = features.allowCloudflaredTunnel
    fun canUseScenes() = features.allowScenes
    fun canUseChromaKey() = features.allowChromaKey
    fun canUseManualCamera() = features.allowManualCamera
    fun canUseMultiChat() = features.allowMultiChat
    fun canRecordOverlays() = features.allowRecording
    fun can1080p() = features.maxResolution.height >= 1080
    fun canUseOverlayVideo() = features.allowOverlayVideo

    // Returns null if allowed, or a user-facing message if blocked
    fun check(feature: String): String? = when (feature) {
        "multistream" -> if (canMultiStream()) null else "🔒 Multistream disponible en plan Starter o superior"
        "srt" -> if (canUseSrt()) null else "🔒 SRT disponible en plan Starter o superior"
        "srt_server" -> if (canUseSrtServer()) null else "🔒 Servidor SRT disponible en plan Pro"
        "tunnel" -> if (canUseTunnel()) null else "🔒 Túnel Cloudflared disponible en plan Pro"
        "scenes" -> if (canUseScenes()) null else "🔒 Escenas disponibles en plan Pro"
        "chroma" -> if (canUseChromaKey()) null else "🔒 Chroma key disponible en plan Pro"
        "manual_cam" -> if (canUseManualCamera()) null else "🔒 Cámara manual disponible en plan Pro"
        "multichat" -> if (canUseMultiChat()) null else "🔒 Multi-chat disponible en plan Pro"
        "1080p" -> if (can1080p()) null else "🔒 1080p disponible en plan Pro"
        else -> null
    }
}
