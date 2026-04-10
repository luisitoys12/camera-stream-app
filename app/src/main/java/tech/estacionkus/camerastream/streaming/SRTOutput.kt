package tech.estacionkus.camerastream.streaming

class SRTOutput {
    private var socketHandle: Long = -1
    private var connected = false

    init { System.loadLibrary("camerastream") }

    fun connect(host: String, port: Int, latencyMs: Int): Boolean {
        socketHandle = nativeConnect(host, port, latencyMs)
        connected = socketHandle > 0
        return connected
    }

    fun send(data: ByteArray): Boolean {
        if (!connected) return false
        return nativeSend(socketHandle, data, data.size)
    }

    fun disconnect() {
        if (connected) {
            nativeDisconnect(socketHandle)
            socketHandle = -1
            connected = false
        }
    }

    val isConnected get() = connected

    private external fun nativeConnect(url: String, port: Int, latencyMs: Int): Long
    private external fun nativeSend(handle: Long, data: ByteArray, length: Int): Boolean
    private external fun nativeDisconnect(handle: Long)
}
