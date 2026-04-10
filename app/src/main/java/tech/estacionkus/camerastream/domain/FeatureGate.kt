package tech.estacionkus.camerastream.domain

import tech.estacionkus.camerastream.domain.model.PlanFeatures
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeatureGate @Inject constructor() {
    var features: PlanFeatures = PlanFeatures.FREE
        private set

    fun upgrade(f: PlanFeatures) { features = f }
    fun canMultiStream() = features.maxPlatforms > 1
    fun canSrt() = features.allowSrt
    fun canSrtServer() = features.allowSrtServer
    fun canTunnel() = features.allowCloudflaredTunnel
    fun canScenes() = features.allowScenes
    fun canManualCamera() = features.allowManualCamera
    fun canMultiChat() = features.allowMultiChat
    fun can1080p() = features.maxResolution.height >= 1080
    fun check(feature: String): String? = when (feature) {
        "multistream" -> if (canMultiStream()) null else "🔒 Disponible en plan Starter"
        "srt" -> if (canSrt()) null else "🔒 SRT disponible en Pro"
        "srt_server" -> if (canSrtServer()) null else "🔒 Servidor SRT en Pro"
        "scenes" -> if (canScenes()) null else "🔒 Escenas en Pro"
        "manual_cam" -> if (canManualCamera()) null else "🔒 Cámara manual en Pro"
        "1080p" -> if (can1080p()) null else "🔒 1080p en Pro"
        else -> null
    }
}
