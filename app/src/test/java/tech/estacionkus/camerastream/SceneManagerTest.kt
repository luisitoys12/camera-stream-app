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

    @Test fun `default scene is main`() {
        assertEquals("main", manager.activeSceneId.value)
    }

    @Test fun `initial scenes include main brb and start`() {
        val ids = manager.scenes.value.map { it.id }
        assertTrue(ids.containsAll(listOf("main", "brb", "start")))
    }

    @Test fun `addScene increases count`() {
        val before = manager.scenes.value.size
        manager.addScene("Goles")
        assertEquals(before + 1, manager.scenes.value.size)
    }

    @Test fun `switchTo changes activeSceneId`() {
        manager.switchTo("brb")
        assertEquals("brb", manager.activeSceneId.value)
    }

    @Test fun `deleteScene main is ignored`() {
        manager.deleteScene("main")
        assertTrue(manager.scenes.value.any { it.id == "main" })
    }

    @Test fun `deleteScene non-main removes it`() {
        manager.deleteScene("brb")
        assertFalse(manager.scenes.value.any { it.id == "brb" })
    }

    @Test fun `addOverlay adds to scene`() {
        val overlay = OverlayItem("test_overlay", OverlayType.LOWER_THIRD, text = "Gol de Checo!")
        manager.addOverlay("main", overlay)
        val scene = manager.scenes.value.find { it.id == "main" }!!
        assertTrue(scene.overlays.any { it.id == "test_overlay" })
    }

    @Test fun `removeOverlay removes from scene`() {
        val overlay = OverlayItem("rem_overlay", OverlayType.WATERMARK)
        manager.addOverlay("main", overlay)
        manager.removeOverlay("main", "rem_overlay")
        val scene = manager.scenes.value.find { it.id == "main" }!!
        assertFalse(scene.overlays.any { it.id == "rem_overlay" })
    }
}
