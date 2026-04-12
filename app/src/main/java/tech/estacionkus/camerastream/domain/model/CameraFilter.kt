package tech.estacionkus.camerastream.domain.model

import android.graphics.ColorMatrix

enum class FilterCategory(val displayName: String) {
    BEAUTY("Beauty"),
    COLOR("Color"),
    MOOD("Mood"),
    FUN("Fun"),
    PRO("Pro")
}

enum class FilterTier { FREE, PRO, AGENCY }

data class CameraFilter(
    val id: String,
    val name: String,
    val category: FilterCategory,
    val tier: FilterTier,
    val colorMatrix: ColorMatrix
)

object CameraFilters {
    private fun matrix(values: FloatArray) = ColorMatrix(values)

    val IDENTITY = matrix(floatArrayOf(
        1f, 0f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f, 0f,
        0f, 0f, 1f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ))

    // --- COLOR (8 filters) --- 3 FREE, 5 PRO
    val WARM = matrix(floatArrayOf(1.2f, 0f, 0f, 0f, 10f, 0f, 1.1f, 0f, 0f, 5f, 0f, 0f, 0.9f, 0f, -10f, 0f, 0f, 0f, 1f, 0f))
    val COOL = matrix(floatArrayOf(0.9f, 0f, 0f, 0f, -10f, 0f, 1.0f, 0f, 0f, 0f, 0f, 0f, 1.2f, 0f, 10f, 0f, 0f, 0f, 1f, 0f))
    val VIVID = matrix(floatArrayOf(1.3f, 0f, 0f, 0f, 0f, 0f, 1.3f, 0f, 0f, 0f, 0f, 0f, 1.3f, 0f, 0f, 0f, 0f, 0f, 1f, 0f))
    val BW = matrix(floatArrayOf(0.33f, 0.59f, 0.11f, 0f, 0f, 0.33f, 0.59f, 0.11f, 0f, 0f, 0.33f, 0.59f, 0.11f, 0f, 0f, 0f, 0f, 0f, 1f, 0f))
    val SEPIA = matrix(floatArrayOf(0.393f, 0.769f, 0.189f, 0f, 0f, 0.349f, 0.686f, 0.168f, 0f, 0f, 0.272f, 0.534f, 0.131f, 0f, 0f, 0f, 0f, 0f, 1f, 0f))
    val VINTAGE = matrix(floatArrayOf(0.9f, 0.5f, 0.1f, 0f, 20f, 0.3f, 0.8f, 0.1f, 0f, 10f, 0.2f, 0.3f, 0.5f, 0f, -10f, 0f, 0f, 0f, 1f, 0f))
    val CINEMATIC = matrix(floatArrayOf(1.1f, 0f, 0f, 0f, -15f, 0f, 1.05f, 0f, 0f, -10f, 0f, 0f, 1.2f, 0f, -5f, 0f, 0f, 0f, 1f, 0f))
    val TEAL_ORANGE = matrix(floatArrayOf(1.2f, 0f, 0f, 0f, 10f, 0f, 1.0f, 0.1f, 0f, -5f, -0.1f, 0.1f, 1.3f, 0f, 10f, 0f, 0f, 0f, 1f, 0f))

    // --- BEAUTY (5 filters) --- all PRO
    val BRIGHTEN = matrix(floatArrayOf(1.1f, 0f, 0f, 0f, 25f, 0f, 1.1f, 0f, 0f, 25f, 0f, 0f, 1.1f, 0f, 25f, 0f, 0f, 0f, 1f, 0f))
    val SMOOTH_SKIN = matrix(floatArrayOf(1.05f, 0.05f, 0.05f, 0f, 15f, 0.05f, 1.05f, 0.05f, 0f, 15f, 0.05f, 0.05f, 1.0f, 0f, 10f, 0f, 0f, 0f, 1f, 0f))
    val SLIM_FACE = matrix(floatArrayOf(1.02f, 0f, 0f, 0f, 5f, 0f, 1.02f, 0f, 0f, 5f, 0f, 0f, 1.02f, 0f, 5f, 0f, 0f, 0f, 1f, 0f))
    val BIG_EYES = matrix(floatArrayOf(1.05f, 0f, 0f, 0f, 10f, 0f, 1.05f, 0f, 0f, 10f, 0f, 0f, 1.05f, 0f, 10f, 0f, 0f, 0f, 1f, 0f))
    val TEETH_WHITE = matrix(floatArrayOf(1.0f, 0.05f, 0f, 0f, 15f, 0f, 1.0f, 0.05f, 0f, 15f, 0f, 0f, 1.0f, 0f, 10f, 0f, 0f, 0f, 1f, 0f))

