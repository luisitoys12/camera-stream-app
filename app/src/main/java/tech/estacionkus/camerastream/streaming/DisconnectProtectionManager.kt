package tech.estacionkus.camerastream.streaming

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Represents the type of network currently active */
enum class NetworkType { WIFI, CELLULAR, ETHERNET, NONE }

@Singleton
class DisconnectProtectionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "DisconnectProtection"
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // -------------------------------------------------------------------------
    // Public state flows
    // -------------------------------------------------------------------------

    private val _isNetworkLost = MutableStateFlow(false)
    val isNetworkLost: StateFlow<Boolean> = _isNetworkLost.asStateFlow()

    private val _reconnectCountdown = MutableStateFlow(0)
    val reconnectCountdown: StateFlow<Int> = _reconnectCountdown.asStateFlow()

    private val _isReconnecting = MutableStateFlow(false)
    val isReconnecting: StateFlow<Boolean> = _isReconnecting.asStateFlow()

    private val _showBrb = MutableStateFlow(false)
    val showBrb: StateFlow<Boolean> = _showBrb.asStateFlow()

    private val _networkType = MutableStateFlow(NetworkType.NONE)
    val networkType: StateFlow<NetworkType> = _networkType.asStateFlow()

    private val _reconnectAttemptCount = MutableStateFlow(0)
    val reconnectAttemptCount: StateFlow<Int> = _reconnectAttemptCount.asStateFlow()

    // -------------------------------------------------------------------------
    // Configuration
    // -------------------------------------------------------------------------

    /** Duration before giving up on reconnection. Clamped to 30-120 seconds. */
    var timeoutSeconds: Int = 60
        set(value) {
            field = value.coerceIn(30, 120)
        }

    var onReconnect: (() -> Unit)? = null
    var onGiveUp: (() -> Unit)? = null
    var onNetworkTypeChanged: ((NetworkType) -> Unit)? = null

    // -------------------------------------------------------------------------
    // Internal state
    // -------------------------------------------------------------------------

    private var countdownJob: Job? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var isMonitoring = false
    private var activeNetworks = mutableSetOf<Network>()

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        _reconnectAttemptCount.value = 0

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Network available: $network")
                activeNetworks.add(network)
                val caps = cm.getNetworkCapabilities(network)
                val detectedType = detectNetworkType(caps)
                updateNetworkType(detectedType)

                if (_isNetworkLost.value) {
                    _isNetworkLost.value = false
                    _showBrb.value = false
                    _isReconnecting.value = true
                    countdownJob?.cancel()
                    _reconnectCountdown.value = 0
                    _reconnectAttemptCount.value = _reconnectAttemptCount.value + 1
                    scope.launch {
                        delay(1000)
                        onReconnect?.invoke()
                        delay(2000)
                        _isReconnecting.value = false
                    }
                }
            }

            override fun onLost(network: Network) {
                Log.w(TAG, "Network lost: $network")
                activeNetworks.remove(network)
                if (activeNetworks.isEmpty()) {
                    updateNetworkType(NetworkType.NONE)
                    _isNetworkLost.value = true
                    _showBrb.value = true
                    startCountdown()
                } else {
                    // Still have another network; update to remaining network type
                    val remainingCaps = activeNetworks.firstOrNull()?.let { cm.getNetworkCapabilities(it) }
                    updateNetworkType(detectNetworkType(remainingCaps))
                }
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                Log.d(TAG, "Network capabilities changed for $network")
                val detectedType = detectNetworkType(networkCapabilities)
                if (activeNetworks.contains(network)) {
                    updateNetworkType(detectedType)
                }
            }
        }

        cm.registerNetworkCallback(request, networkCallback!!)

        // Initialise with current network state
        val activeNet = cm.activeNetwork
        val activeCaps = activeNet?.let { cm.getNetworkCapabilities(it) }
        if (activeNet != null && activeCaps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true) {
            activeNetworks.add(activeNet)
            updateNetworkType(detectNetworkType(activeCaps))
        } else {
            updateNetworkType(NetworkType.NONE)
        }
    }

    fun stopMonitoring() {
        isMonitoring = false
        countdownJob?.cancel()
        activeNetworks.clear()
        _isNetworkLost.value = false
        _showBrb.value = false
        _reconnectCountdown.value = 0
        _isReconnecting.value = false
        _networkType.value = NetworkType.NONE
        networkCallback?.let {
            try {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                cm.unregisterNetworkCallback(it)
            } catch (_: Exception) {}
        }
        networkCallback = null
    }

    /** Force-reset the BRB overlay without waiting for the countdown to expire */
    fun dismissBrb() {
        _showBrb.value = false
        countdownJob?.cancel()
        _reconnectCountdown.value = 0
    }

    /** Reset attempt counter (e.g. after a successful reconnection) */
    fun resetAttemptCounter() {
        _reconnectAttemptCount.value = 0
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private fun startCountdown() {
        countdownJob?.cancel()
        _reconnectCountdown.value = timeoutSeconds
        countdownJob = scope.launch {
            var remaining = timeoutSeconds
            while (remaining > 0 && _isNetworkLost.value) {
                _reconnectCountdown.value = remaining
                delay(1000)
                remaining--
            }
            if (_isNetworkLost.value) {
                _reconnectCountdown.value = 0
                _showBrb.value = false
                onGiveUp?.invoke()
            }
        }
    }

    private fun detectNetworkType(caps: NetworkCapabilities?): NetworkType {
        if (caps == null) return NetworkType.NONE
        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
            else -> NetworkType.NONE
        }
    }

    private fun updateNetworkType(type: NetworkType) {
        if (_networkType.value != type) {
            Log.d(TAG, "Network type changed: ${_networkType.value} -> $type")
            _networkType.value = type
            onNetworkTypeChanged?.invoke(type)
        }
    }
}
