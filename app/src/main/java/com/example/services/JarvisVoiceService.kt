package com.example.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.voice.WakeWordDetector

class JarvisVoiceService : Service() {

    companion object {
        const val CHANNEL_ID = "jarvis_voice_service_channel"
        const val NOTIFICATION_ID = 2001
        const val ACTION_START = "com.example.jarvis.action.START"
        const val ACTION_STOP = "com.example.jarvis.action.STOP"
        const val ACTION_WAKE = "com.example.jarvis.action.WAKE"

        var isRunning: Boolean = false
            private set

        fun startService(context: Context) {
            val intent = Intent(context, JarvisVoiceService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, JarvisVoiceService::class.java).apply {
                action = ACTION_STOP
            }
            context.stopService(intent)
            isRunning = false
        }
    }

    private var wakeWordDetector: WakeWordDetector? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        wakeWordDetector = WakeWordDetector(this, "hey jarvis").apply {
            onWakeWordDetected = {
                // Wake up app and bring to front
                val launchIntent = Intent(this@JarvisVoiceService, MainActivity::class.java).apply {
                    action = ACTION_WAKE
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(launchIntent)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            wakeWordDetector?.stopMonitoring()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        isRunning = true

        wakeWordDetector?.startMonitoring()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, JarvisVoiceService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("JARVIS — Active & Listening")
            .setContentText("Say \"Hey Jarvis\" or tap to talk. Assistant is ready.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(0, "Stop Assistant", stopPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "JARVIS Wake-Word & Voice Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors for 'Hey Jarvis' hands-free activation"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        wakeWordDetector?.stopMonitoring()
    }
}
