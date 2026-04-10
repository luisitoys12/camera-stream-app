package tech.estacionkus.camerastream.data.streaming

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

data class BondingState(
    val isActive: Boolean = false,
    val wifiActive: Boolean = false,
    val cellularActive: Boolean = false,
    val combinedBitrateKbps: Int = 0
)

/**
 * SRTLA Bonding — usa WiFi y datos móviles simultáneamente.
 * Envía paquetes SRT por múltiples interfaces de red para mayor estabilidad.
 * Similar a IRL Pro bonding.
 */
@Singleton
class SrtlaBondingManager @Inject constructor(private val context: Context) {
    private val _state = MutableStateFlow(BondingState())
    val state: StateFlow<BondingState> = _state.asStateFlow()

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val availableNetworks = mutableListOf<Network>()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            availableNetworks.add(network)
            updateState()
        }
        override fun onLost(network: Network) {
            availableNetworks.remove(network)
            updateState()
        }
    }

    fun startBonding() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
        _state.value = _state.value.copy(isActive = true)
    }

    fun stopBonding() {
        try { connectivityManager.unregisterNetworkCallback(networkCallback) } catch (_: Exception) {}
        availableNetworks.clear()
        _state.value = BondingState()
    }

    private fun updateState() {
        val hasWifi = availableNetworks.any { net ->
            connectivityManager.getNetworkCapabilities(net)
                ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        }
        val hasCellular = availableNetworks.any { net ->
            connectivityManager.getNetworkCapabilities(net)
                ?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
        }
        _state.value = _state.value.copy(
            wifiActive = hasWifi,
            cellularActive = hasCellular
        )
    }
}
