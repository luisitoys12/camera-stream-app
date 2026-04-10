package com.cushMedia.camerastream.model

data class StreamSettings(
    // SRT
    val srtHost: String = "",
    val srtStreamId: String = "",
    val srtLatency: Int = 200, // ms

    // YouTube
    val youtubeEnabled: Boolean = false,
    val youtubeRtmpUrl: String = "rtmp://a.rtmp.youtube.com/live2",
    val youtubeStreamKey: String = "",

    // Twitch
    val twitchEnabled: Boolean = false,
    val twitchRtmpUrl: String = "rtmp://live.twitch.tv/live",
    val twitchStreamKey: String = "",

    // Facebook
    val facebookEnabled: Boolean = false,
    val facebookRtmpUrl: String = "rtmps://live-api-s.facebook.com:443/rtmp",
    val facebookStreamKey: String = "",

    // Video
    val resolution: String = "1080p (1920x1080)",
    val videoBitrate: String = "2500",
    val audioBitrate: String = "128",
    val fps: Int = 30
)
