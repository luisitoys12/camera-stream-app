package tech.estacionkus.camerastream.domain.model

data class CameraSettings(
    val iso: Int = -1,              // -1 = auto
    val exposureCompensation: Int = 0,
    val whiteBalance: WhiteBalance = WhiteBalance.AUTO,
    val focusMode: FocusMode = FocusMode.AUTO,
    val manualFocusDistance: Float = 0f, // 0..1 range
    val zoomRatio: Float = 1f,
    val stabilizationEnabled: Boolean = true,
    val nightModeEnabled: Boolean = false,
    val torchEnabled: Boolean = false,
    val beautyFilterEnabled: Boolean = false,
    val beautySmoothing: Float = 0.5f,
    val beautyBrightness: Float = 0.5f,
    val colorFilter: ColorFilter = ColorFilter.NONE,
    val gridOverlay: GridOverlay = GridOverlay.NONE
)

enum class WhiteBalance(val label: String) {
    AUTO("Auto"),
    INCANDESCENT("Incandescent"),
    FLUORESCENT("Fluorescent"),
    DAYLIGHT("Daylight"),
    CLOUDY("Cloudy"),
    TUNGSTEN("Tungsten")
}

enum class FocusMode { AUTO, MANUAL, CONTINUOUS }

enum class ColorFilter(val label: String) {
    NONE("None"),
    WARM("Warm"),
    COOL("Cool"),
    VIVID("Vivid"),
    BW("B&W"),
    VINTAGE("Vintage")
}

enum class GridOverlay(val label: String) {
    NONE("Off"),
    THIRDS("Rule of Thirds"),
    GRID_4X4("4x4 Grid"),
    CENTER("Center Cross")
}
