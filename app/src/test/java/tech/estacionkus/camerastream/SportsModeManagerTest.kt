package tech.estacionkus.camerastream

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tech.estacionkus.camerastream.streaming.SportsModeManager

class SportsModeManagerTest {

    private lateinit var manager: SportsModeManager

    @Before fun setup() { manager = SportsModeManager() }

    @Test fun `default preset is SPORTS_720P`() {
        assertEquals(SportsModeManager.BroadcastPreset.SPORTS_720P, manager.activePreset)
    }

    @Test fun `apply changes active preset`() {
        manager.apply(SportsModeManager.BroadcastPreset.SPORTS_1080P)
        assertEquals(SportsModeManager.BroadcastPreset.SPORTS_1080P, manager.activePreset)
    }

    @Test fun `suggestBitrate high bandwidth returns 1080p bitrate`() {
        val bw = manager.suggestBitrate(15000)
        assertEquals(SportsModeManager.BroadcastPreset.SPORTS_1080P.bitrateKbps, bw)
    }

    @Test fun `suggestBitrate low bandwidth returns news bitrate`() {
        val bw = manager.suggestBitrate(1000)
        assertEquals(SportsModeManager.BroadcastPreset.NEWS_LOW_BW.bitrateKbps, bw)
    }

    @Test fun `sports 1080p fps is 60`() {
        assertTrue(SportsModeManager.BroadcastPreset.SPORTS_1080P.fps == 60)
    }

    @Test fun `sports SRT latency is below 250ms for 5G preset`() {
        assertTrue(SportsModeManager.BroadcastPreset.SPORTS_1080P.srtLatencyMs < 250)
    }

    @Test fun `tv show keyframe interval is 2s`() {
        assertEquals(2, SportsModeManager.BroadcastPreset.TV_SHOW.keyframeIntervalSec)
    }

    @Test fun `concert has highest audio bitrate`() {
        val maxAudio = SportsModeManager.BroadcastPreset.entries.maxOf { it.audioBitrateKbps }
        assertEquals(maxAudio, SportsModeManager.BroadcastPreset.CONCERT.audioBitrateKbps)
    }
}
