package tech.estacionkus.camerastream.domain.model

data class ChatMessage(
    val id: String,
    val author: String,
    val content: String,
    val platform: Platform,
    val authorColor: String = "#9B59B6",
    val isPinned: Boolean = false,
    val badges: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)
