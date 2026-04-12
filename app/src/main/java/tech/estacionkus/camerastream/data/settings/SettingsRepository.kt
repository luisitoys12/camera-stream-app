package tech.estacionkus.camerastream.data.settings

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import tech.estacionkus.camerastream.domain.model.StreamTarget
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("cs_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val store = context.dataStore

    companion object {
        val KEY_RTMP_URL = stringPreferencesKey("rtmp_url")
        val KEY_STREAM_KEY = stringPreferencesKey("stream_key")
        val KEY_PLATFORM = stringPreferencesKey("platform")
        val KEY_BITRATE = intPreferencesKey("bitrate_kbps")
        val KEY_FPS = intPreferencesKey("fps")
        val KEY_RESOLUTION = stringPreferencesKey("resolution")
        val KEY_AUDIO_BITRATE = intPreferencesKey("audio_bitrate_kbps")
        val KEY_TARGETS = stringPreferencesKey("stream_targets")
        val KEY_OVERLAY_URI = stringPreferencesKey("overlay_uri")
        val KEY_OVERLAY_ENABLED = booleanPreferencesKey("overlay_enabled")
        val KEY_CHAT_PLATFORM = stringPreferencesKey("chat_platform")
        val KEY_CHAT_CHANNEL = stringPreferencesKey("chat_channel")
    }

    val rtmpUrl = store.data.map { it[KEY_RTMP_URL] ?: "" }
    val streamKey = store.data.map { it[KEY_STREAM_KEY] ?: "" }
    val platform = store.data.map { it[KEY_PLATFORM] ?: "YouTube" }
    val bitrate = store.data.map { it[KEY_BITRATE] ?: 2500 }
    val fps = store.data.map { it[KEY_FPS] ?: 30 }
    val resolution = store.data.map { it[KEY_RESOLUTION] ?: "720p" }
    val audioBitrate = store.data.map { it[KEY_AUDIO_BITRATE] ?: 128 }
    val overlayUri = store.data.map { it[KEY_OVERLAY_URI] ?: "" }
    val overlayEnabled = store.data.map { it[KEY_OVERLAY_ENABLED] ?: false }
    val chatPlatform = store.data.map { it[KEY_CHAT_PLATFORM] ?: "" }
    val chatChannel = store.data.map { it[KEY_CHAT_CHANNEL] ?: "" }

    val streamTargets = store.data.map { prefs ->
        prefs[KEY_TARGETS]?.let {
            try { Json.decodeFromString<List<StreamTarget>>(it) } catch (e: Exception) { emptyList() }
        } ?: emptyList()
    }

    suspend fun setRtmpUrl(v: String) = store.edit { it[KEY_RTMP_URL] = v }
    suspend fun setStreamKey(v: String) = store.edit { it[KEY_STREAM_KEY] = v }
    suspend fun setPlatform(v: String) = store.edit { it[KEY_PLATFORM] = v }
    suspend fun setBitrate(v: Int) = store.edit { it[KEY_BITRATE] = v }
    suspend fun setFps(v: Int) = store.edit { it[KEY_FPS] = v }
    suspend fun setResolution(v: String) = store.edit { it[KEY_RESOLUTION] = v }
    suspend fun setAudioBitrate(v: Int) = store.edit { it[KEY_AUDIO_BITRATE] = v }
    suspend fun setOverlayUri(v: String) = store.edit { it[KEY_OVERLAY_URI] = v }
    suspend fun setOverlayEnabled(v: Boolean) = store.edit { it[KEY_OVERLAY_ENABLED] = v }
    suspend fun setChatPlatform(v: String) = store.edit { it[KEY_CHAT_PLATFORM] = v }
    suspend fun setChatChannel(v: String) = store.edit { it[KEY_CHAT_CHANNEL] = v }
    suspend fun saveTargets(targets: List<StreamTarget>) =
        store.edit { it[KEY_TARGETS] = Json.encodeToString(targets) }
}
