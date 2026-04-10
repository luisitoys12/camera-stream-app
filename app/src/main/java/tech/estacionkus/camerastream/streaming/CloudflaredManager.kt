package tech.estacionkus.camerastream.streaming

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudflaredManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "CloudflaredManager"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _tunnelUrl = MutableStateFlow<String?>(null)
    val tunnelUrl: StateFlow<String?> = _tunnelUrl.asStateFlow()
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var process: Process? = null
    private val binaryFile get() = File(context.filesDir, "cloudflared")

    fun ensureBinary() {
        if (binaryFile.exists() && binaryFile.length() > 1000) return
        try {
            context.assets.open("cloudflared-arm64").use { i ->
                FileOutputStream(binaryFile).use { o -> i.copyTo(o) }
            }
            binaryFile.setExecutable(true)
        } catch (e: Exception) { Log.e(TAG, "Binary extract: ${e.message}") }
    }

    fun startTunnel(localPort: Int = 9999) {
        if (_isRunning.value) return
        ensureBinary()
        scope.launch {
            try {
                process = ProcessBuilder(binaryFile.absolutePath, "tunnel", "--url", "tcp://localhost:$localPort", "--no-autoupdate")
                    .redirectErrorStream(true).start()
                _isRunning.value = true
                val reader = BufferedReader(InputStreamReader(process!!.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val m = Regex("https://[a-z0-9-]+\.trycloudflare\.com").find(line ?: "") ?: continue
                    _tunnelUrl.value = m.value.trim()
                    Log.i(TAG, "Tunnel: ${m.value}")
                }
                _isRunning.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Tunnel error: ${e.message}")
                _isRunning.value = false
            }
        }
    }

    fun stopTunnel() {
        process?.destroy()
        process = null
        _isRunning.value = false
        _tunnelUrl.value = null
    }
}