    // --- MOOD (6 filters) --- all PRO
    val DREAMY = matrix(floatArrayOf(1.1f, 0.1f, 0.1f, 0f, 30f, 0.1f, 1.1f, 0.1f, 0f, 30f, 0.1f, 0.1f, 1.1f, 0f, 30f, 0f, 0f, 0f, 0.9f, 0f))
    val DARK = matrix(floatArrayOf(0.8f, 0f, 0f, 0f, -30f, 0f, 0.8f, 0f, 0f, -30f, 0f, 0f, 0.8f, 0f, -30f, 0f, 0f, 0f, 1f, 0f))
    val GOLDEN_HOUR = matrix(floatArrayOf(1.2f, 0.1f, 0f, 0f, 20f, 0f, 1.1f, 0f, 0f, 15f, 0f, 0f, 0.8f, 0f, -20f, 0f, 0f, 0f, 1f, 0f))
    val NEON = matrix(floatArrayOf(1.4f, 0f, 0f, 0f, 20f, 0f, 1.4f, 0f, 0f, 20f, 0f, 0f, 1.4f, 0f, 20f, 0f, 0f, 0f, 1f, 0f))
    val RETRO = matrix(floatArrayOf(0.9f, 0.2f, 0f, 0f, 15f, 0.1f, 0.8f, 0f, 0f, 5f, 0f, 0f, 0.6f, 0f, -10f, 0f, 0f, 0f, 1f, 0f))
    val FILM_GRAIN = matrix(floatArrayOf(1.0f, 0.1f, 0f, 0f, 5f, 0f, 0.95f, 0.1f, 0f, 5f, 0.05f, 0f, 0.9f, 0f, 0f, 0f, 0f, 0f, 1f, 0f))

    // --- FUN (6 filters) --- all PRO
    val CARTOON = matrix(floatArrayOf(1.5f, -0.3f, -0.2f, 0f, 20f, -0.3f, 1.5f, -0.2f, 0f, 20f, -0.2f, -0.3f, 1.5f, 0f, 20f, 0f, 0f, 0f, 1f, 0f))
    val SKETCH = matrix(floatArrayOf(0.2f, 0.6f, 0.2f, 0f, 30f, 0.2f, 0.6f, 0.2f, 0f, 30f, 0.2f, 0.6f, 0.2f, 0f, 30f, 0f, 0f, 0f, 1f, 0f))
    val PIXELATE = matrix(floatArrayOf(1.0f, 0f, 0f, 0f, 0f, 0f, 1.0f, 0f, 0f, 0f, 0f, 0f, 1.0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f))
    val MIRROR = matrix(floatArrayOf(1.0f, 0f, 0f, 0f, 0f, 0f, 1.0f, 0f, 0f, 0f, 0f, 0f, 1.0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f))
    val FISHEYE = matrix(floatArrayOf(1.1f, 0f, 0f, 0f, 0f, 0f, 1.1f, 0f, 0f, 0f, 0f, 0f, 1.1f, 0f, 0f, 0f, 0f, 0f, 1f, 0f))
    val GLITCH = matrix(floatArrayOf(1.0f, 0.3f, 0f, 0f, 0f, 0f, 1.0f, 0f, 0f, 20f, 0f, 0f, 1.0f, 0.3f, 0f, 0f, 0f, 0f, 1f, 0f))

    // --- PROFESSIONAL (5 filters) --- all AGENCY
    val NEWS_STUDIO = matrix(floatArrayOf(1.05f, 0f, 0f, 0f, 10f, 0f, 1.05f, 0f, 0f, 10f, 0f, 0f, 1.1f, 0f, 5f, 0f, 0f, 0f, 1f, 0f))
    val INTERVIEW = matrix(floatArrayOf(1.1f, 0.05f, 0f, 0f, 15f, 0f, 1.05f, 0.05f, 0f, 10f, 0f, 0f, 1.0f, 0f, 5f, 0f, 0f, 0f, 1f, 0f))
    val LOW_LIGHT = matrix(floatArrayOf(1.3f, 0f, 0f, 0f, 40f, 0f, 1.3f, 0f, 0f, 40f, 0f, 0f, 1.3f, 0f, 40f, 0f, 0f, 0f, 1f, 0f))
    val HDR_ENHANCE = matrix(floatArrayOf(1.2f, 0f, 0f, 0f, -10f, 0f, 1.2f, 0f, 0f, -10f, 0f, 0f, 1.2f, 0f, -10f, 0f, 0f, 0f, 1f, 0f))
    val CUSTOM_LUT = matrix(floatArrayOf(1.15f, 0.05f, 0f, 0f, 0f, 0f, 1.1f, 0.05f, 0f, 0f, 0.05f, 0f, 1.15f, 0f, 0f, 0f, 0f, 0f, 1f, 0f))

