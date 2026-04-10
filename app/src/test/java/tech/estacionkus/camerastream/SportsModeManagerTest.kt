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

    @Test fun suggestBitrateHighBandwidthReturns1080pBitrate() {
        val bw = manager.suggestBitrate(15000)
        assertEquals(SportsModeManager.BroadcastPreset.SPORTS_1080P.bitrateKbps, bw)
    }

    @Test fun suggestBitrateLowBandwidthReturnsNewsBitrate() {
        val bw = manager.suggestBitrate(1000)
        assertEquals(SportsModeManager.BroadcastPreset.NEWS_LOW_BW.bitrateKbps, bw)
    }

    @Test fun sports1080pFpsIs60() {
        assertEquals(60, SportsModeManager.BroadcastPreset.SPORTS_1080P.fps)
    }

    @Test fun sportsSrtLatencyBelow250msFor5GPreset() {
        assertTrue(SportsModeManager.BroadcastPreset.SPORTS_1080P.srtLatencyMs < 250)
    }

    @Test fun tvShowKeyframeIntervalIs2s() {
        assertEquals(2, SportsModeManager.BroadcastPreset.TV_SHOW.keyframeIntervalSec)
    }

    @Test fun concertHasHighestAudioBitrate() {
        val maxAudio = SportsModeManager.BroadcastPreset.entries.maxOf { it.audioBitrateKbps }
        assertEquals(maxAudio, SportsModeManager.BroadcastPreset.CONCERT.audioBitrateKbps)
    }

    @Test fun adaptiveBitrateUsedWhenMidBandwidth() {
        val bw = manager.suggestBitrate(7000)
        assertEquals(SportsModeManager.BroadcastPreset.SPORTS_720P.bitrateKbps, bw)
    }

    @Test fun newsPresetHasLowestBitrate() {
        val min = SportsModeManager.BroadcastPreset.entries.minOf { it.bitrateKbps }
        assertEquals(SportsModeManager.BroadcastPreset.NEWS_LOW_BW.bitrateKbps, min)
    }
}
