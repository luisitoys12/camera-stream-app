package tech.estacionkus.camerastream.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tech.estacionkus.camerastream.domain.model.PlanFeatures
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeatureGate @Inject constructor() {
    var features: PlanFeatures = PlanFeatures.FREE
        private set

    private val _planName = MutableStateFlow("Free")
    val planName: StateFlow<String> = _planName.asStateFlow()

    fun upgrade(f: PlanFeatures) {
        features = f
        _planName.value = when (f) {
            PlanFeatures.PRO -> "Pro"
            PlanFeatures.AGENCY -> "Agency"
            else -> "Free"
        }
    }

    fun canMultiStream() = features.maxPlatforms > 1
    fun canSrt() = features.allowSrt
    fun canSrtServer() = features.allowSrtServer
    fun canTunnel() = features.allowCloudflaredTunnel
    fun canScenes() = features.allowScenes
    fun canManualCamera() = features.allowManualCamera
    fun canMultiChat() = features.allowMultiChat
    fun can1080p() = features.maxResolution.height >= 1080
    fun canDisconnectProtection() = features.allowDisconnectProtection
    fun canGuestMode() = features.allowGuestMode
    fun canBeautyFilter() = features.allowBeautyFilter
    fun canColorFilters() = features.allowColorFilters
    fun canSportsMode() = features.allowSportsMode
    fun canStreamHealth() = features.allowStreamHealth
    fun canAlertWidgets() = features.allowAlertWidgets
    fun canWebOverlay() = features.allowWebOverlay
    fun maxGuests() = features.maxGuests
    fun maxFps() = features.maxFps
    fun showWatermark() = features.showWatermark

    fun check(feature: String): String? = when (feature) {
        "multistream" -> if (canMultiStream()) null else "Upgrade to Pro for multi-streaming"
        "srt" -> if (canSrt()) null else "SRT available on Pro plan"
        "srt_server" -> if (canSrtServer()) null else "SRT Server on Pro plan"
        "scenes" -> if (canScenes()) null else "Scenes available on Pro plan"
        "manual_cam" -> if (canManualCamera()) null else "Manual camera on Pro plan"
        "1080p" -> if (can1080p()) null else "1080p on Pro plan"
        "disconnect_protection" -> if (canDisconnectProtection()) null else "Disconnect Protection on Pro plan"
        "guest_mode" -> if (canGuestMode()) null else "Guest Mode on Pro plan"
        "beauty_filter" -> if (canBeautyFilter()) null else "Beauty Filter on Pro plan"
        "sports_mode" -> if (canSportsMode()) null else "Sports Mode on Pro plan"
        "stream_health" -> if (canStreamHealth()) null else "Stream Health on Pro plan"
        "alerts" -> if (canAlertWidgets()) null else "Alert Widgets on Pro plan"
        else -> null
    }
}
