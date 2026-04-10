package tech.estacionkus.camerastream

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import tech.estacionkus.camerastream.domain.SceneManager
import tech.estacionkus.camerastream.domain.model.OverlayItem
import tech.estacionkus.camerastream.domain.model.OverlayType

class SceneManagerTest {
    private lateinit var manager: SceneManager

    @Before fun setup() { manager = SceneManager() }

    @Test fun defaultSceneIsMain() { assertEquals("main", manager.activeSceneId.value) }

    @Test fun initialScenesHaveThreeEntries() {
        val ids = manager.scenes.value.map { it.id }
        assertTrue(ids.containsAll(listOf("main", "brb", "start")))
    }

    @Test fun addSceneIncreasesCount() {
        val before = manager.scenes.value.size
        manager.addScene("Goles")
        assertEquals(before + 1, manager.scenes.value.size)
    }

    @Test fun switchToChangesActive() {
        manager.switchTo("brb")
        assertEquals("brb", manager.activeSceneId.value)
    }

    @Test fun cannotDeleteMain() {
        manager.deleteScene("main")
        assertTrue(manager.scenes.value.any { it.id == "main" })
    }

    @Test fun deleteNonMainRemovesIt() {
        manager.deleteScene("brb")
        assertFalse(manager.scenes.value.any { it.id == "brb" })
    }

    @Test fun deleteActiveSceneFallsBackToMain() {
        manager.switchTo("brb")
        manager.deleteScene("brb")
        assertEquals("main", manager.activeSceneId.value)
    }

    @Test fun addOverlayAppearsInScene() {
        manager.addOverlay("main", OverlayItem("o1", OverlayType.LOWER_THIRD, text = "Gol!"))
        assertTrue(manager.scenes.value.find { it.id == "main" }!!.overlays.any { it.id == "o1" })
    }

    @Test fun removeOverlayDisappearsFromScene() {
        manager.addOverlay("main", OverlayItem("o2", OverlayType.WATERMARK))
        manager.removeOverlay("main", "o2")
        assertFalse(manager.scenes.value.find { it.id == "main" }!!.overlays.any { it.id == "o2" })
    }
}
