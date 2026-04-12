package tech.estacionkus.camerastream.streaming

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.*
import tech.estacionkus.camerastream.domain.model.ChatMessage
import tech.estacionkus.camerastream.domain.model.Platform
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MultiChatManager @Inject constructor() {
    private val TAG = "MultiChatManager"

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _connectedPlatforms = MutableStateFlow<Set<Platform>>(emptySet())
    val connectedPlatforms: StateFlow<Set<Platform>> = _connectedPlatforms.asStateFlow()

    private val _pinnedMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val pinnedMessages: StateFlow<List<ChatMessage>> = _pinnedMessages.asStateFlow()

    private val connections = mutableMapOf<Platform, WebSocket>()
    private val client = OkHttpClient()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val json = Json { ignoreUnknownKeys = true }

    // YouTube API configuration
    private var youtubeApiKey: String? = null
    private var youtubePollingJob: Job? = null

    fun setYouTubeApiKey(key: String) {
        youtubeApiKey = key.ifBlank { null }
    }

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

    /**
     * Connect to YouTube Live Chat using YouTube Data API v3.
     * @param liveChatId The YouTube live chat ID or video ID to poll messages from.
     */
    private fun connectYouTube(liveChatId: String) {
        val apiKey = youtubeApiKey
        if (apiKey.isNullOrBlank()) {
            Log.w(TAG, "YouTube API key not configured. YouTube chat requires a YouTube Data API v3 key.")
            addMsg(ChatMessage(
                id = UUID.randomUUID().toString(),
                author = "System",
                content = "YouTube chat requires an API key. Go to Settings > Chat > YouTube API Key to configure it.",
                platform = Platform.YOUTUBE,
                authorColor = "#FF0000"
            ))
            _connectedPlatforms.value = _connectedPlatforms.value + Platform.YOUTUBE
            return
        }

        _connectedPlatforms.value = _connectedPlatforms.value + Platform.YOUTUBE

        youtubePollingJob?.cancel()
        youtubePollingJob = scope.launch {
            var nextPageToken: String? = null
            var pollingIntervalMs = 5000L

            while (isActive) {
                try {
                    val url = buildString {
                        append("https://www.googleapis.com/youtube/v3/liveChat/messages")
                        append("?liveChatId=$liveChatId")
                        append("&part=snippet,authorDetails")
                        append("&key=$apiKey")
                        if (nextPageToken != null) {
                            append("&pageToken=$nextPageToken")
                        }
                    }

                    val request = Request.Builder().url(url).get().build()
                    val response = client.newCall(request).execute()

                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (body != null) {
                            parseYouTubeChatResponse(body)?.let { parsed ->
                                nextPageToken = parsed.nextPageToken
                                pollingIntervalMs = parsed.pollingIntervalMillis.coerceAtLeast(2000)

                                parsed.messages.forEach { msg ->
                                    addMsg(ChatMessage(
                                        id = msg.id,
                                        author = msg.author,
                                        content = msg.content,
                                        platform = Platform.YOUTUBE,
                                        authorColor = "#FF0000",
                                        badges = if (msg.isChatOwner) listOf("owner") else if (msg.isChatModerator) listOf("moderator") else emptyList()
                                    ))
                                }
                            }
                        }
                    } else {
                        Log.w(TAG, "YouTube API error: ${response.code} ${response.message}")
                        if (response.code == 403 || response.code == 401) {
                            addMsg(ChatMessage(
                                id = UUID.randomUUID().toString(),
                                author = "System",
                                content = "YouTube API error: ${response.code}. Check your API key and quota.",
                                platform = Platform.YOUTUBE,
                                authorColor = "#FF0000"
                            ))
                            break
                        }
                    }
                    response.close()
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Log.w(TAG, "YouTube polling error: ${e.message}")
                }
                delay(pollingIntervalMs)
            }
        }
    }

    private data class YouTubeChatParsed(
        val nextPageToken: String?,
        val pollingIntervalMillis: Long,
        val messages: List<YouTubeChatMessage>
    )

    private data class YouTubeChatMessage(
        val id: String,
        val author: String,
        val content: String,
        val isChatOwner: Boolean,
        val isChatModerator: Boolean
    )

    private fun parseYouTubeChatResponse(body: String): YouTubeChatParsed? {
        return try {
            // Parse using regex for lightweight extraction without adding a JSON model dependency
            val nextPageToken = Regex("\"nextPageToken\"\\s*:\\s*\"([^\"]+)\"").find(body)?.groupValues?.get(1)
            val pollingInterval = Regex("\"pollingIntervalMillis\"\\s*:\\s*(\\d+)").find(body)?.groupValues?.get(1)?.toLongOrNull() ?: 5000L

            val messages = mutableListOf<YouTubeChatMessage>()
            // Parse each item in the items array
            val itemsMatch = Regex("\"items\"\\s*:\\s*\\[([\\s\\S]*?)\\]").find(body)
            if (itemsMatch != null) {
                val itemsStr = itemsMatch.groupValues[1]
                // Split by each message object pattern
                val idMatches = Regex("\"id\"\\s*:\\s*\"([^\"]+)\"").findAll(itemsStr).toList()
                val displayNameMatches = Regex("\"displayName\"\\s*:\\s*\"([^\"]+)\"").findAll(itemsStr).toList()
                val displayMessageMatches = Regex("\"displayMessage\"\\s*:\\s*\"([^\"]+)\"").findAll(itemsStr).toList()
                val ownerMatches = Regex("\"isChatOwner\"\\s*:\\s*(true|false)").findAll(itemsStr).toList()
                val modMatches = Regex("\"isChatModerator\"\\s*:\\s*(true|false)").findAll(itemsStr).toList()

                val count = minOf(idMatches.size, displayNameMatches.size, displayMessageMatches.size)
                for (i in 0 until count) {
                    messages.add(YouTubeChatMessage(
                        id = idMatches[i].groupValues[1],
                        author = displayNameMatches[i].groupValues[1],
                        content = displayMessageMatches[i].groupValues[1],
                        isChatOwner = ownerMatches.getOrNull(i)?.groupValues?.get(1) == "true",
                        isChatModerator = modMatches.getOrNull(i)?.groupValues?.get(1) == "true"
                    ))
                }
            }

            YouTubeChatParsed(nextPageToken, pollingInterval, messages)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse YouTube chat response: ${e.message}")
            null
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
        if (platform == Platform.YOUTUBE) {
            youtubePollingJob?.cancel()
            youtubePollingJob = null
        }
        connections[platform]?.close(1000, null)
        connections.remove(platform)
        _connectedPlatforms.value = _connectedPlatforms.value - platform
    }

    fun disconnectAll() {
        youtubePollingJob?.cancel()
        youtubePollingJob = null
        connections.values.forEach { it.close(1000, null) }
        connections.clear()
        _connectedPlatforms.value = emptySet()
        _messages.value = emptyList()
        _pinnedMessages.value = emptyList()
    }
}
