package tech.estacionkus.camerastream.domain.features

/**
 * Feature flags for all 50 CameraStream features.
 * FREE = available on free plan
 * PAID = requires Starter/Pro/Agency
 * PRO  = requires Pro or Agency
 * AGENCY = requires Agency only
 */
enum class Feature(val tier: Tier, val displayName: String) {
    // FREE (10)
    RTMP_SINGLE(Tier.FREE, "RTMP 1 plataforma"),
    CAMERA_FLIP(Tier.FREE, "Flip de cámara"),
    OVERLAY_IMAGE_BASIC(Tier.FREE, "1 overlay de imagen"),
    MUTE_MIC(Tier.FREE, "Mute de micrófono"),
    LIVE_TIMER(Tier.FREE, "Cronómetro en vivo"),
    CHAT_SINGLE_READ(Tier.FREE, "Chat 1 plataforma (lectura)"),
    MANUAL_BITRATE(Tier.FREE, "Bitrate manual básico"),
    RECORD_720P(Tier.FREE, "Grabación local 720p"),
    AUTH_EMAIL(Tier.FREE, "Registro con email"),
    COUPON_REDEEM(Tier.FREE, "Canjear cupón creador"),

    // PAID - Starter+ (40)
    RTMP_MULTI(Tier.PAID, "Multistream 5 plataformas"),
    SRT_CALLER(Tier.PAID, "SRT saliente (caller)"),
    SRT_SERVER_LOCAL(Tier.PAID, "Servidor SRT local"),
    SRTLA_BONDING(Tier.PRO, "SRTLA bonding WiFi+datos"),
    ADAPTIVE_BITRATE(Tier.PAID, "Adaptive bitrate automático"),
    STREAM_1080P(Tier.PAID, "Stream 1080p 60fps"),
    RTMPS(Tier.PAID, "RTMPS (SSL)"),
    USB_TETHERING(Tier.PAID, "Stream por USB tethering"),
    STANDBY_MODE(Tier.PAID, "Modo Standby"),
    AUTO_RECONNECT(Tier.PAID, "Reconexión automática"),
    OVERLAY_VIDEO_NATIVE(Tier.PAID, "Overlays de video nativo"),
    LOWER_THIRDS_CUSTOM(Tier.PAID, "Lower thirds personalizados"),
    SCENE_SWITCHING(Tier.PRO, "Múltiples escenas"),
    CHROMA_KEY(Tier.PRO, "Chroma key / fondo verde"),
    IMAGE_PLAYLIST(Tier.PAID, "Playlist de imágenes"),
    OVERLAY_GIF(Tier.PAID, "Overlays GIF animados"),
    LOGO_WATERMARK(Tier.PAID, "Logo/watermark persistente"),
    COUNTDOWN_OVERLAY(Tier.PAID, "Countdown timer overlay"),
    SCORE_OVERLAY(Tier.PRO, "Marcador deportivo"),
    BROWSER_SOURCE(Tier.PRO, "Browser source overlay"),
    CAMERA_MANUAL_CONTROLS(Tier.PAID, "Control manual ISO/EXP/WB"),
    MANUAL_FOCUS(Tier.PAID, "Enfoque manual tap"),
    CINEMATIC_ZOOM(Tier.PAID, "Zoom suave cinematográfico"),
    MULTI_LENS(Tier.PAID, "Multi-lente (wide/tele/ultra)"),
    PORTRAIT_BOKEH(Tier.PRO, "Modo retrato con bokeh"),
    COLOR_FILTERS_LUT(Tier.PRO, "Filtros LUT de color"),
    VIDEO_STABILIZATION(Tier.PAID, "Estabilización de video"),
    NIGHT_MODE(Tier.PRO, "Modo nocturno"),
    CHAT_MULTI(Tier.PAID, "Chat multi-plataforma"),
    ALERTS_OVERLAY(Tier.PAID, "Alertas subs/donations"),
    TALKBACK_AUDIO(Tier.PRO, "Talkback audio retorno"),
    POLLS_OVERLAY(Tier.PRO, "Poll overlay en tiempo real"),
    VIEWER_COUNT_HUD(Tier.PAID, "Contador de viewers HUD"),
    WEBCAM_MODE(Tier.PRO, "Modo webcam para OBS/PC"),
    OBS_WEBSOCKET(Tier.PRO, "OBS Websocket control remoto"),
    WEB_CONTROL_PANEL(Tier.PRO, "Panel web de control remoto"),
    QR_SHARE_URL(Tier.PAID, "QR code para compartir URL"),
    RECORD_1080P(Tier.PRO, "Grabación 1080p 60fps"),
    RECORD_BACKGROUND(Tier.PRO, "Grabación en background"),
    EXPORT_CLOUD(Tier.PRO, "Export a Drive/OneDrive"),
    CLOUDFLARED_TUNNEL(Tier.PAID, "Túnel Cloudflared"),
    RTSP_INPUT(Tier.PRO, "Entrada RTSP (cámaras IP)"),
    MULTI_AUDIO_TRACK(Tier.PRO, "Pistas de audio múltiples"),
    STREAM_SCHEDULER(Tier.AGENCY, "Programador de streams"),
    WHITE_LABEL(Tier.AGENCY, "Branding personalizado"),
    API_ACCESS(Tier.AGENCY, "API access programática"),
    TEAM_ACCOUNTS(Tier.AGENCY, "Cuentas de equipo"),
    ANALYTICS_DASHBOARD(Tier.AGENCY, "Dashboard de analíticas"),
    PRIORITY_SUPPORT(Tier.AGENCY, "Soporte prioritario")
}

enum class Tier { FREE, PAID, PRO, AGENCY }
