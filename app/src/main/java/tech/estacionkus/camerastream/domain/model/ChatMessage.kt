package tech.estacionkus.camerastream.domain.model

data class ChatMessage(
    val id: String,
    val author: String,
    val content: String,
    val platform: Platform,
    val authorColor: String = "#9B59B6"
)
