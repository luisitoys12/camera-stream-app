package tech.estacionkus.camerastream.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tech.estacionkus.camerastream.domain.model.OverlayItem
import tech.estacionkus.camerastream.domain.model.OverlayType
import tech.estacionkus.camerastream.domain.model.Scene
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SceneManager @Inject constructor() {
    private val _scenes = MutableStateFlow(listOf(
        Scene("main", "Principal"),
        Scene("brb", "BRB", listOf(OverlayItem("brb_lt", OverlayType.LOWER_THIRD, text = "Volvemos pronto..."))),
        Scene("start", "Iniciando", listOf(OverlayItem("cd", OverlayType.COUNTDOWN)))
    ))
    val scenes: StateFlow<List<Scene>> = _scenes.asStateFlow()

    private val _activeSceneId = MutableStateFlow("main")
    val activeSceneId: StateFlow<String> = _activeSceneId.asStateFlow()

    val activeScene get() = _scenes.value.find { it.id == _activeSceneId.value } ?: _scenes.value.first()

    fun switchTo(id: String) { _activeSceneId.value = id }
    fun addScene(name: String) { _scenes.value = _scenes.value + Scene(UUID.randomUUID().toString(), name) }
    fun deleteScene(id: String) {
        if (id == "main") return
        _scenes.value = _scenes.value.filter { it.id != id }
        if (_activeSceneId.value == id) _activeSceneId.value = "main"
    }
    fun addOverlay(sceneId: String, overlay: OverlayItem) {
        _scenes.value = _scenes.value.map { if (it.id == sceneId) it.copy(overlays = it.overlays + overlay) else it }
    }
    fun removeOverlay(sceneId: String, overlayId: String) {
        _scenes.value = _scenes.value.map { if (it.id == sceneId) it.copy(overlays = it.overlays.filter { o -> o.id != overlayId }) else it }
    }
}
