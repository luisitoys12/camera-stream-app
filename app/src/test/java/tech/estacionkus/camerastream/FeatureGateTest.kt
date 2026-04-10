package tech.estacionkus.camerastream

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import tech.estacionkus.camerastream.domain.FeatureGate
import tech.estacionkus.camerastream.domain.model.PlanFeatures

class FeatureGateTest {
    private lateinit var gate: FeatureGate

    @Before fun setup() { gate = FeatureGate() }

    @Test fun `free plan cannot multistream`() {
        assertFalse(gate.canMultiStream())
    }

    @Test fun `free plan cannot SRT`() {
        assertFalse(gate.canSrt())
    }

    @Test fun `pro plan can do everything`() {
        gate.upgrade(PlanFeatures.PRO)
        assertTrue(gate.canMultiStream())
        assertTrue(gate.canSrt())
        assertTrue(gate.canSrtServer())
        assertTrue(gate.canTunnel())
        assertTrue(gate.canScenes())
        assertTrue(gate.canManualCamera())
        assertTrue(gate.can1080p())
    }

    @Test fun `check returns error message for locked features`() {
        assertNotNull(gate.check("srt"))
        assertNotNull(gate.check("scenes"))
    }

    @Test fun `check returns null for unlocked features after upgrade`() {
        gate.upgrade(PlanFeatures.PRO)
        assertNull(gate.check("srt"))
        assertNull(gate.check("scenes"))
    }
}
