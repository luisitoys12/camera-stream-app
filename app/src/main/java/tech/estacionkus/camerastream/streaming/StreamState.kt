package tech.estacionkus.camerastream.streaming

/**
 * Estado del stream SRT/RTMP.
 * Enum puro JVM — sin dependencias Android ni JNI.
 */
enum class StreamState {
    IDLE,
    CONNECTING,
    LIVE,
    RECONNECTING,
    ERROR
}
