package tech.estacionkus.camerastream.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "stream_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val SETTINGS_KEY = stringPreferencesKey("settings_json")

    val settings: Flow<StreamSettings> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            prefs[SETTINGS_KEY]?.let {
                try { Json.decodeFromString<StreamSettings>(it) } catch (e: Exception) { StreamSettings() }
            } ?: StreamSettings()
        }

    suspend fun saveSettings(settings: StreamSettings) {
        context.dataStore.edit { prefs ->
            prefs[SETTINGS_KEY] = Json.encodeToString(settings)
        }
    }
}
