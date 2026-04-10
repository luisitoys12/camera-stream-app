package tech.estacionkus.camerastream.streaming

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdaptiveBitrateController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val streamManager: RtmpStreamManager
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _currentBitrate = MutableStateFlow(2500)
    val currentBitrate: StateFlow<Int> = _currentBitrate.asStateFlow()

    private val _isEnabled = MutableStateFlow(false)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private var controlJob: Job? = null
    private var targetBitrate = 2500
    private var consecutiveDrops = 0

    fun enable(maxBitrateKbps: Int) {
        targetBitrate = maxBitrateKbps
        _isEnabled.value = true
        controlJob = scope.launch {
            while (isActive) {
                delay(3000)
                adjust()
            }
        }
    }

    fun disable() {
        controlJob?.cancel()
        _isEnabled.value = false
    }

    private fun adjust() {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork)
        val bw = caps?.linkDownstreamBandwidthKbps ?: return

        val stats = streamManager.stats.value
        val dropped = stats.droppedFrames

        val newBitrate = when {
            dropped > 10 || bw < 1000 -> {
                consecutiveDrops++
                (_currentBitrate.value * 0.7).toInt().coerceAtLeast(300)
            }
            dropped == 0 && bw > 5000 && consecutiveDrops == 0 -> {
                ((_currentBitrate.value * 1.1).toInt()).coerceAtMost(targetBitrate)
            }
            else -> {
                consecutiveDrops = (consecutiveDrops - 1).coerceAtLeast(0)
                _currentBitrate.value
            }
        }

        if (newBitrate != _currentBitrate.value) {
            _currentBitrate.value = newBitrate
            streamManager.setBitrate(newBitrate)
        }
    }
}
