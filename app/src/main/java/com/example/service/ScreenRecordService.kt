package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.RecordingRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class ScreenRecordService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null
    private var elapsedMs: Long = 0L
    private var isPaused: Boolean = false

    // MediaProjection and recording components
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaRecorder: MediaRecorder? = null

    private var currentOutputFile: File? = null
    private var recordWidth: Int = 1080
    private var recordHeight: Int = 1920
    private var recordDpi: Int = 420
    private var recordFps: Int = 60
    private var recordBitrate: Int = 8_000_000
    private var recordAudio: Boolean = true

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START
        when (action) {
            ACTION_START_PROJECTION -> {
                val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, 0) ?: 0
                @Suppress("DEPRECATION")
                val resultData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent?.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
                } else {
                    intent?.getParcelableExtra(EXTRA_RESULT_DATA)
                }

                recordWidth = intent?.getIntExtra(EXTRA_WIDTH, 1080) ?: 1080
                recordHeight = intent?.getIntExtra(EXTRA_HEIGHT, 1920) ?: 1920
                recordDpi = intent?.getIntExtra(EXTRA_DPI, 420) ?: 420
                recordFps = intent?.getIntExtra(EXTRA_FPS, 60) ?: 60
                recordBitrate = intent?.getIntExtra(EXTRA_BITRATE, 8_000_000) ?: 8_000_000
                recordAudio = intent?.getBooleanExtra(EXTRA_AUDIO, true) ?: true

                // Make dimensions even numbers (required by AVC/H264 encoders)
                if (recordWidth % 2 != 0) recordWidth--
                if (recordHeight % 2 != 0) recordHeight--

                startRecordingWithProjection(resultCode, resultData)
            }
            ACTION_PAUSE -> pauseRecording()
            ACTION_RESUME -> resumeRecording()
            ACTION_STOP -> stopRecording()
        }
        return START_NOT_STICKY
    }

    private fun startRecordingWithProjection(resultCode: Int, resultData: Intent?) {
        _isRecordingFlow.value = true
        _isPausedFlow.value = false
        _elapsedTimeMsFlow.value = 0L
        elapsedMs = 0L
        isPaused = false

        // 1. Mandatory on Android 14+: startForeground MUST be called BEFORE getMediaProjection
        val notification = buildNotification(0L, false)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting foreground service", e)
            try {
                startForeground(NOTIFICATION_ID, notification)
            } catch (_: Exception) {}
        }

        // 2. Prepare destination output file
        val moviesDir = File(filesDir, "recordings")
        if (!moviesDir.exists()) moviesDir.mkdirs()
        val fileName = "XRecorder_${System.currentTimeMillis()}.mp4"
        currentOutputFile = File(moviesDir, fileName)

        // 3. Initialize MediaRecorder
        try {
            @Suppress("DEPRECATION")
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                MediaRecorder()
            }

            mediaRecorder?.apply {
                if (recordAudio) {
                    try {
                        setAudioSource(MediaRecorder.AudioSource.MIC)
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not set audio source to MIC: ${e.message}")
                    }
                }
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile(currentOutputFile?.absolutePath)
                setVideoSize(recordWidth, recordHeight)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                if (recordAudio) {
                    try {
                        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                        setAudioEncodingBitRate(128_000)
                        setAudioSamplingRate(44_100)
                    } catch (e: Exception) {
                        Log.w(TAG, "Audio encoder setup fallback: ${e.message}")
                    }
                }
                setVideoEncodingBitRate(recordBitrate)
                setVideoFrameRate(recordFps)
                prepare()
            }
        } catch (e: Exception) {
            Log.e(TAG, "MediaRecorder preparation error", e)
        }

        // 4. Obtain MediaProjection and create VirtualDisplay
        if (resultCode != 0 && resultData != null) {
            val mpManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
            try {
                mediaProjection = mpManager?.getMediaProjection(resultCode, resultData)

                // MANDATORY on Android 14+: registerCallback before createVirtualDisplay
                mediaProjection?.registerCallback(object : MediaProjection.Callback() {
                    override fun onStop() {
                        Log.d(TAG, "MediaProjection stopped by system")
                        stopRecording()
                    }
                }, Handler(Looper.getMainLooper()))

                virtualDisplay = mediaProjection?.createVirtualDisplay(
                    "XRecorderScreenCapture",
                    recordWidth,
                    recordHeight,
                    recordDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mediaRecorder?.surface,
                    null,
                    null
                )

                mediaRecorder?.start()
                Log.d(TAG, "MediaRecorder started successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error initiating screen capture via MediaProjection", e)
            }
        }

        startTimer()
    }

    private fun pauseRecording() {
        if (!isPaused) {
            isPaused = true
            _isPausedFlow.value = true
            timerJob?.cancel()
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mediaRecorder?.pause()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error pausing MediaRecorder", e)
            }
            updateNotification()
        }
    }

    private fun resumeRecording() {
        if (isPaused) {
            isPaused = false
            _isPausedFlow.value = false
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mediaRecorder?.resume()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error resuming MediaRecorder", e)
            }
            startTimer()
            updateNotification()
        }
    }

    private fun stopRecording() {
        timerJob?.cancel()
        _isRecordingFlow.value = false
        _isPausedFlow.value = false

        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            Log.w(TAG, "MediaRecorder stop note: ${e.message}")
        }

        try {
            mediaRecorder?.reset()
            mediaRecorder?.release()
        } catch (_: Exception) {}
        mediaRecorder = null

        try {
            virtualDisplay?.release()
        } catch (_: Exception) {}
        virtualDisplay = null

        try {
            mediaProjection?.stop()
        } catch (_: Exception) {}
        mediaProjection = null

        // Add newly saved file to repository
        val finalFile = currentOutputFile
        val finalDuration = elapsedMs.coerceAtLeast(1000L)
        if (finalFile != null) {
            serviceScope.launch {
                try {
                    val repo = RecordingRepository(applicationContext)
                    val resLabel = "${recordHeight}P"
                    repo.addRecording(
                        durationMs = finalDuration,
                        isScreenshot = false,
                        resolutionLabel = resLabel,
                        fpsVal = recordFps
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error recording item saved to repository", e)
                }
            }
        }

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
                if (elapsedMs % 4000L == 0L) {
                    updateNotification()
                }
            }
        }
    }

    private fun updateNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID, buildNotification(elapsedMs, isPaused))
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

        val title = if (paused) "XRecorder (Paused) • $formattedTime" else "Recording Screen • $formattedTime"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("Screen capturing active outside app • Tap to manage")
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
                description = "Shows live recording status and outside-app controls"
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
        private const val TAG = "ScreenRecordService"
        const val CHANNEL_ID = "xrecorder_recording_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.service.action.START"
        const val ACTION_START_PROJECTION = "com.example.service.action.START_PROJECTION"
        const val ACTION_PAUSE = "com.example.service.action.PAUSE"
        const val ACTION_RESUME = "com.example.service.action.RESUME"
        const val ACTION_STOP = "com.example.service.action.STOP"

        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_RESULT_DATA = "extra_result_data"
        const val EXTRA_WIDTH = "extra_width"
        const val EXTRA_HEIGHT = "extra_height"
        const val EXTRA_DPI = "extra_dpi"
        const val EXTRA_FPS = "extra_fps"
        const val EXTRA_BITRATE = "extra_bitrate"
        const val EXTRA_AUDIO = "extra_audio"

        private val _isRecordingFlow = MutableStateFlow(false)
        val isRecordingFlow: StateFlow<Boolean> = _isRecordingFlow.asStateFlow()

        private val _isPausedFlow = MutableStateFlow(false)
        val isPausedFlow: StateFlow<Boolean> = _isPausedFlow.asStateFlow()

        private val _elapsedTimeMsFlow = MutableStateFlow(0L)
        val elapsedTimeMsFlow: StateFlow<Long> = _elapsedTimeMsFlow.asStateFlow()

        fun startWithProjection(
            context: Context,
            resultCode: Int,
            resultData: Intent,
            width: Int,
            height: Int,
            densityDpi: Int,
            fps: Int = 60,
            bitrate: Int = 8_000_000,
            audio: Boolean = true
        ) {
            try {
                val intent = Intent(context, ScreenRecordService::class.java).apply {
                    action = ACTION_START_PROJECTION
                    putExtra(EXTRA_RESULT_CODE, resultCode)
                    putExtra(EXTRA_RESULT_DATA, resultData)
                    putExtra(EXTRA_WIDTH, width)
                    putExtra(EXTRA_HEIGHT, height)
                    putExtra(EXTRA_DPI, densityDpi)
                    putExtra(EXTRA_FPS, fps)
                    putExtra(EXTRA_BITRATE, bitrate)
                    putExtra(EXTRA_AUDIO, audio)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Could not start ScreenRecordService with MediaProjection", e)
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
