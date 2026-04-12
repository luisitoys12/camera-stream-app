package tech.estacionkus.camerastream.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tech.estacionkus.camerastream.domain.model.OverlayItem
import tech.estacionkus.camerastream.domain.model.OverlayType
import tech.estacionkus.camerastream.domain.model.Scene
import tech.estacionkus.camerastream.domain.model.TransitionType
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SceneManager @Inject constructor() {
    private val _scenes = MutableStateFlow(listOf(
        Scene("main", "Full Camera", isDefault = true, thumbnailColor = 0xFF1A1A2E),
        Scene("cam_overlays", "Cam + Overlays", thumbnailColor = 0xFF2D1B69),
        Scene("brb", "Be Right Back", listOf(
            OverlayItem("brb_lt", OverlayType.LOWER_THIRD, text = "Be right back...")
        ), thumbnailColor = 0xFF6B1A1A),
        Scene("starting", "Starting Soon", listOf(
            OverlayItem("start_cd", OverlayType.COUNTDOWN),
            OverlayItem("start_text", OverlayType.TEXT, text = "Starting Soon!", fontSize = 32f)
        ), thumbnailColor = 0xFF1A4A1A),
        Scene("custom", "Custom", thumbnailColor = 0xFF1A1A4A)
    ))
    val scenes: StateFlow<List<Scene>> = _scenes.asStateFlow()

    private val _activeSceneId = MutableStateFlow("main")
    val activeSceneId: StateFlow<String> = _activeSceneId.asStateFlow()

    private val _transitionType = MutableStateFlow(TransitionType.CUT)
    val transitionType: StateFlow<TransitionType> = _transitionType.asStateFlow()

    private val _isTransitioning = MutableStateFlow(false)
    val isTransitioning: StateFlow<Boolean> = _isTransitioning.asStateFlow()

    val activeScene get() = _scenes.value.find { it.id == _activeSceneId.value } ?: _scenes.value.first()

    fun setTransitionType(type: TransitionType) { _transitionType.value = type }

    fun switchTo(id: String) {
        if (id == _activeSceneId.value) return
        _isTransitioning.value = true
        _activeSceneId.value = id
        // Transition animation handled by UI layer
        _isTransitioning.value = false
    }

    fun addScene(name: String): String {
        val id = UUID.randomUUID().toString()
        _scenes.value = _scenes.value + Scene(id, name)
        return id
    }

    fun deleteScene(id: String) {
        if (_scenes.value.find { it.id == id }?.isDefault == true) return
        _scenes.value = _scenes.value.filter { it.id != id }
        if (_activeSceneId.value == id) _activeSceneId.value = "main"
    }

    fun renameScene(id: String, name: String) {
        _scenes.value = _scenes.value.map { if (it.id == id) it.copy(name = name) else it }
    }

    fun addOverlay(sceneId: String, overlay: OverlayItem) {
        _scenes.value = _scenes.value.map {
            if (it.id == sceneId) it.copy(overlays = it.overlays + overlay) else it
        }
    }

    fun removeOverlay(sceneId: String, overlayId: String) {
        _scenes.value = _scenes.value.map {
            if (it.id == sceneId) it.copy(overlays = it.overlays.filter { o -> o.id != overlayId }) else it
        }
    }

    fun updateOverlay(sceneId: String, overlay: OverlayItem) {
        _scenes.value = _scenes.value.map { scene ->
            if (scene.id == sceneId) {
                scene.copy(overlays = scene.overlays.map { if (it.id == overlay.id) overlay else it })
            } else scene
        }
    }

    fun duplicateScene(sceneId: String) {
        val source = _scenes.value.find { it.id == sceneId } ?: return
        val newId = UUID.randomUUID().toString()
        val newOverlays = source.overlays.map { it.copy(id = UUID.randomUUID().toString()) }
        _scenes.value = _scenes.value + source.copy(id = newId, name = "${source.name} (Copy)", overlays = newOverlays, isDefault = false)
    }
}
