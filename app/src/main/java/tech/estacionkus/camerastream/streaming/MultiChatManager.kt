package tech.estacionkus.camerastream.streaming

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.*
import tech.estacionkus.camerastream.domain.model.ChatMessage
import tech.estacionkus.camerastream.domain.model.Platform
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MultiChatManager @Inject constructor() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val connections = mutableMapOf<Platform, WebSocket>()
    private val client = OkHttpClient()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun connect(platform: Platform, channel: String) {
        when (platform) {
            Platform.TWITCH -> connectTwitch(channel)
            Platform.KICK -> connectKick(channel)
            else -> {} // YouTube/TikTok require OAuth — handled separately
        }
    }

    private fun connectTwitch(channel: String) {
        val clean = channel.trimStart('#').lowercase()
        val req = Request.Builder().url("wss://irc-ws.chat.twitch.tv:443").build()
        connections[Platform.TWITCH] = client.newWebSocket(req, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, r: Response) {
                ws.send("CAP REQ :twitch.tv/tags")
                ws.send("PASS oauth:justinfan${(10000..99999).random()}")
                ws.send("NICK justinfan${(10000..99999).random()}")
                ws.send("JOIN #$clean")
            }
            override fun onMessage(ws: WebSocket, text: String) {
                if (text.startsWith("PING")) { ws.send("PONG :tmi.twitch.tv"); return }
                parseTwitch(text)
            }
        })
    }

    // Kick uses Pusher-based WebSocket
    private fun connectKick(channel: String) {
        val clean = channel.lowercase()
        // Kick public chat WebSocket
        val req = Request.Builder()
            .url("wss://ws-us2.pusher.com/app/32cbd69e4b950bf97679?protocol=7&client=js&version=7.4.0&flash=false")
            .build()
        connections[Platform.KICK] = client.newWebSocket(req, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, r: Response) {
                ws.send("""{"event":"pusher:subscribe","data":{"auth":"","channel":"chatrooms.$clean.v2"}}""")
            }
            override fun onMessage(ws: WebSocket, text: String) {
                parseKick(text)
            }
        })
    }

    private fun parseTwitch(raw: String) {
        if (!raw.contains("PRIVMSG")) return
        try {
            val color = Regex("color=([^;]*)").find(raw)?.groupValues?.get(1) ?: "#9B59B6"
            val name = Regex("display-name=([^;]*)").find(raw)?.groupValues?.get(1) ?: "user"
            val msg = Regex("PRIVMSG #\\S+ :(.+)").find(raw)?.groupValues?.get(1) ?: return
            addMessage(ChatMessage(UUID.randomUUID().toString(), name, msg.trim(), Platform.TWITCH, authorColor = color.ifBlank { "#9B59B6" }))
        } catch (_: Exception) {}
    }

    private fun parseKick(raw: String) {
        try {
            if (!raw.contains("ChatMessageSent")) return
            val name = Regex("\"username\":\"([^\"]+)\"").find(raw)?.groupValues?.get(1) ?: return
            val content = Regex("\"content\":\"([^\"]+)\"").find(raw)?.groupValues?.get(1) ?: return
            addMessage(ChatMessage(UUID.randomUUID().toString(), name, content, Platform.KICK, authorColor = "#53FC18"))
        } catch (_: Exception) {}
    }

    private fun addMessage(msg: ChatMessage) {
        _messages.value = (_messages.value + msg).takeLast(200)
    }

    fun disconnect(platform: Platform) {
        connections[platform]?.close(1000, null)
        connections.remove(platform)
    }

    fun disconnectAll() {
        connections.values.forEach { it.close(1000, null) }
        connections.clear()
        _messages.value = emptyList()
    }
}
