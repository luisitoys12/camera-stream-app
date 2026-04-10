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

    // cloudflared binary must be bundled in assets/cloudflared-arm64
    private val binaryFile: File
        get() = File(context.filesDir, "cloudflared")

    fun ensureBinary() {
        if (binaryFile.exists()) return
        try {
            context.assets.open("cloudflared-arm64").use { input ->
                FileOutputStream(binaryFile).use { output -> input.copyTo(output) }
            }
            binaryFile.setExecutable(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract cloudflared binary: ${e.message}")
        }
    }

    fun startTunnel(localPort: Int = 9999) {
        if (_isRunning.value) return
        ensureBinary()
        scope.launch {
            try {
                val cmd = arrayOf(
                    binaryFile.absolutePath,
                    "tunnel", "--url", "tcp://localhost:$localPort",
                    "--no-autoupdate"
                )
                process = ProcessBuilder(*cmd)
                    .redirectErrorStream(true)
                    .start()

                _isRunning.value = true
                val reader = BufferedReader(InputStreamReader(process!!.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    Log.d(TAG, line ?: continue)
                    // Cloudflared prints the URL in stderr/stdout like:
                    // "Your quick Tunnel has been created! Visit it at (it may take some time to start up):"
                    // "https://xxxx.trycloudflare.com"
                    val match = Regex("https://[a-z0-9-]+\.trycloudflare\.com").find(line ?: "") ?:
                                Regex("INF \\|(.+trycloudflare\.com)").find(line ?: "")
                    if (match != null) {
                        val url = match.value.trim()
                        // For SRT tunnels, replace https with the TCP address
                        _tunnelUrl.value = url
                        Log.i(TAG, "Tunnel URL: $url")
                    }
                }
                _isRunning.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Cloudflared error: ${e.message}")
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
