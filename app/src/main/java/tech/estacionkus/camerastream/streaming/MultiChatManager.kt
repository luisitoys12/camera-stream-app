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

    private val _connectedPlatforms = MutableStateFlow<Set<Platform>>(emptySet())
    val connectedPlatforms: StateFlow<Set<Platform>> = _connectedPlatforms.asStateFlow()

    private val _pinnedMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val pinnedMessages: StateFlow<List<ChatMessage>> = _pinnedMessages.asStateFlow()

    private val connections = mutableMapOf<Platform, WebSocket>()
    private val client = OkHttpClient()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun connect(platform: Platform, channel: String) {
        when (platform) {
            Platform.TWITCH -> connectTwitch(channel)
            Platform.KICK -> connectKick(channel)
            Platform.YOUTUBE -> connectYouTube(channel)
            else -> {}
        }
    }

    private fun connectTwitch(channel: String) {
        val ch = channel.trimStart('#').lowercase()
        val req = Request.Builder().url("wss://irc-ws.chat.twitch.tv:443").build()
        connections[Platform.TWITCH] = client.newWebSocket(req, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, r: Response) {
                ws.send("CAP REQ :twitch.tv/tags twitch.tv/commands")
                ws.send("PASS oauth:justinfan${(10000..99999).random()}")
                ws.send("NICK justinfan${(10000..99999).random()}")
                ws.send("JOIN #$ch")
                _connectedPlatforms.value = _connectedPlatforms.value + Platform.TWITCH
            }
            override fun onMessage(ws: WebSocket, text: String) {
                if (text.startsWith("PING")) { ws.send("PONG :tmi.twitch.tv"); return }
                if (!text.contains("PRIVMSG")) return
                try {
                    val color = Regex("color=([^;]*)").find(text)?.groupValues?.get(1) ?: "#9B59B6"
                    val name = Regex("display-name=([^;]*)").find(text)?.groupValues?.get(1) ?: "user"
                    val badges = Regex("badges=([^;]*)").find(text)?.groupValues?.get(1)
                        ?.split(",")?.map { it.split("/").first() } ?: emptyList()
                    val msg = Regex("PRIVMSG #\\S+ :(.+)").find(text)?.groupValues?.get(1) ?: return
                    addMsg(ChatMessage(
                        id = UUID.randomUUID().toString(),
                        author = name,
                        content = msg.trim(),
                        platform = Platform.TWITCH,
                        authorColor = color.ifBlank { "#9B59B6" },
                        badges = badges
                    ))
                } catch (_: Exception) {}
            }
            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                _connectedPlatforms.value = _connectedPlatforms.value - Platform.TWITCH
            }
            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                _connectedPlatforms.value = _connectedPlatforms.value - Platform.TWITCH
            }
        })
    }

    private fun connectKick(channel: String) {
        val req = Request.Builder()
            .url("wss://ws-us2.pusher.com/app/32cbd69e4b950bf97679?protocol=7&client=js&version=7.4.0&flash=false")
            .build()
        connections[Platform.KICK] = client.newWebSocket(req, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, r: Response) {
                ws.send("""{"event":"pusher:subscribe","data":{"auth":"","channel":"chatrooms.${channel.lowercase()}.v2"}}""")
                _connectedPlatforms.value = _connectedPlatforms.value + Platform.KICK
            }
            override fun onMessage(ws: WebSocket, text: String) {
                if (!text.contains("ChatMessageSent")) return
                try {
                    val name = Regex("\"username\":\"([^\"]+)\"").find(text)?.groupValues?.get(1) ?: return
                    val content = Regex("\"content\":\"([^\"]+)\"").find(text)?.groupValues?.get(1) ?: return
                    addMsg(ChatMessage(
                        id = UUID.randomUUID().toString(),
                        author = name,
                        content = content,
                        platform = Platform.KICK,
                        authorColor = "#53FC18"
                    ))
                } catch (_: Exception) {}
            }
            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                _connectedPlatforms.value = _connectedPlatforms.value - Platform.KICK
            }
            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                _connectedPlatforms.value = _connectedPlatforms.value - Platform.KICK
            }
        })
    }

    private fun connectYouTube(liveChatId: String) {
        // YouTube Live Chat requires API key and polling (not WebSocket)
        // Implement polling-based chat reader
        _connectedPlatforms.value = _connectedPlatforms.value + Platform.YOUTUBE
        scope.launch {
            // Poll YouTube Live Chat API every 5 seconds
            // In production, this would use YouTube Data API v3
            // liveChatMessages.list with liveChatId
            while (isActive) {
                try {
                    // Placeholder: Real implementation would poll YouTube API
                    delay(5000)
                } catch (_: Exception) {
                    break
                }
            }
        }
    }

    fun pinMessage(messageId: String) {
        val msg = _messages.value.find { it.id == messageId } ?: return
        val pinned = msg.copy(isPinned = true)
        _pinnedMessages.value = _pinnedMessages.value + pinned
    }

    fun unpinMessage(messageId: String) {
        _pinnedMessages.value = _pinnedMessages.value.filter { it.id != messageId }
    }

    private fun addMsg(msg: ChatMessage) {
        _messages.value = (_messages.value + msg).takeLast(200)
    }

    fun disconnectPlatform(platform: Platform) {
        connections[platform]?.close(1000, null)
        connections.remove(platform)
        _connectedPlatforms.value = _connectedPlatforms.value - platform
    }

    fun disconnectAll() {
        connections.values.forEach { it.close(1000, null) }
        connections.clear()
        _connectedPlatforms.value = emptySet()
        _messages.value = emptyList()
        _pinnedMessages.value = emptyList()
    }
}
