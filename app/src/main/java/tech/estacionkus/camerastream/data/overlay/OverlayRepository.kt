package tech.estacionkus.camerastream.data.overlay

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import tech.estacionkus.camerastream.data.media.OverlayCategory
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.overlayDataStore: DataStore<Preferences> by preferencesDataStore(name = "active_overlays")

// Serializable snapshot para DataStore
@Serializable
data class OverlaySnapshot(
    val id: String,
    val uriString: String,
    val name: String,
    val category: String,
    val isVideo: Boolean,
    val position: String,
    val scalePercent: Float,
    val alpha: Float,
    val isLooping: Boolean,
    val autoHideAfterMs: Long?
)

@Singleton
class OverlayRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val OVERLAYS_KEY = stringPreferencesKey("overlays_json")

    private val _activeOverlays = MutableStateFlow<List<ActiveOverlay>>(emptyList())
    val activeOverlays: StateFlow<List<ActiveOverlay>> = _activeOverlays.asStateFlow()

    // Load persisted overlays on init
    suspend fun loadPersisted() {
        context.overlayDataStore.data
            .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
            .first()[OVERLAYS_KEY]?.let { json ->
                try {
                    val snapshots = Json.decodeFromString<List<OverlaySnapshot>>(json)
                    _activeOverlays.value = snapshots.map { it.toActiveOverlay() }
                } catch (_: Exception) {}
            }
    }

    fun addOverlay(overlay: ActiveOverlay) {
        _activeOverlays.value = _activeOverlays.value + overlay
        persistAsync()
    }

    fun removeOverlay(id: String) {
        _activeOverlays.value = _activeOverlays.value.filter { it.id != id }
        persistAsync()
    }

    fun updateOverlay(overlay: ActiveOverlay) {
        _activeOverlays.value = _activeOverlays.value.map { if (it.id == overlay.id) overlay else it }
        persistAsync()
    }

    fun clearAll() {
        _activeOverlays.value = emptyList()
        persistAsync()
    }

    private fun persistAsync() {
        kotlinx.coroutines.GlobalScope.run {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                val snapshots = _activeOverlays.value.map { it.toSnapshot() }
                context.overlayDataStore.edit { prefs ->
                    prefs[OVERLAYS_KEY] = Json.encodeToString(snapshots)
                }
            }
        }
    }

    private fun ActiveOverlay.toSnapshot() = OverlaySnapshot(
        id, uri.toString(), name, category.name, isVideo,
        position.name, scalePercent, alpha, isLooping, autoHideAfterMs
    )

    private fun OverlaySnapshot.toActiveOverlay() = ActiveOverlay(
        id = id,
        uri = Uri.parse(uriString),
        name = name,
        category = OverlayCategory.valueOf(category),
        isVideo = isVideo,
        position = OverlayPosition.valueOf(position),
        scalePercent = scalePercent,
        alpha = alpha,
        isLooping = isLooping,
        autoHideAfterMs = autoHideAfterMs
    )
}
