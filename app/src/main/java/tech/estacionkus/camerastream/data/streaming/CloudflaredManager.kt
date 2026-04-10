package tech.estacionkus.camerastream.data.streaming

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.*
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

data class TunnelState(
    val isRunning: Boolean = false,
    val publicUrl: String? = null,
    val error: String? = null
)

@Singleton
class CloudflaredManager @Inject constructor(
    private val context: Context
) {
    private val _state = MutableStateFlow(TunnelState())
    val state: StateFlow<TunnelState> = _state.asStateFlow()

    private var tunnelProcess: Process? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Lanza cloudflared como binario nativo ARM64.
     * El binario se copia desde assets al storage interno en primer uso.
     * Genera URL pública tipo: https://xxxx.trycloudflare.com
     * Para SRT usamos TCP tunnel: cloudflared tunnel --url tcp://localhost:{port}
     */
    suspend fun startTunnel(port: Int): String? {
        val binary = extractCloudflaredBinary() ?: return null
        return withContext(Dispatchers.IO) {
            val process = ProcessBuilder(
                binary.absolutePath,
                "tunnel",
                "--url", "tcp://localhost:$port",
                "--no-autoupdate"
            )
                .redirectErrorStream(true)
                .start()
            tunnelProcess = process
            // Parse cloudflared output to extract the public URL
            val url = parseCloudflaredUrl(process.inputStream)
            if (url != null) {
                _state.value = TunnelState(isRunning = true, publicUrl = url)
            }
            url
        }
    }

    fun stopTunnel() {
        tunnelProcess?.destroy()
        tunnelProcess = null
        _state.value = TunnelState()
    }

    private fun extractCloudflaredBinary(): File? {
        return try {
            val binaryFile = File(context.filesDir, "cloudflared")
            if (!binaryFile.exists()) {
                context.assets.open("cloudflared-arm64").use { input ->
                    binaryFile.outputStream().use { output -> input.copyTo(output) }
                }
                binaryFile.setExecutable(true)
            }
            binaryFile
        } catch (e: Exception) { null }
    }

    private fun parseCloudflaredUrl(stream: InputStream): String? {
        val reader = BufferedReader(InputStreamReader(stream))
        var line: String?
        val timeout = System.currentTimeMillis() + 15_000 // 15s timeout
        while (System.currentTimeMillis() < timeout) {
            line = reader.readLine() ?: continue
            // cloudflared prints: "https://xxxx.trycloudflare.com"
            val match = Regex("https://[a-z0-9-]+\\.trycloudflare\\.com").find(line)
            if (match != null) return match.value
        }
        return null
    }

    /** Convert HTTPS tunnel URL to SRT format for display */
    fun toSrtUrl(httpsUrl: String, port: Int = 9999): String {
        val host = httpsUrl.removePrefix("https://")
        return "srt://$host:$port"
    }
}
