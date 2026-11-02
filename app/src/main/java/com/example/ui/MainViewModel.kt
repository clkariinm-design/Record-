package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.RecordingRepository
import com.example.model.*
import com.example.service.ScreenRecordService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MainUiState(
    val currentTab: AppTab = AppTab.VIDEOS,
    val mediaFilter: MediaFilter = MediaFilter.ALL,
    val recordings: List<RecordingItem> = emptyList(),
    val settings: RecordingSettings = RecordingSettings(),
    val recordingState: RecordingState = RecordingState.Idle,
    val isFloatingBallVisible: Boolean = true,
    val isFloatingMenuExpanded: Boolean = false,
    val activeVideoPlayerItem: RecordingItem? = null,
    val activeTrimmerItem: RecordingItem? = null,
    val completedRecordingItem: RecordingItem? = null,
    val renameTargetItem: RecordingItem? = null,
    val deleteTargetItem: RecordingItem? = null,
    val toastMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RecordingRepository(application)
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null
    private var recordingTimerJob: Job? = null
    private var currentRecordingDurationMs = 0L

    init {
        // Collect repository recordings
        viewModelScope.launch {
            repository.recordings.collect { items ->
                _uiState.update { it.copy(recordings = items) }
            }
        }

        // Collect repository settings
        viewModelScope.launch {
            repository.settings.collect { s ->
                _uiState.update { it.copy(settings = s) }
            }
        }

        // Collect background service state in sync
        viewModelScope.launch {
            combine(
                ScreenRecordService.isRecordingFlow,
                ScreenRecordService.isPausedFlow,
                ScreenRecordService.elapsedTimeMsFlow
            ) { isRec, isPaused, elapsed ->
                Triple(isRec, isPaused, elapsed)
            }.collect { (isRec, isPaused, elapsed) ->
                if (isRec) {
                    currentRecordingDurationMs = elapsed
                    _uiState.update {
                        it.copy(recordingState = RecordingState.Recording(elapsed, isPaused))
                    }
                } else if (_uiState.value.recordingState is RecordingState.Recording) {
                    // Service stopped from notification
                    onServiceStoppedExternally()
                }
            }
        }
    }

    fun selectTab(tab: AppTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun setMediaFilter(filter: MediaFilter) {
        _uiState.update { it.copy(mediaFilter = filter) }
    }

    fun toggleFloatingMenu() {
        _uiState.update { it.copy(isFloatingMenuExpanded = !it.isFloatingMenuExpanded) }
    }

    fun dismissFloatingMenu() {
        _uiState.update { it.copy(isFloatingMenuExpanded = false) }
    }

    fun toggleFloatingBallVisibility() {
        val next = !_uiState.value.isFloatingBallVisible
        _uiState.update { it.copy(isFloatingBallVisible = next) }
        showToast(if (next) "Floating ball enabled" else "Floating ball hidden")
    }

    fun requestStartRecording() {
        val currentState = _uiState.value.recordingState
        if (currentState !is RecordingState.Idle) return

        val countdownSec = _uiState.value.settings.countdownSeconds
        if (countdownSec <= 0) {
            startActualRecording()
        } else {
            countdownJob?.cancel()
            countdownJob = viewModelScope.launch {
                for (i in countdownSec downTo 1) {
                    _uiState.update { it.copy(recordingState = RecordingState.Countdown(i)) }
                    delay(1000L)
                }
                startActualRecording()
            }
        }
    }

    private fun startActualRecording() {
        _uiState.update {
            it.copy(
                recordingState = RecordingState.Recording(0L, false),
                isFloatingMenuExpanded = false
            )
        }
        currentRecordingDurationMs = 0L
        ScreenRecordService.start(getApplication())

        recordingTimerJob?.cancel()
        recordingTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val state = _uiState.value.recordingState
                if (state is RecordingState.Recording) {
                    if (!state.isPaused) {
                        currentRecordingDurationMs += 1000L
                        _uiState.update {
                            it.copy(recordingState = RecordingState.Recording(currentRecordingDurationMs, false))
                        }
                    }
                } else {
                    break
                }
            }
        }
    }

    fun pauseRecording() {
        val state = _uiState.value.recordingState
        if (state is RecordingState.Recording && !state.isPaused) {
            ScreenRecordService.pause(getApplication())
            _uiState.update {
                it.copy(recordingState = RecordingState.Recording(state.durationMs, true))
            }
        }
    }

    fun resumeRecording() {
        val state = _uiState.value.recordingState
        if (state is RecordingState.Recording && state.isPaused) {
            ScreenRecordService.resume(getApplication())
            _uiState.update {
                it.copy(recordingState = RecordingState.Recording(state.durationMs, false))
            }
        }
    }

    fun stopRecording() {
        recordingTimerJob?.cancel()
        countdownJob?.cancel()
        val state = _uiState.value.recordingState

        if (state is RecordingState.Recording) {
            val finalDuration = currentRecordingDurationMs.coerceAtLeast(1000L)
            ScreenRecordService.stop(getApplication())
            _uiState.update { it.copy(recordingState = RecordingState.Saving) }

            viewModelScope.launch {
                delay(600L) // smooth saving transition
                val resLabel = _uiState.value.settings.resolution.label.substringBefore(" ")
                val fpsVal = _uiState.value.settings.fps.value
                val newItem = repository.addRecording(
                    durationMs = finalDuration,
                    isScreenshot = false,
                    resolutionLabel = resLabel,
                    fpsVal = fpsVal
                )
                _uiState.update {
                    it.copy(
                        recordingState = RecordingState.Idle,
                        completedRecordingItem = newItem,
                        isFloatingMenuExpanded = false
                    )
                }
            }
        } else if (state is RecordingState.Countdown) {
            _uiState.update { it.copy(recordingState = RecordingState.Idle) }
        }
    }

    private fun onServiceStoppedExternally() {
        val finalDuration = currentRecordingDurationMs.coerceAtLeast(1000L)
        val resLabel = _uiState.value.settings.resolution.label.substringBefore(" ")
        val fpsVal = _uiState.value.settings.fps.value
        val newItem = repository.addRecording(
            durationMs = finalDuration,
            isScreenshot = false,
            resolutionLabel = resLabel,
            fpsVal = fpsVal
        )
        _uiState.update {
            it.copy(
                recordingState = RecordingState.Idle,
                completedRecordingItem = newItem
            )
        }
    }

    fun takeInstantScreenshot() {
        val res = "${_uiState.value.settings.resolution.width}x${_uiState.value.settings.resolution.height}"
        val item = repository.addRecording(
            durationMs = 0L,
            isScreenshot = true,
            resolutionLabel = res,
            fpsVal = 0
        )
        _uiState.update {
            it.copy(
                completedRecordingItem = item,
                isFloatingMenuExpanded = false
            )
        }
        showToast("Screenshot captured!")
    }

    fun openPlayer(item: RecordingItem) {
        _uiState.update { it.copy(activeVideoPlayerItem = item) }
    }

    fun closePlayer() {
        _uiState.update { it.copy(activeVideoPlayerItem = null) }
    }

    fun openTrimmer(item: RecordingItem) {
        if (!item.isScreenshot) {
            _uiState.update { it.copy(activeTrimmerItem = item) }
        }
    }

    fun closeTrimmer() {
        _uiState.update { it.copy(activeTrimmerItem = null) }
    }

    fun saveTrim(startFraction: Float, endFraction: Float) {
        val target = _uiState.value.activeTrimmerItem ?: return
        val trimmed = repository.trimRecording(target.id, startFraction, endFraction)
        _uiState.update {
            it.copy(
                activeTrimmerItem = null,
                completedRecordingItem = trimmed
            )
        }
        showToast("Video trimmed & saved successfully!")
    }

    fun dismissCompletedDialog() {
        _uiState.update { it.copy(completedRecordingItem = null) }
    }

    fun promptRename(item: RecordingItem) {
        _uiState.update { it.copy(renameTargetItem = item) }
    }

    fun confirmRename(newName: String) {
        val target = _uiState.value.renameTargetItem ?: return
        if (newName.isNotBlank()) {
            repository.renameRecording(target.id, newName.trim())
            showToast("File renamed!")
        }
        _uiState.update { it.copy(renameTargetItem = null) }
    }

    fun dismissRename() {
        _uiState.update { it.copy(renameTargetItem = null) }
    }

    fun promptDelete(item: RecordingItem) {
        _uiState.update { it.copy(deleteTargetItem = item) }
    }

    fun confirmDelete() {
        val target = _uiState.value.deleteTargetItem ?: return
        repository.deleteRecording(target.id)
        _uiState.update { it.copy(deleteTargetItem = null) }
        showToast("Recording deleted")
    }

    fun dismissDelete() {
        _uiState.update { it.copy(deleteTargetItem = null) }
    }

    fun updateSettings(newSettings: RecordingSettings) {
        repository.updateSettings(newSettings)
        showToast("Settings updated")
    }

    fun showToast(msg: String) {
        _uiState.update { it.copy(toastMessage = msg) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
