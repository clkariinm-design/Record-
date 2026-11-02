package com.example.model

enum class AppTab {
    VIDEOS,
    TOOLS,
    SETTINGS
}

enum class MediaFilter {
    ALL,
    VIDEOS,
    SCREENSHOTS
}

enum class Resolution(val label: String, val width: Int, val height: Int) {
    RES_1080P("1080p (FHD)", 1080, 1920),
    RES_720P("720p (HD)", 720, 1280),
    RES_540P("540p (SD)", 540, 960),
    RES_480P("480p (Basic)", 480, 854)
}

enum class Fps(val value: Int, val label: String) {
    FPS_60(60, "60 FPS (Ultra Smooth)"),
    FPS_50(50, "50 FPS (High)"),
    FPS_30(30, "30 FPS (Standard)"),
    FPS_24(24, "24 FPS (Cinematic)")
}

enum class AudioSource(val label: String, val description: String) {
    MUTE("Mute", "Record video only without sound"),
    MIC("Microphone", "Capture your voice and external sounds"),
    INTERNAL("Internal Audio", "Record game or in-app sounds"),
    INTERNAL_AND_MIC("Internal & Mic", "Record both game sound and voice")
}

data class RecordingSettings(
    val resolution: Resolution = Resolution.RES_1080P,
    val fps: Fps = Fps.FPS_60,
    val bitrate: String = "Auto (Recommended)",
    val audioSource: AudioSource = AudioSource.INTERNAL_AND_MIC,
    val countdownSeconds: Int = 3,
    val orientation: String = "Auto",
    val showFloatingBall: Boolean = true,
    val hideBallWhileRecording: Boolean = false,
    val shakeToStop: Boolean = true,
    val keepRecordingWhenScreenOff: Boolean = false
)

data class RecordingItem(
    val id: String,
    val title: String,
    val filePath: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val resolution: String,
    val fps: Int,
    val timestamp: Long,
    val isScreenshot: Boolean = false,
    val thumbnailGradientIndex: Int = 0
) {
    val formattedDuration: String
        get() {
            if (isScreenshot) return "Photo"
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

    val formattedSize: String
        get() {
            val mb = sizeBytes.toDouble() / (1024 * 1024)
            return if (mb < 1.0) {
                val kb = sizeBytes / 1024
                "$kb KB"
            } else {
                String.format("%.1f MB", mb)
            }
        }
}

sealed class RecordingState {
    object Idle : RecordingState()
    data class Countdown(val secondsRemaining: Int) : RecordingState()
    data class Recording(val durationMs: Long, val isPaused: Boolean = false) : RecordingState()
    object Saving : RecordingState()
}
