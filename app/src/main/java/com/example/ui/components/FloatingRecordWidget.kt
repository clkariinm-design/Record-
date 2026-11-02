package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RecordingState
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun FloatingRecordWidget(
    recordingState: RecordingState,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onStartRecord: () -> Unit,
    onPauseRecord: () -> Unit,
    onResumeRecord: () -> Unit,
    onStopRecord: () -> Unit,
    onScreenshot: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Draggable position offset
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val isRecording = recordingState is RecordingState.Recording
    val isPaused = (recordingState as? RecordingState.Recording)?.isPaused == true
    val durationMs = (recordingState as? RecordingState.Recording)?.durationMs ?: 0L

    // Pulse animation when recording
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording && !isPaused) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Expanded Radial Actions Menu
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = XRecorderSurfaceElevated),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .border(1.dp, XRecorderOrange.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isRecording) {
                            // Stop Button
                            FloatingActionIcon(
                                icon = Icons.Default.Stop,
                                label = "Stop",
                                iconTint = Color.White,
                                bgBrush = Brush.linearGradient(listOf(XRecorderAccentRed, Color(0xFFD32F2F))),
                                onClick = onStopRecord,
                                testTag = "floating_stop_btn"
                            )

                            // Pause / Resume Button
                            FloatingActionIcon(
                                icon = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                label = if (isPaused) "Resume" else "Pause",
                                iconTint = Color.White,
                                bgBrush = Brush.linearGradient(listOf(XRecorderOrangeBright, XRecorderOrange)),
                                onClick = if (isPaused) onResumeRecord else onPauseRecord,
                                testTag = "floating_pause_btn"
                            )
                        } else {
                            // Start Record Button
                            FloatingActionIcon(
                                icon = Icons.Default.FiberManualRecord,
                                label = "Record",
                                iconTint = Color.White,
                                bgBrush = Brush.linearGradient(listOf(XRecorderAccentRed, XRecorderOrange)),
                                onClick = onStartRecord,
                                testTag = "floating_start_record_btn"
                            )
                        }

                        // Instant Screenshot Button
                        FloatingActionIcon(
                            icon = Icons.Default.PhotoCamera,
                            label = "Screenshot",
                            iconTint = Color.White,
                            bgBrush = Brush.linearGradient(listOf(XRecorderAccentBlue, Color(0xFF1E88E5))),
                            onClick = onScreenshot,
                            testTag = "floating_screenshot_btn"
                        )

                        // Settings Button
                        FloatingActionIcon(
                            icon = Icons.Default.Settings,
                            label = "Settings",
                            iconTint = Color.White,
                            bgBrush = Brush.linearGradient(listOf(Color(0xFF546E7A), Color(0xFF37474F))),
                            onClick = onOpenSettings,
                            testTag = "floating_settings_btn"
                        )
                    }
                }
            }

            // The Iconic Floating Ball
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .scale(pulseScale)
                    .size(62.dp)
                    .shadow(12.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            if (isRecording) listOf(XRecorderAccentRed, XRecorderOrange)
                            else listOf(XRecorderOrangeBright, XRecorderOrange)
                        )
                    )
                    .border(2.5.dp, Color.White.copy(alpha = 0.85f), CircleShape)
                    .clickable { onToggleExpand() }
                    .testTag("floating_record_ball")
            ) {
                if (isRecording) {
                    val sec = (durationMs / 1000) % 60
                    val min = (durationMs / 1000) / 60
                    val text = String.format("%02d:%02d", min, sec)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isPaused) Color.Yellow else Color.White)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = text,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Screen Recorder Control Ball",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingActionIcon(
    icon: ImageVector,
    label: String,
    iconTint: Color,
    bgBrush: Brush,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(bgBrush)
                .testTag(testTag)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
