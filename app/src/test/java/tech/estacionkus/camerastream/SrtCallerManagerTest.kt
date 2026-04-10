package tech.estacionkus.camerastream

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import tech.estacionkus.camerastream.streaming.SrtCallerManager
import tech.estacionkus.camerastream.streaming.StreamState

class SrtCallerManagerTest {
    private lateinit var manager: SrtCallerManager

    @Before fun setup() { manager = SrtCallerManager() }

    @Test fun `initial state is IDLE`() = runTest {
        manager.state.test {
            assertEquals(StreamState.IDLE, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun `recommendedLatency returns at least 120ms`() {
        assertTrue(manager.recommendedLatency() >= 120)
    }

    @Test fun `recommendedLatency with high RTT returns rtt x4`() {
        // Simulate 50ms RTT via reflection
        val field = manager.javaClass.getDeclaredField("_rttMs")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val flow = field.get(manager) as kotlinx.coroutines.flow.MutableStateFlow<Int>
        flow.value = 50
        assertEquals(200, manager.recommendedLatency())
    }

    @Test fun `disconnect does not throw when not connected`() {
        assertDoesNotThrow { manager.disconnect() }
    }
}
