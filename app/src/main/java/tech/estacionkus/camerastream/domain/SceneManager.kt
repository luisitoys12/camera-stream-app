package tech.estacionkus.camerastream.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tech.estacionkus.camerastream.domain.model.CameraSource
import tech.estacionkus.camerastream.domain.model.OverlayItem
import tech.estacionkus.camerastream.domain.model.OverlayType
import tech.estacionkus.camerastream.domain.model.Scene
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SceneManager @Inject constructor() {
    private val _scenes = MutableStateFlow(
        listOf(
            Scene(id = "main", name = "Principal"),
            Scene(id = "brb", name = "BRB", overlays = listOf(
                OverlayItem(id = "brb_lt", type = OverlayType.LOWER_THIRD, text = "Volvemos pronto...")
            )),
            Scene(id = "starting", name = "Iniciando", overlays = listOf(
                OverlayItem(id = "cd", type = OverlayType.COUNTDOWN)
            ))
        )
    )
    val scenes: StateFlow<List<Scene>> = _scenes.asStateFlow()

    private val _activeSceneId = MutableStateFlow("main")
    val activeSceneId: StateFlow<String> = _activeSceneId.asStateFlow()

    val activeScene get() = _scenes.value.find { it.id == _activeSceneId.value } ?: _scenes.value.first()

    fun switchTo(sceneId: String) { _activeSceneId.value = sceneId }

    fun addScene(name: String) {
        _scenes.value = _scenes.value + Scene(id = UUID.randomUUID().toString(), name = name)
    }

    fun deleteScene(id: String) {
        if (id == "main") return
        _scenes.value = _scenes.value.filter { it.id != id }
        if (_activeSceneId.value == id) _activeSceneId.value = "main"
    }

    fun addOverlayToScene(sceneId: String, overlay: OverlayItem) {
        _scenes.value = _scenes.value.map { scene ->
            if (scene.id == sceneId) scene.copy(overlays = scene.overlays + overlay) else scene
        }
    }

    fun removeOverlayFromScene(sceneId: String, overlayId: String) {
        _scenes.value = _scenes.value.map { scene ->
            if (scene.id == sceneId) scene.copy(overlays = scene.overlays.filter { it.id != overlayId }) else scene
        }
    }
}
