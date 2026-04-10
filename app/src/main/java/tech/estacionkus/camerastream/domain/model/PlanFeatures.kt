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
        val FREE = PlanFeatures(1, false, false, false, false, false, false, false, true, false, Resolution(1280, 720))
        val PRO = PlanFeatures(5, true, true, true, true, true, true, true, true, true, Resolution(1920, 1080))
    }
}
