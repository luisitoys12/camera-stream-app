package tech.estacionkus.camerastream.data.media

import android.net.Uri

enum class MediaAssetType {
    IMAGE,   // logos, lower thirds estáticos, fondos
    VIDEO,   // intro, outro, loop background, lower thirds animados
    GIF
}

enum class OverlayCategory {
    INTRO,
    OUTRO,
    LOWER_THIRD,
    LOGO,
    BACKGROUND,
    OTHER
}

data class MediaAsset(
    val id: String,
    val uri: Uri,
    val name: String,
    val type: MediaAssetType,
    val category: OverlayCategory = OverlayCategory.OTHER,
    val durationMs: Long? = null,  // null for images
    val sizeBytes: Long = 0,
    val addedAt: Long = System.currentTimeMillis()
)
