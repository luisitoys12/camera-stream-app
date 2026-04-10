package tech.estacionkus.camerastream.data.streaming

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websockets.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local HTTP + WebSocket server (port 8080) para:
 * - Panel web de control remoto (Feature #46)
 * - OBS Websocket bridge (Feature #45)
 * - Status endpoint para monitoring
 */
@Singleton
class LocalControlServer @Inject constructor(
    private val engineState: () -> EngineState,
    private val srtState: () -> SrtServerState,
    private val tunnelState: () -> TunnelState
) {
    private var server: EmbeddedServer<*, *>? = null
    private val HTTP_PORT = 8080

    fun start() {
        server = embeddedServer(Netty, port = HTTP_PORT) {
            install(ContentNegotiation) { json() }
            install(WebSockets)
            install(CORS) { anyHost() }

            routing {
                get("/status") {
                    call.respond(StatusResponse(
                        streaming = engineState().isStreaming,
                        bitrate = engineState().bitrateKbps,
                        networkQuality = engineState().networkQuality.name,
                        srtRunning = srtState().isRunning,
                        tunnelUrl = tunnelState().publicUrl,
                        activeTargets = engineState().activeTargets
                    ))
                }
                webSocket("/control") {
                    // Receive scene switch / overlay commands from web panel
                    for (frame in incoming) {
                        // Parse command and dispatch to engine
                    }
                }
            }
        }.start(wait = false)
    }

    fun stop() { server?.stop(); server = null }
}

@Serializable
data class StatusResponse(
    val streaming: Boolean,
    val bitrate: Int,
    val networkQuality: String,
    val srtRunning: Boolean,
    val tunnelUrl: String?,
    val activeTargets: List<String>
)
