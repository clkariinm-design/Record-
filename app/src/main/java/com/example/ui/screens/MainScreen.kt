package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppTab
import com.example.model.RecordingState
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.toastMessage) {
        val msg = uiState.toastMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    val isRecording = uiState.recordingState is RecordingState.Recording
    val isPaused = (uiState.recordingState as? RecordingState.Recording)?.isPaused == true
    val durationMs = (uiState.recordingState as? RecordingState.Recording)?.durationMs ?: 0L

    // Pulse animation for recording FAB
    val infiniteTransition = rememberInfiniteTransition(label = "fab_pulse")
    val fabScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording && !isPaused) 1.12f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fab_scale"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = XRecorderDarkBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(XRecorderOrange)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "XRecorder",
                            color = Color.White,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(
                            containerColor = Color(0xFFFFB300),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                "PRO",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                },
                actions = {
                    // Quick screenshot action button in top bar
                    IconButton(
                        onClick = { viewModel.takeInstantScreenshot() },
                        modifier = Modifier.testTag("topbar_screenshot_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Capture Screenshot",
                            tint = Color.White
                        )
                    }

                    // Floating ball toggle icon
                    IconButton(
                        onClick = { viewModel.toggleFloatingBallVisibility() },
                        modifier = Modifier.testTag("topbar_floating_ball_btn")
                    ) {
                        Icon(
                            imageVector = if (uiState.isFloatingBallVisible) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Toggle Floating Ball",
                            tint = if (uiState.isFloatingBallVisible) XRecorderOrange else XRecorderTextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = XRecorderDarkBg,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = XRecorderSurface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = uiState.currentTab == AppTab.VIDEOS,
                    onClick = { viewModel.selectTab(AppTab.VIDEOS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = "Videos"
                        )
                    },
                    label = { Text("Videos", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = XRecorderOrange,
                        indicatorColor = XRecorderOrange,
                        unselectedIconColor = XRecorderTextSecondary,
                        unselectedTextColor = XRecorderTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_videos")
                )

                NavigationBarItem(
                    selected = uiState.currentTab == AppTab.TOOLS,
                    onClick = { viewModel.selectTab(AppTab.TOOLS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = "Tools"
                        )
                    },
                    label = { Text("Tools", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = XRecorderOrange,
                        indicatorColor = XRecorderOrange,
                        unselectedIconColor = XRecorderTextSecondary,
                        unselectedTextColor = XRecorderTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_tools")
                )

                NavigationBarItem(
                    selected = uiState.currentTab == AppTab.SETTINGS,
                    onClick = { viewModel.selectTab(AppTab.SETTINGS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = XRecorderOrange,
                        indicatorColor = XRecorderOrange,
                        unselectedIconColor = XRecorderTextSecondary,
                        unselectedTextColor = XRecorderTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        },
        floatingActionButton = {
            // Iconic Record Button with Live Status
            FloatingActionButton(
                onClick = {
                    if (isRecording) {
                        viewModel.stopRecording()
                    } else {
                        viewModel.requestStartRecording()
                    }
                },
                shape = CircleShape,
                containerColor = Color.Transparent,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                modifier = Modifier
                    .scale(fabScale)
                    .size(if (isRecording) 68.dp else 64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            if (isRecording) listOf(XRecorderAccentRed, Color(0xFFC62828))
                            else listOf(XRecorderOrangeBright, XRecorderOrange)
                        )
                    )
                    .border(2.dp, Color.White.copy(alpha = 0.85f), CircleShape)
                    .testTag("main_record_fab")
            ) {
                if (isRecording) {
                    val sec = (durationMs / 1000) % 60
                    val min = (durationMs / 1000) / 60
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop Recording",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = String.format("%02d:%02d", min, sec),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.FiberManualRecord,
                        contentDescription = "Start Recording",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Tabs Switching
            when (uiState.currentTab) {
                AppTab.VIDEOS -> {
                    VideosScreen(
                        recordings = uiState.recordings,
                        currentFilter = uiState.mediaFilter,
                        onFilterSelect = { viewModel.setMediaFilter(it) },
                        onPlay = { viewModel.openPlayer(it) },
                        onTrim = { viewModel.openTrimmer(it) },
                        onRename = { viewModel.promptRename(it) },
                        onDelete = { viewModel.promptDelete(it) }
                    )
                }
                AppTab.TOOLS -> {
                    ToolsScreen(
                        recordings = uiState.recordings,
                        onOpenTrimmer = { viewModel.openTrimmer(it) },
                        onTakeScreenshot = { viewModel.takeInstantScreenshot() },
                        onShowMessage = { viewModel.showToast(it) }
                    )
                }
                AppTab.SETTINGS -> {
                    SettingsScreen(
                        settings = uiState.settings,
                        onUpdateSettings = { viewModel.updateSettings(it) }
                    )
                }
            }

            // Draggable Floating Ball Widget (Signature XRecorder feature)
            if (uiState.isFloatingBallVisible && uiState.settings.showFloatingBall) {
                FloatingRecordWidget(
                    recordingState = uiState.recordingState,
                    isExpanded = uiState.isFloatingMenuExpanded,
                    onToggleExpand = { viewModel.toggleFloatingMenu() },
                    onStartRecord = { viewModel.requestStartRecording() },
                    onPauseRecord = { viewModel.pauseRecording() },
                    onResumeRecord = { viewModel.resumeRecording() },
                    onStopRecord = { viewModel.stopRecording() },
                    onScreenshot = { viewModel.takeInstantScreenshot() },
                    onOpenSettings = {
                        viewModel.dismissFloatingMenu()
                        viewModel.selectTab(AppTab.SETTINGS)
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                )
            }

            // Fullscreen Countdown Overlay (3, 2, 1...)
            if (uiState.recordingState is RecordingState.Countdown) {
                val sec = (uiState.recordingState as RecordingState.Countdown).secondsRemaining
                CountdownOverlay(
                    secondsRemaining = sec,
                    onCancel = { viewModel.stopRecording() }
                )
            }

            // Saving indicator overlay
            if (uiState.recordingState is RecordingState.Saving) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = XRecorderSurfaceElevated),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = XRecorderOrange)
                            Spacer(modifier = Modifier.height(14.dp))
                            Text("Saving recording...", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Video Player Sheet Dialog
    uiState.activeVideoPlayerItem?.let { item ->
        VideoPlayerSheet(
            item = item,
            onClose = { viewModel.closePlayer() },
            onTrim = {
                viewModel.closePlayer()
                viewModel.openTrimmer(item)
            },
            onDelete = {
                viewModel.closePlayer()
                viewModel.promptDelete(item)
            }
        )
    }

    // Video Trimmer Sheet Dialog
    uiState.activeTrimmerItem?.let { item ->
        VideoTrimmerSheet(
            item = item,
            onClose = { viewModel.closeTrimmer() },
            onSaveTrim = { start, end ->
                viewModel.saveTrim(start, end)
            }
        )
    }

    // Post-Recording Success Dialog
    uiState.completedRecordingItem?.let { item ->
        RecordCompleteDialog(
            item = item,
            onDismiss = { viewModel.dismissCompletedDialog() },
            onPlay = {
                viewModel.dismissCompletedDialog()
                viewModel.openPlayer(item)
            },
            onTrim = {
                viewModel.dismissCompletedDialog()
                viewModel.openTrimmer(item)
            },
            onDelete = {
                viewModel.dismissCompletedDialog()
                viewModel.promptDelete(item)
            }
        )
    }

    // Rename Dialog
    uiState.renameTargetItem?.let { target ->
        var newTitle by remember { mutableStateOf(target.title.substringBeforeLast(".")) }
        AlertDialog(
            onDismissRequest = { viewModel.dismissRename() },
            title = { Text("Rename File", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("File Name", color = XRecorderTextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = XRecorderOrange,
                        unfocusedBorderColor = XRecorderDivider
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("rename_input_field")
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmRename(newTitle) },
                    colors = ButtonDefaults.buttonColors(containerColor = XRecorderOrange)
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissRename() }) {
                    Text("Cancel", color = XRecorderTextSecondary)
                }
            },
            containerColor = XRecorderSurfaceElevated
        )
    }

    // Delete Confirmation Dialog
    uiState.deleteTargetItem?.let { target ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDelete() },
            title = { Text("Delete Recording?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to permanently delete \"${target.title}\"? This cannot be undone.",
                    color = XRecorderTextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDelete() },
                    colors = ButtonDefaults.buttonColors(containerColor = XRecorderAccentRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDelete() }) {
                    Text("Cancel", color = XRecorderTextSecondary)
                }
            },
            containerColor = XRecorderSurfaceElevated
        )
    }
}
