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
    val maxResolution: Resolution
) {
    companion object {
        val FREE = PlanFeatures(
            maxPlatforms = 1,
            allowSrt = false,
            allowSrtServer = false,
            allowCloudflaredTunnel = false,
            allowScenes = false,
            allowChromaKey = false,
            allowManualCamera = false,
            allowMultiChat = false,
            allowRecording = true,
            allowOverlayVideo = false,
            maxResolution = Resolution(1280, 720)
        )
        val PRO = PlanFeatures(
            maxPlatforms = 5,
            allowSrt = true,
            allowSrtServer = true,
            allowCloudflaredTunnel = true,
            allowScenes = true,
            allowChromaKey = true,
            allowManualCamera = true,
            allowMultiChat = true,
            allowRecording = true,
            allowOverlayVideo = true,
            maxResolution = Resolution(1920, 1080)
        )
    }
}
