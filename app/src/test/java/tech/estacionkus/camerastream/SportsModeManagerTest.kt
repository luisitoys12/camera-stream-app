package tech.estacionkus.camerastream

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tech.estacionkus.camerastream.streaming.SportsModeManager

class SportsModeManagerTest {
    private lateinit var manager: SportsModeManager

    @Before fun setup() { manager = SportsModeManager() }

    @Test fun defaultPresetIsSports720p() {
        assertEquals(SportsModeManager.BroadcastPreset.SPORTS_720P, manager.activePreset)
    }

    @Test fun applyChangesActivePreset() {
        manager.apply(SportsModeManager.BroadcastPreset.SPORTS_1080P)
        assertEquals(SportsModeManager.BroadcastPreset.SPORTS_1080P, manager.activePreset)
    }

    @Test fun suggestBitrateHighBandwidth() {
        assertEquals(SportsModeManager.BroadcastPreset.SPORTS_1080P.bitrateKbps, manager.suggestBitrate(15000))
    }

    @Test fun suggestBitrateMidBandwidth() {
        assertEquals(SportsModeManager.BroadcastPreset.SPORTS_720P.bitrateKbps, manager.suggestBitrate(7000))
    }

    @Test fun suggestBitrateLowBandwidth() {
        assertEquals(SportsModeManager.BroadcastPreset.NEWS_LOW_BW.bitrateKbps, manager.suggestBitrate(1000))
    }

    @Test fun sports1080pFpsIs60() {
        assertEquals(60, SportsModeManager.BroadcastPreset.SPORTS_1080P.fps)
    }

    @Test fun srtLatencyBelow250msFor5G() {
        assertTrue(SportsModeManager.BroadcastPreset.SPORTS_1080P.srtLatencyMs < 250)
    }

    @Test fun concertHasHighestAudioBitrate() {
        val max = SportsModeManager.BroadcastPreset.entries.maxOf { it.audioBitrateKbps }
        assertEquals(max, SportsModeManager.BroadcastPreset.CONCERT.audioBitrateKbps)
    }

    @Test fun newsHasLowestBitrate() {
        val min = SportsModeManager.BroadcastPreset.entries.minOf { it.bitrateKbps }
        assertEquals(SportsModeManager.BroadcastPreset.NEWS_LOW_BW.bitrateKbps, min)
    }
}
