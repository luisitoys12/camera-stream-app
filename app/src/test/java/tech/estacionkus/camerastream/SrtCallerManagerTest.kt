package tech.estacionkus.camerastream

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import tech.estacionkus.camerastream.streaming.SrtCallerManager
import tech.estacionkus.camerastream.streaming.StreamState

class SrtCallerManagerTest {
    private lateinit var manager: SrtCallerManager

    @Before fun setup() { manager = SrtCallerManager() }

    @Test fun initialStateIsIdle() = runTest {
        assertEquals(StreamState.IDLE, manager.state.value)
    }

    @Test fun recommendedLatencyAtLeast120ms() {
        assertTrue(manager.recommendedLatency() >= 120)
    }

    @Test fun recommendedLatencyWithHighRttReturnsRttX4() {
        // Access private MutableStateFlow via reflection
        val field = SrtCallerManager::class.java.getDeclaredField("_rttMs")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val flow = field.get(manager) as MutableStateFlow<Int>
        flow.value = 50
        assertEquals(200, manager.recommendedLatency())
    }

    @Test fun disconnectWhenNotConnectedDoesNotCrash() {
        // Should complete without exception
        try {
            manager.disconnect()
        } catch (e: Exception) {
            fail("disconnect() threw: ${e.message}")
        }
    }

    @Test fun initialRttIsZero() {
        assertEquals(0, manager.rttMs.value)
    }

    @Test fun initialBitrateIsZero() {
        assertEquals(0, manager.bitrateKbps.value)
    }
}
