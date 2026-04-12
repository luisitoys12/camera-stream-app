package tech.estacionkus.camerastream.ui.screens.editor

import android.net.Uri

/** Aspect ratio presets for editor projects */
enum class AspectRatio(val widthRatio: Int, val heightRatio: Int, val label: String) {
    LANDSCAPE(16, 9, "16:9"),
    PORTRAIT(9, 16, "9:16"),
    SQUARE(1, 1, "1:1"),
    SOCIAL(4, 5, "4:5")
}

/** A video clip on the timeline */
data class VideoClip(
    val id: String = java.util.UUID.randomUUID().toString(),
    val uri: Uri,
    val startMs: Long = 0L,
    val endMs: Long = 0L,
    val speed: Float = 1f,
    val filterName: String? = null,
    val filterIntensity: Float = 1f,
    val volume: Float = 1f,
    val originalDurationMs: Long = 0L
) {
    val trimmedDurationMs: Long get() = ((endMs - startMs) / speed).toLong()
}

/** Text overlay on the video */
data class TextLayer(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val startMs: Long = 0L,
    val endMs: Long = 5000L,
    val x: Float = 0.5f,
    val y: Float = 0.5f,
    val fontSize: Float = 24f,
    val color: Int = 0xFFFFFFFF.toInt(),
    val outlineColor: Int = 0xFF000000.toInt(),
    val outlineWidth: Float = 2f,
    val shadowEnabled: Boolean = false,
    val fontFamily: String = "sans-serif",
    val animation: TextAnimation = TextAnimation.NONE,
    val animationDurationMs: Long = 500L
)

enum class TextAnimation(val label: String) {
    NONE("None"),
    FADE_IN("Fade In"),
    FADE_OUT("Fade Out"),
    FADE_IN_OUT("Fade In/Out"),
    SLIDE_LEFT("Slide Left"),
    SLIDE_RIGHT("Slide Right"),
    SLIDE_UP("Slide Up"),
    TYPEWRITER("Typewriter"),
    BOUNCE("Bounce"),
    SCALE_UP("Scale Up")
}

/** Audio layer (background music, voiceover) */
data class AudioLayer(
    val id: String = java.util.UUID.randomUUID().toString(),
    val uri: Uri,
    val name: String = "",
    val startMs: Long = 0L,
    val endMs: Long = 0L,
    val volume: Float = 1f,
    val fadeInMs: Long = 0L,
    val fadeOutMs: Long = 0L,
    val isVoiceover: Boolean = false
)

/** Transition between clips */
data class Transition(
    val type: TransitionType = TransitionType.NONE,
    val durationMs: Long = 500L
)

enum class TransitionType(val label: String) {
    NONE("None"),
    CROSSFADE("Crossfade"),
    DISSOLVE("Dissolve"),
    SLIDE_LEFT("Slide Left"),
    SLIDE_RIGHT("Slide Right"),
    SLIDE_UP("Slide Up"),
    SLIDE_DOWN("Slide Down"),
    ZOOM_IN("Zoom In"),
    ZOOM_OUT("Zoom Out"),
    WIPE("Wipe")
}

/** Export quality presets */
enum class ExportQuality(val label: String, val crf: Int) {
    LOW("Low", 28),
    MEDIUM("Medium", 23),
    HIGH("High", 18),
    MAX("Maximum", 14)
}

/** Export resolution presets */
enum class ExportResolution(val label: String, val width: Int, val height: Int) {
    RES_480P("480p", 854, 480),
    RES_720P("720p", 1280, 720),
    RES_1080P("1080p", 1920, 1080),
    RES_4K("4K", 3840, 2160)
}

/** The complete editor project */
data class EditorProject(
    val id: String = java.util.UUID.randomUUID().toString(),
    val clips: List<VideoClip> = emptyList(),
    val textLayers: List<TextLayer> = emptyList(),
    val audioLayers: List<AudioLayer> = emptyList(),
    val transitions: Map<Int, Transition> = emptyMap(),
    val aspectRatio: AspectRatio = AspectRatio.LANDSCAPE,
    val name: String = "New Project"
) {
    val totalDurationMs: Long
        get() {
            var total = 0L
            clips.forEachIndexed { i, clip ->
                total += clip.trimmedDurationMs
                transitions[i]?.let { total -= it.durationMs / 2 }
            }
            return total.coerceAtLeast(0L)
        }
}

/** Editor tool tabs */
enum class EditorTool(val label: String) {
    TRIM("Trim"),
    TEXT("Text"),
    TRANSITIONS("Transitions"),
    FILTERS("Filters"),
    AUDIO("Audio"),
    SPEED("Speed"),
    TEMPLATES("Templates"),
    EXPORT("Export")
}

/** Template definition */
data class EditorTemplate(
    val id: String,
    val name: String,
    val category: TemplateCategory,
    val aspectRatio: AspectRatio,
    val textLayers: List<TextLayer>,
    val transitionType: TransitionType,
    val suggestedDurationSec: Int,
    val description: String
)

enum class TemplateCategory(val label: String) {
    INTRO("Intro"),
    OUTRO("Outro"),
    SOCIAL("Social"),
    TUTORIAL("Tutorial"),
    GAMING("Gaming"),
    VLOG("Vlog"),
    PRODUCT("Product Review"),
    NEWS("News"),
    EVENT("Event"),
    PROMO("Promo")
}
