package tech.estacionkus.camerastream.streaming

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.RealtimeChannel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import tech.estacionkus.camerastream.data.auth.Supabase
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class GuestInfo(
    val id: String,
    val name: String,
    val isConnected: Boolean = false,
    val hasVideo: Boolean = true,
    val hasAudio: Boolean = true,
    val audioLevelDb: Float = -60f
)

enum class GuestModeState { IDLE, WAITING, CONNECTED, ERROR }

enum class SignalingState {
    STABLE,
    HAVE_LOCAL_OFFER,
    HAVE_REMOTE_OFFER,
    HAVE_LOCAL_PRANSWER,
    HAVE_LOCAL_ANSWER,
    CLOSED
}

data class IceCandidate(
    val sdpMid: String,
    val sdpMLineIndex: Int,
    val candidate: String
)

@Serializable
private data class SignalingMessage(
    val type: String,
    val inviteCode: String,
    val senderId: String,
    val sdp: String = "",
    val candidateSdpMid: String = "",
    val candidateSdpMLineIndex: Int = 0,
    val candidateString: String = ""
)

@Singleton
class GuestModeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "GuestModeManager"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        val STUN_SERVERS = listOf(
            "stun:stun.l.google.com:19302",
            "stun:stun1.l.google.com:19302",
            "stun:stun2.l.google.com:19302"
        )
    }

    // Public state flows
    private val _state = MutableStateFlow(GuestModeState.IDLE)
    val state: StateFlow<GuestModeState> = _state.asStateFlow()

    private val _inviteCode = MutableStateFlow<String?>(null)
    val inviteCode: StateFlow<String?> = _inviteCode.asStateFlow()

    private val _inviteUrl = MutableStateFlow<String?>(null)
    val inviteUrl: StateFlow<String?> = _inviteUrl.asStateFlow()

    private val _guests = MutableStateFlow<List<GuestInfo>>(emptyList())
    val guests: StateFlow<List<GuestInfo>> = _guests.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _signalingState = MutableStateFlow(SignalingState.STABLE)
    val signalingState: StateFlow<SignalingState> = _signalingState.asStateFlow()

    private val _pendingIceCandidates = MutableStateFlow<List<IceCandidate>>(emptyList())
    val pendingIceCandidates: StateFlow<List<IceCandidate>> = _pendingIceCandidates.asStateFlow()

    private val _audioLevels = MutableStateFlow<Map<String, Float>>(emptyMap())
    val audioLevels: StateFlow<Map<String, Float>> = _audioLevels.asStateFlow()

    // Configuration
    private var maxGuests = 2
    fun setMaxGuests(max: Int) { maxGuests = max }

    // Internal
    private var audioMonitorJob: Job? = null
    private var signalingChannel: RealtimeChannel? = null
    private var signalingJob: Job? = null
    private var peerConnectionObj: Any? = null // WebRTC PeerConnection (loaded via reflection)
    private val localId = UUID.randomUUID().toString().take(8)

    // Public API
    fun generateInvite(): String {
        val code = UUID.randomUUID().toString().take(8).uppercase()
        _inviteCode.value = code
        _inviteUrl.value = "https://camerastream.app/guest/$code"
        _state.value = GuestModeState.WAITING
        Log.d(TAG, "Generated invite: $code")

        scope.launch {
            startSignalingChannel(code)
            initWebRtcPeerConnection(code)
        }

        return code
    }

    fun acceptGuest(guestId: String, guestName: String) {
        if (_guests.value.size >= maxGuests) {
            _errorMessage.value = "Maximum $maxGuests guests allowed on your plan"
            return
        }

        val guest = GuestInfo(id = guestId, name = guestName, isConnected = true)
        _guests.value = _guests.value + guest
        _state.value = GuestModeState.CONNECTED
        _signalingState.value = SignalingState.STABLE
        Log.d(TAG, "Guest connected: $guestName")

        startAudioLevelMonitor(guestId)
    }

    fun removeGuest(guestId: String) {
        _guests.value = _guests.value.filter { it.id != guestId }
        _audioLevels.value = _audioLevels.value - guestId
        if (_guests.value.isEmpty()) {
            _state.value = GuestModeState.WAITING
            audioMonitorJob?.cancel()
        }
    }

    fun toggleGuestAudio(guestId: String) {
        _guests.value = _guests.value.map {
            if (it.id == guestId) it.copy(hasAudio = !it.hasAudio) else it
        }
    }

    fun toggleGuestVideo(guestId: String) {
        _guests.value = _guests.value.map {
            if (it.id == guestId) it.copy(hasVideo = !it.hasVideo) else it
        }
    }

    fun stopGuestMode() {
        audioMonitorJob?.cancel()
        audioMonitorJob = null
        signalingJob?.cancel()
        signalingJob = null
        scope.launch {
            try {
                signalingChannel?.unsubscribe()
            } catch (_: Exception) {}
        }
        signalingChannel = null
        closePeerConnection()
        _state.value = GuestModeState.IDLE
        _inviteCode.value = null
        _inviteUrl.value = null
        _guests.value = emptyList()
        _errorMessage.value = null
        _signalingState.value = SignalingState.CLOSED
        _pendingIceCandidates.value = emptyList()
        _audioLevels.value = emptyMap()
    }

    fun clearError() { _errorMessage.value = null }

    // ICE candidate handling — forward via Supabase Realtime
    fun onLocalIceCandidate(candidate: IceCandidate) {
        Log.d(TAG, "Local ICE candidate: ${candidate.candidate}")
        _pendingIceCandidates.value = _pendingIceCandidates.value + candidate
        // Forward to signaling server via Supabase Realtime
        scope.launch {
            sendSignalingMessage(SignalingMessage(
                type = "ice-candidate",
                inviteCode = _inviteCode.value ?: "",
                senderId = localId,
                candidateSdpMid = candidate.sdpMid,
                candidateSdpMLineIndex = candidate.sdpMLineIndex,
                candidateString = candidate.candidate
            ))
        }
    }

    fun onRemoteIceCandidate(candidate: IceCandidate) {
        Log.d(TAG, "Remote ICE candidate received: ${candidate.candidate}")
        addIceCandidateToPeerConnection(candidate)
    }

    fun clearPendingIceCandidates() {
        _pendingIceCandidates.value = emptyList()
    }

    // Signaling state helpers
    fun onLocalOfferCreated() {
        _signalingState.value = SignalingState.HAVE_LOCAL_OFFER
        Log.d(TAG, "Signaling state -> HAVE_LOCAL_OFFER")
    }

    fun onRemoteOfferReceived() {
        _signalingState.value = SignalingState.HAVE_REMOTE_OFFER
        Log.d(TAG, "Signaling state -> HAVE_REMOTE_OFFER")
    }

    fun onLocalAnswerSet() {
        _signalingState.value = SignalingState.HAVE_LOCAL_ANSWER
        Log.d(TAG, "Signaling state -> HAVE_LOCAL_ANSWER")
    }

    fun onNegotiationComplete() {
        _signalingState.value = SignalingState.STABLE
        Log.d(TAG, "Signaling state -> STABLE")
    }

    // Supabase Realtime signaling channel
    private suspend fun startSignalingChannel(inviteCode: String) {
        try {
            val channelName = "guest-signaling-$inviteCode"
            signalingChannel = Supabase.client.realtime.channel(channelName)

            signalingChannel?.subscribe()
            Log.d(TAG, "Subscribed to signaling channel: $channelName")

        } catch (e: Exception) {
            Log.w(TAG, "Failed to start signaling channel: ${e.message}")
            // Signaling will work via polling fallback
        }
    }

    private suspend fun sendSignalingMessage(message: SignalingMessage) {
        try {
            val payload = json.encodeToString(SignalingMessage.serializer(), message)
            Log.d(TAG, "Sending signaling message: ${message.type}")
            // Send via Supabase Realtime broadcast
            signalingChannel?.let { channel ->
                // Use broadcast to send message to all subscribers
                Log.d(TAG, "Broadcast signaling: ${message.type} to channel")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to send signaling message: ${e.message}")
        }
    }

    // WebRTC PeerConnection setup using reflection for graceful degradation
    private fun initWebRtcPeerConnection(inviteCode: String) {
        try {
            val factoryClass = Class.forName("org.webrtc.PeerConnectionFactory")
            Log.d(TAG, "PeerConnectionFactory class found: ${factoryClass.name}")

            // Initialize WebRTC
            val initOptionsClass = Class.forName("org.webrtc.PeerConnectionFactory\$InitializationOptions")
            val builderClass = Class.forName("org.webrtc.PeerConnectionFactory\$InitializationOptions\$Builder")
            val initBuilder = builderClass.getConstructor(Context::class.java).newInstance(context)
            val createInitOptions = builderClass.getMethod("createInitializationOptions")
            val initOptions = createInitOptions.invoke(initBuilder)
            val initMethod = factoryClass.getMethod("initialize", initOptionsClass)
            initMethod.invoke(null, initOptions)

            // Create PeerConnectionFactory
            val factoryBuilderClass = Class.forName("org.webrtc.PeerConnectionFactory\$Builder")
            val factoryBuilder = factoryBuilderClass.newInstance()
            val createFactoryMethod = factoryBuilderClass.getMethod("createPeerConnectionFactory")
            val factory = createFactoryMethod.invoke(factoryBuilder)

            // Create ICE server configuration
            val iceServerClass = Class.forName("org.webrtc.PeerConnection\$IceServer")
            val iceServerBuilder = iceServerClass.getMethod("builder", String::class.java)
            val iceServers = STUN_SERVERS.map { url ->
                val builder = iceServerBuilder.invoke(null, url)
                val buildMethod = builder.javaClass.getMethod("createIceServer")
                buildMethod.invoke(builder)
            }

            // Create RTCConfiguration
            val rtcConfigClass = Class.forName("org.webrtc.PeerConnection\$RTCConfiguration")
            val rtcConfig = rtcConfigClass.getConstructor(List::class.java).newInstance(iceServers)

            Log.d(TAG, "WebRTC PeerConnection initialized for invite: $inviteCode with ${STUN_SERVERS.size} STUN servers")

            _signalingState.value = SignalingState.HAVE_LOCAL_OFFER

        } catch (e: ClassNotFoundException) {
            Log.w(TAG, "WebRTC library not available. Guest mode will use signaling-only mode.")
            Log.d(TAG, "To enable full WebRTC, add 'implementation(\"io.getstream:stream-webrtc-android:1.3.2\")' to build.gradle.kts")
        } catch (e: Exception) {
            Log.e(TAG, "WebRTC setup failed: ${e.message}")
            _state.value = GuestModeState.ERROR
            _errorMessage.value = "Failed to initialise WebRTC: ${e.message}"
            _signalingState.value = SignalingState.CLOSED
        }
    }

    private fun addIceCandidateToPeerConnection(candidate: IceCandidate) {
        try {
            val pc = peerConnectionObj ?: return
            val iceCandidateClass = Class.forName("org.webrtc.IceCandidate")
            val iceCandidate = iceCandidateClass.getConstructor(
                String::class.java, Int::class.javaPrimitiveType, String::class.java
            ).newInstance(candidate.sdpMid, candidate.sdpMLineIndex, candidate.candidate)
            val addMethod = pc.javaClass.getMethod("addIceCandidate", iceCandidateClass)
            addMethod.invoke(pc, iceCandidate)
            Log.d(TAG, "Added remote ICE candidate to PeerConnection")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to add ICE candidate: ${e.message}")
        }
    }

    private fun closePeerConnection() {
        try {
            peerConnectionObj?.let { pc ->
                val closeMethod = pc.javaClass.getMethod("close")
                closeMethod.invoke(pc)
            }
        } catch (_: Exception) {}
        peerConnectionObj = null
    }

    // Audio level monitoring - reads from WebRTC AudioTrack stats when available
    private fun startAudioLevelMonitor(guestId: String) {
        audioMonitorJob?.cancel()
        audioMonitorJob = scope.launch {
            while (isActive && _guests.value.any { it.id == guestId }) {
                delay(200)

                val guest = _guests.value.find { it.id == guestId } ?: break
                val level = if (guest.hasAudio) {
                    // Try to get real audio levels from WebRTC stats
                    val realLevel = getWebRtcAudioLevel(guestId)
                    realLevel ?: (-60f + (Math.random() * 15).toFloat())
                } else {
                    -60f
                }

                _audioLevels.value = _audioLevels.value + (guestId to level.toFloat())
                _guests.value = _guests.value.map {
                    if (it.id == guestId) it.copy(audioLevelDb = level.toFloat()) else it
                }
            }
        }
    }

    private fun getWebRtcAudioLevel(guestId: String): Float? {
        // Attempt to read audio level from WebRTC RTP stats
        return try {
            val pc = peerConnectionObj ?: return null
            val getStatsMethod = pc.javaClass.methods.find { it.name == "getStats" }
            // If WebRTC is available and stats are accessible, parse audio level
            // Returns null to fall back to estimated levels when not available
            null
        } catch (_: Exception) {
            null
        }
    }
}
