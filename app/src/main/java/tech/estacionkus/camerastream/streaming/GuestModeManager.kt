package tech.estacionkus.camerastream.streaming

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class GuestInfo(
    val id: String,
    val name: String,
    val isConnected: Boolean = false,
    val hasVideo: Boolean = true,
    val hasAudio: Boolean = true,
    val audioLevelDb: Float = -60f  // approximate audio level in dBFS
)

enum class GuestModeState { IDLE, WAITING, CONNECTED, ERROR }

/** WebRTC signaling state mirroring RTCSignalingState values */
enum class SignalingState {
    STABLE,
    HAVE_LOCAL_OFFER,
    HAVE_REMOTE_OFFER,
    HAVE_LOCAL_PRANSWER,
    HAVE_LOCAL_ANSWER,
    CLOSED
}

/** Represents a single ICE candidate stub */
data class IceCandidate(
    val sdpMid: String,
    val sdpMLineIndex: Int,
    val candidate: String
)

@Singleton
class GuestModeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "GuestModeManager"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // -------------------------------------------------------------------------
    // Public state flows
    // -------------------------------------------------------------------------

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

    /** Collected ICE candidates waiting to be sent to the remote peer */
    private val _pendingIceCandidates = MutableStateFlow<List<IceCandidate>>(emptyList())
    val pendingIceCandidates: StateFlow<List<IceCandidate>> = _pendingIceCandidates.asStateFlow()

    /** Audio levels keyed by guest id (dBFS approximation, -60 = silence, 0 = max) */
    private val _audioLevels = MutableStateFlow<Map<String, Float>>(emptyMap())
    val audioLevels: StateFlow<Map<String, Float>> = _audioLevels.asStateFlow()

    // -------------------------------------------------------------------------
    // Configuration
    // -------------------------------------------------------------------------

    private var maxGuests = 2
    fun setMaxGuests(max: Int) { maxGuests = max }

    // -------------------------------------------------------------------------
    // Internal
    // -------------------------------------------------------------------------

    private var audioMonitorJob: Job? = null

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    fun generateInvite(): String {
        val code = UUID.randomUUID().toString().take(8).uppercase()
        _inviteCode.value = code
        _inviteUrl.value = "https://camerastream.app/guest/$code"
        _state.value = GuestModeState.WAITING
        Log.d(TAG, "Generated invite: $code")

        scope.launch {
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

    // -------------------------------------------------------------------------
    // ICE candidate handling stubs
    // -------------------------------------------------------------------------

    /**
     * Called when the local peer connection generates a new ICE candidate.
     * In a full implementation this would be forwarded to the signaling server.
     */
    fun onLocalIceCandidate(candidate: IceCandidate) {
        Log.d(TAG, "Local ICE candidate: ${candidate.candidate}")
        _pendingIceCandidates.value = _pendingIceCandidates.value + candidate
        // TODO: forward to signaling server
    }

    /**
     * Called when a remote ICE candidate is received from the signaling server.
     * In a full implementation this would be added to the PeerConnection.
     */
    fun onRemoteIceCandidate(candidate: IceCandidate) {
        Log.d(TAG, "Remote ICE candidate received: ${candidate.candidate}")
        // TODO: peerConnection?.addIceCandidate(candidate)
    }

    /** Clear pending candidates after they have been flushed to the signaling server */
    fun clearPendingIceCandidates() {
        _pendingIceCandidates.value = emptyList()
    }

    // -------------------------------------------------------------------------
    // Signaling state helpers
    // -------------------------------------------------------------------------

    /** Call this when an SDP offer is created locally */
    fun onLocalOfferCreated() {
        _signalingState.value = SignalingState.HAVE_LOCAL_OFFER
        Log.d(TAG, "Signaling state -> HAVE_LOCAL_OFFER")
    }

    /** Call this when a remote offer SDP is received */
    fun onRemoteOfferReceived() {
        _signalingState.value = SignalingState.HAVE_REMOTE_OFFER
        Log.d(TAG, "Signaling state -> HAVE_REMOTE_OFFER")
    }

    /** Call this after setting the local answer SDP */
    fun onLocalAnswerSet() {
        _signalingState.value = SignalingState.HAVE_LOCAL_ANSWER
        Log.d(TAG, "Signaling state -> HAVE_LOCAL_ANSWER")
    }

    /** Call this when negotiation is complete and the connection is stable */
    fun onNegotiationComplete() {
        _signalingState.value = SignalingState.STABLE
        Log.d(TAG, "Signaling state -> STABLE")
    }

    // -------------------------------------------------------------------------
    // WebRTC PeerConnection setup stub
    // -------------------------------------------------------------------------

    /**
     * Attempts to initialise a WebRTC PeerConnection using org.webrtc classes.
     * The try/catch around ClassNotFoundException allows the app to run normally
     * even when the WebRTC library is not present at runtime (e.g., in flavours
     * that don't include it).
     */
    private fun initWebRtcPeerConnection(inviteCode: String) {
        try {
            // Attempt to load the WebRTC PeerConnectionFactory via reflection so
            // the class is referenced by name rather than directly, enabling
            // graceful degradation when the dependency is absent.
            val factoryClass = Class.forName("org.webrtc.PeerConnectionFactory")
            Log.d(TAG, "PeerConnectionFactory class found: ${factoryClass.name}")

            // In a real implementation we would:
            // 1. Call PeerConnectionFactory.initialize(InitializationOptions)
            // 2. Create a PeerConnectionFactory instance
            // 3. Create audio/video tracks
            // 4. Create a PeerConnection with ICE server configuration
            // 5. Add the tracks to the peer connection
            // 6. Create an offer SDP and post it to the signaling server

            _signalingState.value = SignalingState.HAVE_LOCAL_OFFER
            Log.d(TAG, "WebRTC PeerConnection stub initialised for invite: $inviteCode")

        } catch (e: ClassNotFoundException) {
            // WebRTC library not present at runtime — fall back to a stub implementation
            Log.w(TAG, "WebRTC library not available (ClassNotFoundException). Running in stub mode.")
            Log.d(TAG, "WebRTC signaling stub ready for code: $inviteCode")
            // State stays WAITING — the UI can show a "no WebRTC" message if needed

        } catch (e: Exception) {
            Log.e(TAG, "WebRTC setup failed: ${e.message}")
            _state.value = GuestModeState.ERROR
            _errorMessage.value = "Failed to initialise WebRTC: ${e.message}"
            _signalingState.value = SignalingState.CLOSED
        }
    }

    // -------------------------------------------------------------------------
    // Audio level monitoring stub
    // -------------------------------------------------------------------------

    /**
     * Starts a coroutine that periodically simulates audio level readings for
     * the given guest. In a real implementation these would come from the
     * WebRTC AudioTrack.setObserver / AudioTrackSink callbacks.
     */
    private fun startAudioLevelMonitor(guestId: String) {
        audioMonitorJob?.cancel()
        audioMonitorJob = scope.launch {
            while (isActive && _guests.value.any { it.id == guestId }) {
                delay(200) // poll ~5 times per second

                // Stub: generate a plausible-looking audio level
                // Real impl: read from org.webrtc.AudioTrack or RTP stats
                val guest = _guests.value.find { it.id == guestId } ?: break
                val level = if (guest.hasAudio) {
                    // Simulate voice activity with some noise floor variation
                    val noise = (-60f + (Math.random() * 15).toFloat())
                    noise
                } else {
                    -60f  // muted = silence floor
                }

                _audioLevels.value = _audioLevels.value + (guestId to level)
                _guests.value = _guests.value.map {
                    if (it.id == guestId) it.copy(audioLevelDb = level) else it
                }
            }
        }
    }
}
