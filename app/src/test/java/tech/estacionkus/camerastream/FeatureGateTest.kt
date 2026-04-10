package tech.estacionkus.camerastream

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import tech.estacionkus.camerastream.domain.FeatureGate
import tech.estacionkus.camerastream.domain.model.PlanFeatures

class FeatureGateTest {
    private lateinit var gate: FeatureGate

    @Before fun setup() { gate = FeatureGate() }

    @Test fun freePlanCannotMultistream() {
        assertFalse(gate.canMultiStream())
    }

    @Test fun freePlanCannotSrt() {
        assertFalse(gate.canSrt())
    }

    @Test fun freePlanCannotSrtServer() {
        assertFalse(gate.canSrtServer())
    }

    @Test fun proPlanCanDoEverything() {
        gate.upgrade(PlanFeatures.PRO)
        assertTrue(gate.canMultiStream())
        assertTrue(gate.canSrt())
        assertTrue(gate.canSrtServer())
        assertTrue(gate.canTunnel())
        assertTrue(gate.canScenes())
        assertTrue(gate.canManualCamera())
        assertTrue(gate.can1080p())
    }

    @Test fun checkReturnsErrorMessageForLockedFeature() {
        assertNotNull(gate.check("srt"))
        assertNotNull(gate.check("scenes"))
    }

    @Test fun checkReturnsNullForUnlockedFeaturesAfterUpgrade() {
        gate.upgrade(PlanFeatures.PRO)
        assertNull(gate.check("srt"))
        assertNull(gate.check("scenes"))
        assertNull(gate.check("manual_cam"))
    }

    @Test fun unknownFeatureReturnsNull() {
        assertNull(gate.check("unknown_feature_xyz"))
    }
}
