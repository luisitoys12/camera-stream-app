package tech.estacionkus.camerastream.domain.model

data class Scene(
    val id: String,
    val name: String,
    val overlays: List<OverlayItem> = emptyList()
)

data class OverlayItem(
    val id: String,
    val type: OverlayType,
    val uri: String = "",
    val text: String = "",
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 1f,
    val height: Float = 0.15f,
    val alpha: Float = 1f,
    val zOrder: Int = 0
)

enum class OverlayType { IMAGE, GIF, LOWER_THIRD, WATERMARK, COUNTDOWN, SCOREBOARD }
