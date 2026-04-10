package tech.estacionkus.camerastream.domain.model

data class CameraSettings(
    val iso: Int = -1,              // -1 = auto
    val exposureCompensation: Int = 0,
    val whiteBalance: WhiteBalance = WhiteBalance.AUTO,
    val focusMode: FocusMode = FocusMode.AUTO,
    val zoomRatio: Float = 1f,
    val stabilizationEnabled: Boolean = true,
    val nightModeEnabled: Boolean = false
)

enum class WhiteBalance(val label: String) {
    AUTO("Auto"),
    INCANDESCENT("Incandescent"),
    FLUORESCENT("Fluorescent"),
    DAYLIGHT("Daylight"),
    CLOUDY("Nublado")
}

enum class FocusMode { AUTO, MANUAL, CONTINUOUS }
