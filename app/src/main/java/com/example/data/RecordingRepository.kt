package com.example.data

import android.content.Context
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.UUID

class RecordingRepository(private val context: Context) {

    private val _recordings = MutableStateFlow<List<RecordingItem>>(emptyList())
    val recordings: StateFlow<List<RecordingItem>> = _recordings.asStateFlow()

    private val _settings = MutableStateFlow(RecordingSettings())
    val settings: StateFlow<RecordingSettings> = _settings.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        // Create initial starter recordings so the user immediately sees XRecorder's full power
        val sampleItems = listOf(
            RecordingItem(
                id = UUID.randomUUID().toString(),
                title = "XRecorder_20261102_Gameplay_PUBG.mp4",
                filePath = "/storage/emulated/0/Movies/XRecorder/XRecorder_20261102_Gameplay_PUBG.mp4",
                durationMs = 145_000L, // 02:25
                sizeBytes = 46_800_000L, // ~44.6 MB
                resolution = "1080P",
                fps = 60,
                timestamp = System.currentTimeMillis() - 3600_000 * 2,
                isScreenshot = false,
                thumbnailGradientIndex = 0
            ),
            RecordingItem(
                id = UUID.randomUUID().toString(),
                title = "XRecorder_20261102_Tutorial_Compose.mp4",
                filePath = "/storage/emulated/0/Movies/XRecorder/XRecorder_20261102_Tutorial_Compose.mp4",
                durationMs = 78_000L, // 01:18
                sizeBytes = 22_400_000L, // ~21.3 MB
                resolution = "1080P",
                fps = 60,
                timestamp = System.currentTimeMillis() - 3600_000 * 5,
                isScreenshot = false,
                thumbnailGradientIndex = 1
            ),
            RecordingItem(
                id = UUID.randomUUID().toString(),
                title = "Screenshot_20261102_143022.png",
                filePath = "/storage/emulated/0/Pictures/Screenshots/Screenshot_20261102_143022.png",
                durationMs = 0L,
                sizeBytes = 1_850_000L, // 1.8 MB
                resolution = "1080x2400",
                fps = 0,
                timestamp = System.currentTimeMillis() - 3600_000 * 8,
                isScreenshot = true,
                thumbnailGradientIndex = 2
            ),
            RecordingItem(
                id = UUID.randomUUID().toString(),
                title = "XRecorder_20261101_Reels_Clip.mp4",
                filePath = "/storage/emulated/0/Movies/XRecorder/XRecorder_20261101_Reels_Clip.mp4",
                durationMs = 34_000L, // 00:34
                sizeBytes = 11_200_000L, // ~10.6 MB
                resolution = "720P",
                fps = 30,
                timestamp = System.currentTimeMillis() - 3600_000 * 24,
                isScreenshot = false,
                thumbnailGradientIndex = 3
            )
        )
        _recordings.value = sampleItems
    }

    fun addRecording(
        durationMs: Long,
        isScreenshot: Boolean = false,
        resolutionLabel: String = "1080P",
        fpsVal: Int = 60
    ): RecordingItem {
        val now = System.currentTimeMillis()
        val extension = if (isScreenshot) "png" else "mp4"
        val prefix = if (isScreenshot) "Screenshot" else "XRecorder"
        val fileName = "${prefix}_${now}.$extension"
        
        // Approximate size: ~18MB per minute at 1080p
        val estimatedSize = if (isScreenshot) {
            2_100_000L
        } else {
            val seconds = (durationMs / 1000).coerceAtLeast(1)
            seconds * 320_000L + 500_000L
        }

        val item = RecordingItem(
            id = UUID.randomUUID().toString(),
            title = fileName,
            filePath = "${context.filesDir.absolutePath}/$fileName",
            durationMs = if (isScreenshot) 0L else durationMs,
            sizeBytes = estimatedSize,
            resolution = resolutionLabel,
            fps = if (isScreenshot) 0 else fpsVal,
            timestamp = now,
            isScreenshot = isScreenshot,
            thumbnailGradientIndex = (0..3).random()
        )

        // Try creating actual file placeholder in app private files so media intents work
        try {
            val file = File(context.filesDir, fileName)
            if (!file.exists()) {
                file.writeBytes(ByteArray(1024))
            }
        } catch (_: Exception) {}

        _recordings.value = listOf(item) + _recordings.value
        return item
    }

    fun deleteRecording(id: String) {
        val target = _recordings.value.find { it.id == id }
        if (target != null) {
            try {
                val f = File(target.filePath)
                if (f.exists()) f.delete()
            } catch (_: Exception) {}
        }
        _recordings.value = _recordings.value.filter { it.id != id }
    }

    fun renameRecording(id: String, newTitle: String) {
        val cleanName = if (!newTitle.endsWith(".mp4") && !newTitle.endsWith(".png")) {
            val item = _recordings.value.find { it.id == id }
            val ext = if (item?.isScreenshot == true) ".png" else ".mp4"
            "$newTitle$ext"
        } else newTitle

        _recordings.value = _recordings.value.map {
            if (it.id == id) it.copy(title = cleanName) else it
        }
    }

    fun trimRecording(id: String, startFraction: Float, endFraction: Float): RecordingItem? {
        val original = _recordings.value.find { it.id == id } ?: return null
        val originalDuration = original.durationMs
        val newDuration = ((endFraction - startFraction) * originalDuration).toLong().coerceAtLeast(1000L)
        val newSize = ((endFraction - startFraction) * original.sizeBytes).toLong().coerceAtLeast(100_000L)
        val now = System.currentTimeMillis()
        val trimmedTitle = original.title.substringBeforeLast(".") + "_trimmed_${now % 10000}.mp4"

        val trimmedItem = RecordingItem(
            id = UUID.randomUUID().toString(),
            title = trimmedTitle,
            filePath = "${context.filesDir.absolutePath}/$trimmedTitle",
            durationMs = newDuration,
            sizeBytes = newSize,
            resolution = original.resolution,
            fps = original.fps,
            timestamp = now,
            isScreenshot = false,
            thumbnailGradientIndex = (original.thumbnailGradientIndex + 1) % 4
        )

        _recordings.value = listOf(trimmedItem) + _recordings.value
        return trimmedItem
    }

    fun updateSettings(newSettings: RecordingSettings) {
        _settings.value = newSettings
    }
}
