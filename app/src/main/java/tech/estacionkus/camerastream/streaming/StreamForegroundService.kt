package tech.estacionkus.camerastream.streaming

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import tech.estacionkus.camerastream.MainActivity
import tech.estacionkus.camerastream.R

class StreamForegroundService : Service() {
    companion object {
        const val CHANNEL_ID = "stream_channel"
        const val NOTIF_ID = 1001
        const val ACTION_STOP = "ACTION_STOP_STREAM"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        startForeground(NOTIF_ID, buildNotification())
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        val stopIntent = Intent(this, StreamForegroundService::class.java).apply { action = ACTION_STOP }
        val stopPending = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("● CameraStream EN VIVO")
            .setContentText("Stream activo en segundo plano")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(openIntent)
            .addAction(android.R.drawable.ic_media_pause, "Detener", stopPending)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val chan = NotificationChannel(CHANNEL_ID, "Stream activo", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(chan)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
