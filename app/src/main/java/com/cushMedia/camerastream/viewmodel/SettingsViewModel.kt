package com.cushMedia.camerastream.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cushMedia.camerastream.model.StreamSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    private val TAG = "SettingsViewModel"

    private val _settings = MutableStateFlow(StreamSettings())
    val settings: StateFlow<StreamSettings> = _settings

    fun updateSrtHost(host: String) { _settings.value = _settings.value.copy(srtHost = host) }
    fun updateSrtStreamId(id: String) { _settings.value = _settings.value.copy(srtStreamId = id) }
    fun updateYoutubeUrl(url: String) { _settings.value = _settings.value.copy(youtubeRtmpUrl = url) }
    fun updateYoutubeKey(key: String) { _settings.value = _settings.value.copy(youtubeStreamKey = key) }
    fun updateTwitchUrl(url: String) { _settings.value = _settings.value.copy(twitchRtmpUrl = url) }
    fun updateTwitchKey(key: String) { _settings.value = _settings.value.copy(twitchStreamKey = key) }
    fun updateFacebookUrl(url: String) { _settings.value = _settings.value.copy(facebookRtmpUrl = url) }
    fun updateFacebookKey(key: String) { _settings.value = _settings.value.copy(facebookStreamKey = key) }
    fun updateResolution(res: String) { _settings.value = _settings.value.copy(resolution = res) }
    fun updateBitrate(bitrate: String) { _settings.value = _settings.value.copy(videoBitrate = bitrate) }
    fun toggleYoutube() { _settings.value = _settings.value.copy(youtubeEnabled = !_settings.value.youtubeEnabled) }
    fun toggleTwitch() { _settings.value = _settings.value.copy(twitchEnabled = !_settings.value.twitchEnabled) }
    fun toggleFacebook() { _settings.value = _settings.value.copy(facebookEnabled = !_settings.value.facebookEnabled) }

    fun saveSettings() {
        viewModelScope.launch {
            // TODO: persistir en DataStore
            Log.i(TAG, "Configuración guardada: ${_settings.value}")
        }
    }
}
