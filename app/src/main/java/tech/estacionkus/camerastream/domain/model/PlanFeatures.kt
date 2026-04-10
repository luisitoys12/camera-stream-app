package tech.estacionkus.camerastream.domain.model

data class PlanFeatures(
    val maxPlatforms: Int,
    val maxResolution: Resolution,
    val maxFps: Int,
    val allowSrt: Boolean,
    val allowMultiChat: Boolean,
    val allowRecording: Boolean,
    val allowOverlayVideo: Boolean,
    val allowScenes: Boolean,
    val allowChromaKey: Boolean,
    val allowManualCamera: Boolean,
    val allowSrtServer: Boolean,
    val allowCloudflaredTunnel: Boolean
) {
    companion object {
        val FREE = PlanFeatures(
            maxPlatforms = 1,
            maxResolution = Resolution.HD_720,
            maxFps = 30,
            allowSrt = false,
            allowMultiChat = false,
            allowRecording = true,
            allowOverlayVideo = false,
            allowScenes = false,
            allowChromaKey = false,
            allowManualCamera = false,
            allowSrtServer = false,
            allowCloudflaredTunnel = false
        )
        val STARTER = PlanFeatures(
            maxPlatforms = 2,
            maxResolution = Resolution.HD_720,
            maxFps = 30,
            allowSrt = true,
            allowMultiChat = false,
            allowRecording = true,
            allowOverlayVideo = true,
            allowScenes = false,
            allowChromaKey = false,
            allowManualCamera = false,
            allowSrtServer = false,
            allowCloudflaredTunnel = false
        )
        val PRO = PlanFeatures(
            maxPlatforms = 5,
            maxResolution = Resolution.FHD_1080,
            maxFps = 60,
            allowSrt = true,
            allowMultiChat = true,
            allowRecording = true,
            allowOverlayVideo = true,
            allowScenes = true,
            allowChromaKey = true,
            allowManualCamera = true,
            allowSrtServer = true,
            allowCloudflaredTunnel = true
        )
        val AGENCY = PRO.copy(maxPlatforms = 99)

        fun fromPlanId(id: String?) = when (id) {
            "starter" -> STARTER
            "pro" -> PRO
            "agency" -> AGENCY
            else -> FREE
        }
    }
}

enum class Resolution(val width: Int, val height: Int, val label: String) {
    HD_720(1280, 720, "720p"),
    FHD_1080(1920, 1080, "1080p")
}
