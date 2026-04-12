package tech.estacionkus.camerastream.domain.model

data class Resolution(val width: Int, val height: Int)

data class PlanFeatures(
    val maxPlatforms: Int,
    val allowSrt: Boolean,
    val allowSrtServer: Boolean,
    val allowCloudflaredTunnel: Boolean,
    val allowScenes: Boolean,
    val allowChromaKey: Boolean,
    val allowManualCamera: Boolean,
    val allowMultiChat: Boolean,
    val allowRecording: Boolean,
    val allowOverlayVideo: Boolean,
    val maxResolution: Resolution,
    val allowDisconnectProtection: Boolean = false,
    val allowGuestMode: Boolean = false,
    val maxGuests: Int = 0,
    val maxFps: Int = 30,
    val allowBeautyFilter: Boolean = false,
    val allowColorFilters: Boolean = false,
    val allowCustomBranding: Boolean = false,
    val showWatermark: Boolean = true,
    val allowAlertWidgets: Boolean = false,
    val allowSportsMode: Boolean = false,
    val allowStreamHealth: Boolean = false,
    val allowWebOverlay: Boolean = false
) {
    companion object {
        val FREE = PlanFeatures(
            maxPlatforms = 1, allowSrt = false, allowSrtServer = false,
            allowCloudflaredTunnel = false, allowScenes = false, allowChromaKey = false,
            allowManualCamera = false, allowMultiChat = false, allowRecording = true,
            allowOverlayVideo = false, maxResolution = Resolution(1280, 720),
            allowDisconnectProtection = false, allowGuestMode = false, maxGuests = 0,
            maxFps = 30, allowBeautyFilter = false, allowColorFilters = false,
            allowCustomBranding = false, showWatermark = true, allowAlertWidgets = false,
            allowSportsMode = false, allowStreamHealth = false, allowWebOverlay = false
        )
        val PRO = PlanFeatures(
            maxPlatforms = 3, allowSrt = true, allowSrtServer = true,
            allowCloudflaredTunnel = true, allowScenes = true, allowChromaKey = true,
            allowManualCamera = true, allowMultiChat = true, allowRecording = true,
            allowOverlayVideo = true, maxResolution = Resolution(1920, 1080),
            allowDisconnectProtection = true, allowGuestMode = true, maxGuests = 2,
            maxFps = 60, allowBeautyFilter = true, allowColorFilters = true,
            allowCustomBranding = false, showWatermark = false, allowAlertWidgets = true,
            allowSportsMode = true, allowStreamHealth = true, allowWebOverlay = true
        )
        val AGENCY = PlanFeatures(
            maxPlatforms = 5, allowSrt = true, allowSrtServer = true,
            allowCloudflaredTunnel = true, allowScenes = true, allowChromaKey = true,
            allowManualCamera = true, allowMultiChat = true, allowRecording = true,
            allowOverlayVideo = true, maxResolution = Resolution(2560, 1440),
            allowDisconnectProtection = true, allowGuestMode = true, maxGuests = 4,
            maxFps = 60, allowBeautyFilter = true, allowColorFilters = true,
            allowCustomBranding = true, showWatermark = false, allowAlertWidgets = true,
            allowSportsMode = true, allowStreamHealth = true, allowWebOverlay = true
        )

        fun fromPlanId(planId: String?): PlanFeatures = when (planId) {
            "pro" -> PRO
            "agency" -> AGENCY
            else -> FREE
        }
    }
}
