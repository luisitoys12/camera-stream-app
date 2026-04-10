package tech.estacionkus.camerastream.streaming

import android.content.Context
import com.NodeMedia.NodeMediaClient

data class RTMPDestination(
    val url: String,       // rtmp://a.rtmp.youtube.com/live2
    val streamKey: String,
    val label: String
)

class RTMPOutput(private val context: Context) {
    private val clients = mutableMapOf<String, NodeMediaClient>()

    fun connect(destinations: List<RTMPDestination>, width: Int, height: Int, fps: Int, bitrate: Int) {
        destinations.forEach { dest ->
            val fullUrl = "${dest.url}/${dest.streamKey}"
            val client = NodeMediaClient(context).apply {
                setInputUrl(fullUrl)
                setVideoParam(width, height, fps, bitrate * 1000)
                setAudioParam(44100, 2, 128 * 1000)
                start()
            }
            clients[dest.label] = client
        }
    }

    fun disconnect() {
        clients.values.forEach { it.stop() }
        clients.clear()
    }

    fun isConnected(label: String): Boolean = clients[label] != null
}
