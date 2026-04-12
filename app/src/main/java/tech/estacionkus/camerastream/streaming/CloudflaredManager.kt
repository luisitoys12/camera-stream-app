package tech.estacionkus.camerastream.streaming

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

enum class TunnelStatus { IDLE, DOWNLOADING, STARTING, ACTIVE, ERROR }

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

    private val _status = MutableStateFlow(TunnelStatus.IDLE)
    val status: StateFlow<TunnelStatus> = _status.asStateFlow()

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _srtUrl = MutableStateFlow<String?>(null)
    val srtUrl: StateFlow<String?> = _srtUrl.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    private var process: Process? = null
    private val binaryFile get() = File(context.filesDir, "cloudflared")
    private val client = OkHttpClient()

    private val CLOUDFLARED_URL = "https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-arm64"

    fun isBinaryAvailable(): Boolean = binaryFile.exists() && binaryFile.length() > 1_000_000

    suspend fun downloadBinary(): Boolean = withContext(Dispatchers.IO) {
        if (isBinaryAvailable()) return@withContext true
        _status.value = TunnelStatus.DOWNLOADING
        _statusMessage.value = "Downloading cloudflared..."
        _downloadProgress.value = 0f

        try {
            // Try extracting from assets first
            try {
                context.assets.open("cloudflared-arm64").use { input ->
                    FileOutputStream(binaryFile).use { output -> input.copyTo(output) }
                }
                binaryFile.setExecutable(true)
                if (binaryFile.length() > 1_000_000) {
                    _downloadProgress.value = 1f
                    _statusMessage.value = "Binary ready"
                    return@withContext true
                }
            } catch (_: Exception) {
                // Asset not bundled, download from web
            }

            // Download from GitHub releases
            val request = Request.Builder().url(CLOUDFLARED_URL).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                _status.value = TunnelStatus.ERROR
                _statusMessage.value = "Download failed: HTTP ${response.code}"
                return@withContext false
            }

            val body = response.body ?: run {
                _status.value = TunnelStatus.ERROR
                _statusMessage.value = "Empty response"
                return@withContext false
            }

            val totalBytes = body.contentLength()
            var bytesRead = 0L
            body.byteStream().use { input ->
                FileOutputStream(binaryFile).use { output ->
                    val buf = ByteArray(8192)
                    var n: Int
                    while (input.read(buf).also { n = it } != -1) {
                        output.write(buf, 0, n)
                        bytesRead += n
                        if (totalBytes > 0) {
                            _downloadProgress.value = bytesRead.toFloat() / totalBytes
                        }
                    }
                }
            }

            binaryFile.setExecutable(true)
            _downloadProgress.value = 1f
            _statusMessage.value = "Binary ready"
            true
        } catch (e: Exception) {
            Log.e(TAG, "Download failed: ${e.message}")
            _status.value = TunnelStatus.ERROR
            _statusMessage.value = "Download failed: ${e.message}"
            binaryFile.delete()
            false
        }
    }

    fun startTunnel(localPort: Int = 9999) {
        if (_isRunning.value) return
        scope.launch {
            if (!isBinaryAvailable()) {
                if (!downloadBinary()) return@launch
            }

            try {
                _status.value = TunnelStatus.STARTING
                _statusMessage.value = "Starting tunnel..."

                process = ProcessBuilder(
                    binaryFile.absolutePath, "tunnel", "--url",
                    "tcp://localhost:$localPort", "--no-autoupdate"
                ).redirectErrorStream(true).start()

                _isRunning.value = true
                val reader = BufferedReader(InputStreamReader(process!!.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    Log.d(TAG, "cloudflared: $line")
                    val m = Regex("https://[a-z0-9-]+\\.trycloudflare\\.com").find(line ?: "")
                    if (m != null) {
                        val tunnelHost = m.value.removePrefix("https://")
                        _tunnelUrl.value = m.value
                        _srtUrl.value = "srt://$tunnelHost:$localPort"
                        _status.value = TunnelStatus.ACTIVE
                        _statusMessage.value = "Tunnel active"
                        Log.i(TAG, "Tunnel URL: ${m.value}")
                        Log.i(TAG, "SRT URL: srt://$tunnelHost:$localPort")
                    }
                }
                _isRunning.value = false
                _status.value = TunnelStatus.IDLE
                _statusMessage.value = "Tunnel stopped"
            } catch (e: Exception) {
                Log.e(TAG, "Tunnel error: ${e.message}")
                _isRunning.value = false
                _status.value = TunnelStatus.ERROR
                _statusMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun startWithSrt(srtServerManager: SrtServerManager) {
        scope.launch {
            if (!srtServerManager.isRunning.value) {
                srtServerManager.start()
                delay(500) // Give SRT server time to bind
            }
            startTunnel(srtServerManager.port)
        }
    }

    fun stopTunnel() {
        process?.destroyForcibly()
        process = null
        _isRunning.value = false
        _tunnelUrl.value = null
        _srtUrl.value = null
        _status.value = TunnelStatus.IDLE
        _statusMessage.value = ""
    }
}
