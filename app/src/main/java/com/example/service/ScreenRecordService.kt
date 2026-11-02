package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScreenRecordService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null
    private var elapsedMs: Long = 0L
    private var isPaused: Boolean = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START
        when (action) {
            ACTION_START -> startRecording()
            ACTION_PAUSE -> pauseRecording()
            ACTION_RESUME -> resumeRecording()
            ACTION_STOP -> stopRecording()
        }
        return START_NOT_STICKY
    }

    private fun startRecording() {
        _isRecordingFlow.value = true
        _isPausedFlow.value = false
        _elapsedTimeMsFlow.value = 0L
        elapsedMs = 0L
        isPaused = false

        try {
            val notification = buildNotification(0L, false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                    )
                } catch (secEx: SecurityException) {
                    // Fallback to standard foreground if MediaProjection token is pending
                    startForeground(NOTIFICATION_ID, notification)
                }
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            // Never crash the application if foreground service notification fails in emulator
        }

        startTimer()
    }

    private fun pauseRecording() {
        isPaused = true
        _isPausedFlow.value = true
        timerJob?.cancel()
        updateNotification()
    }

    private fun resumeRecording() {
        isPaused = false
        _isPausedFlow.value = false
        startTimer()
        updateNotification()
    }

    private fun stopRecording() {
        timerJob?.cancel()
        _isRecordingFlow.value = false
        _isPausedFlow.value = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                delay(1000L)
                elapsedMs += 1000L
                _elapsedTimeMsFlow.value = elapsedMs
                if (elapsedMs % 5000L == 0L) {
                    updateNotification()
                }
            }
        }
    }

    private fun updateNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(elapsedMs, isPaused))
    }

    private fun buildNotification(timeMs: Long, paused: Boolean): Notification {
        val appIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, ScreenRecordService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleIntent = Intent(this, ScreenRecordService::class.java).apply {
            action = if (paused) ACTION_RESUME else ACTION_PAUSE
        }
        val togglePendingIntent = PendingIntent.getService(
            this,
            2,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val totalSec = timeMs / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        val formattedTime = String.format("%02d:%02d", min, sec)

        val title = if (paused) "XRecorder (Paused) - $formattedTime" else "XRecorder Recording - $formattedTime"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("Tap to open XRecorder • High Quality 1080P")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(contentPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(
                android.R.drawable.ic_media_pause,
                if (paused) "Resume" else "Pause",
                togglePendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop",
                stopPendingIntent
            )
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "XRecorder Screen Recording",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live recording status and quick controls"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val CHANNEL_ID = "xrecorder_recording_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.service.action.START"
        const val ACTION_PAUSE = "com.example.service.action.PAUSE"
        const val ACTION_RESUME = "com.example.service.action.RESUME"
        const val ACTION_STOP = "com.example.service.action.STOP"

        private val _isRecordingFlow = MutableStateFlow(false)
        val isRecordingFlow: StateFlow<Boolean> = _isRecordingFlow.asStateFlow()

        private val _isPausedFlow = MutableStateFlow(false)
        val isPausedFlow: StateFlow<Boolean> = _isPausedFlow.asStateFlow()

        private val _elapsedTimeMsFlow = MutableStateFlow(0L)
        val elapsedTimeMsFlow: StateFlow<Long> = _elapsedTimeMsFlow.asStateFlow()

        fun start(context: Context) {
            try {
                val intent = Intent(context, ScreenRecordService::class.java).apply {
                    action = ACTION_START
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                // Prevent app crash if background service start is restricted by system
            }
        }

        fun pause(context: Context) {
            try {
                val intent = Intent(context, ScreenRecordService::class.java).apply {
                    action = ACTION_PAUSE
                }
                context.startService(intent)
            } catch (e: Exception) {}
        }

        fun resume(context: Context) {
            try {
                val intent = Intent(context, ScreenRecordService::class.java).apply {
                    action = ACTION_RESUME
                }
                context.startService(intent)
            } catch (e: Exception) {}
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, ScreenRecordService::class.java).apply {
                    action = ACTION_STOP
                }
                context.startService(intent)
            } catch (e: Exception) {}
        }
    }
}