    val allFilters = listOf(
        // None
        CameraFilter("none", "None", FilterCategory.COLOR, FilterTier.FREE, IDENTITY),
        // Color - 3 free
        CameraFilter("warm", "Warm", FilterCategory.COLOR, FilterTier.FREE, WARM),
        CameraFilter("cool", "Cool", FilterCategory.COLOR, FilterTier.FREE, COOL),
        CameraFilter("vivid", "Vivid", FilterCategory.COLOR, FilterTier.FREE, VIVID),
        CameraFilter("bw", "B&W", FilterCategory.COLOR, FilterTier.PRO, BW),
        CameraFilter("sepia", "Sepia", FilterCategory.COLOR, FilterTier.PRO, SEPIA),
        CameraFilter("vintage", "Vintage", FilterCategory.COLOR, FilterTier.PRO, VINTAGE),
        CameraFilter("cinematic", "Cinematic", FilterCategory.COLOR, FilterTier.PRO, CINEMATIC),
        CameraFilter("teal_orange", "Teal&Orange", FilterCategory.COLOR, FilterTier.PRO, TEAL_ORANGE),
        // Beauty - all PRO
        CameraFilter("brighten", "Brighten", FilterCategory.BEAUTY, FilterTier.PRO, BRIGHTEN),
        CameraFilter("smooth_skin", "Smooth Skin", FilterCategory.BEAUTY, FilterTier.PRO, SMOOTH_SKIN),
        CameraFilter("slim_face", "Slim Face", FilterCategory.BEAUTY, FilterTier.PRO, SLIM_FACE),
        CameraFilter("big_eyes", "Big Eyes", FilterCategory.BEAUTY, FilterTier.PRO, BIG_EYES),
        CameraFilter("teeth_white", "Teeth White", FilterCategory.BEAUTY, FilterTier.PRO, TEETH_WHITE),
        // Mood - all PRO
        CameraFilter("dreamy", "Dreamy", FilterCategory.MOOD, FilterTier.PRO, DREAMY),
        CameraFilter("dark", "Dark", FilterCategory.MOOD, FilterTier.PRO, DARK),
        CameraFilter("golden_hour", "Golden Hour", FilterCategory.MOOD, FilterTier.PRO, GOLDEN_HOUR),
        CameraFilter("neon", "Neon", FilterCategory.MOOD, FilterTier.PRO, NEON),
        CameraFilter("retro", "Retro", FilterCategory.MOOD, FilterTier.PRO, RETRO),
        CameraFilter("film_grain", "Film Grain", FilterCategory.MOOD, FilterTier.PRO, FILM_GRAIN),
        // Fun - all PRO
        CameraFilter("cartoon", "Cartoon", FilterCategory.FUN, FilterTier.PRO, CARTOON),
        CameraFilter("sketch", "Sketch", FilterCategory.FUN, FilterTier.PRO, SKETCH),
        CameraFilter("pixelate", "Pixelate", FilterCategory.FUN, FilterTier.PRO, PIXELATE),
        CameraFilter("mirror", "Mirror", FilterCategory.FUN, FilterTier.PRO, MIRROR),
        CameraFilter("fisheye", "Fisheye", FilterCategory.FUN, FilterTier.PRO, FISHEYE),
        CameraFilter("glitch", "Glitch", FilterCategory.FUN, FilterTier.PRO, GLITCH),
        // Pro - all AGENCY
        CameraFilter("news_studio", "News Studio", FilterCategory.PRO, FilterTier.AGENCY, NEWS_STUDIO),
        CameraFilter("interview", "Interview", FilterCategory.PRO, FilterTier.AGENCY, INTERVIEW),
        CameraFilter("low_light", "Low Light", FilterCategory.PRO, FilterTier.AGENCY, LOW_LIGHT),
        CameraFilter("hdr_enhance", "HDR Enhance", FilterCategory.PRO, FilterTier.AGENCY, HDR_ENHANCE),
        CameraFilter("custom_lut", "Custom LUT", FilterCategory.PRO, FilterTier.AGENCY, CUSTOM_LUT)
    )

    fun getByCategory(category: FilterCategory) = allFilters.filter { it.category == category }
}
