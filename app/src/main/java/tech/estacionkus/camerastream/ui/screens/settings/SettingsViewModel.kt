package tech.estacionkus.camerastream.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tech.estacionkus.camerastream.data.settings.SettingsRepository
import tech.estacionkus.camerastream.data.settings.StreamSettings
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: SettingsRepository
) : ViewModel() {
    val settings = repo.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StreamSettings())

    fun save(settings: StreamSettings) {
        viewModelScope.launch { repo.saveSettings(settings) }
    }
}
