package tech.estacionkus.camerastream.ui.screens.radio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tech.estacionkus.camerastream.billing.PlanTier
import tech.estacionkus.camerastream.billing.StripeManager
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class RadioBroadcastViewModel @Inject constructor(
    private val stripeManager: StripeManager
) : ViewModel() {

    val isAgency: StateFlow<Boolean> = stripeManager.currentPlan.map {
        it == PlanTier.AGENCY
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _uiState = MutableStateFlow(RadioUiState())
    val uiState: StateFlow<RadioUiState> = _uiState.asStateFlow()

    fun toggleBroadcast() {
        val current = _uiState.value
        _uiState.value = current.copy(isLive = !current.isLive)
        if (_uiState.value.isLive) {
            startAudioLevelSimulation()
        }
    }

    fun setNowPlaying(value: String) {
        _uiState.value = _uiState.value.copy(nowPlaying = value)
    }

    fun setCurrentShow(value: String) {
        _uiState.value = _uiState.value.copy(currentShow = value)
    }

    fun setNextShow(value: String) {
        _uiState.value = _uiState.value.copy(nextShow = value)
    }

    fun setRtmpUrl(value: String) {
        _uiState.value = _uiState.value.copy(rtmpUrl = value)
    }

    fun setStreamKey(value: String) {
        _uiState.value = _uiState.value.copy(streamKey = value)
    }

    fun setMode(mode: RadioMode) {
        _uiState.value = _uiState.value.copy(mode = mode)
    }

    private fun startAudioLevelSimulation() {
        viewModelScope.launch {
            while (_uiState.value.isLive) {
                _uiState.value = _uiState.value.copy(
                    audioLevelLeft = Random.nextFloat() * 0.7f + 0.1f,
                    audioLevelRight = Random.nextFloat() * 0.7f + 0.1f
                )
                delay(200)
            }
            _uiState.value = _uiState.value.copy(
                audioLevelLeft = 0f,
                audioLevelRight = 0f
            )
        }
    }
}
