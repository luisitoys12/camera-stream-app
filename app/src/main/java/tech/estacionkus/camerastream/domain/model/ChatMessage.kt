package tech.estacionkus.camerastream.domain.model

data class ChatMessage(
    val id: String,
    val author: String,
    val message: String,
    val platform: Platform,
    val timestampMs: Long = System.currentTimeMillis(),
    val authorColor: String = "#FFFFFF"
)
