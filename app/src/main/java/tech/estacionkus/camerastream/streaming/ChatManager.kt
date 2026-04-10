package tech.estacionkus.camerastream.streaming

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.*
import okhttp3.*
import tech.estacionkus.camerastream.domain.model.ChatMessage
import tech.estacionkus.camerastream.domain.model.Platform
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatManager @Inject constructor() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // YouTube Live Chat via WebSocket proxy isn't directly accessible;
    // We implement Twitch IRC over WebSocket (works natively in Android)
    fun connectTwitch(channel: String) {
        val cleanChannel = channel.trimStart('#').lowercase()
        val request = Request.Builder()
            .url("wss://irc-ws.chat.twitch.tv:443")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                ws.send("CAP REQ :twitch.tv/tags twitch.tv/commands")
                ws.send("PASS oauth:justinfan${(10000..99999).random()}")
                ws.send("NICK justinfan${(10000..99999).random()}")
                ws.send("JOIN #$cleanChannel")
                _isConnected.value = true
            }

            override fun onMessage(ws: WebSocket, text: String) {
                if (text.startsWith("PING")) { ws.send("PONG :tmi.twitch.tv"); return }
                parseTwitchMessage(text)
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                _isConnected.value = false
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                _isConnected.value = false
            }
        })
    }

    private fun parseTwitchMessage(raw: String) {
        if (!raw.contains("PRIVMSG")) return
        try {
            val colorMatch = Regex("color=([^;]*)").find(raw)?.groupValues?.get(1) ?: "#FFFFFF"
            val displayName = Regex("display-name=([^;]*)").find(raw)?.groupValues?.get(1) ?: "user"
            val msgMatch = Regex("PRIVMSG #\\S+ :(.+)").find(raw)?.groupValues?.get(1) ?: return
            val msg = ChatMessage(
                id = UUID.randomUUID().toString(),
                author = displayName,
                message = msgMatch.trim(),
                platform = Platform.TWITCH,
                authorColor = colorMatch.ifBlank { "#9B59B6" }
            )
            _messages.value = (_messages.value + msg).takeLast(100)
        } catch (_: Exception) {}
    }

    fun disconnect() {
        webSocket?.close(1000, null)
        webSocket = null
        _isConnected.value = false
        _messages.value = emptyList()
    }

    fun clearMessages() { _messages.value = emptyList() }
}
