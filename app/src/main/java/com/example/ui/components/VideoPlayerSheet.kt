package com.example.ui.components

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.RecordingItem
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun VideoPlayerSheet(
    item: RecordingItem,
    onClose: () -> Unit,
    onTrim: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var currentProgressFraction by remember { mutableFloatStateOf(0f) }

    val durationMs = if (item.isScreenshot) 5000L else item.durationMs.coerceAtLeast(1000L)

    // Simulate playback progress
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(100L)
            currentProgressFraction += 100f / durationMs
            if (currentProgressFraction >= 1f) {
                currentProgressFraction = 0f
            }
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag("video_player_dialog")
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                            .testTag("close_player_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close player",
                            tint = Color.White
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Badge(containerColor = XRecorderOrange) {
                                Text(item.resolution, color = Color.White, fontSize = 10.sp)
                            }
                            if (item.fps > 0) {
                                Badge(containerColor = XRecorderSurfaceElevated) {
                                    Text("${item.fps} FPS", color = Color.LightGray, fontSize = 10.sp)
                                }
                            }
                            Text(
                                item.formattedSize,
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = if (item.isScreenshot) "image/*" else "video/*"
                                putExtra(Intent.EXTRA_SUBJECT, item.title)
                                putExtra(Intent.EXTRA_TEXT, "Recorded with XRecorder: ${item.title}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share recording"))
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                            .testTag("share_player_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White
                        )
                    }
                }

                // Video Viewport (Center)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                when (item.thumbnailGradientIndex) {
                                    0 -> listOf(Color(0xFF1E2235), Color(0xFF0D0F18))
                                    1 -> listOf(Color(0xFF261D3A), Color(0xFF0F0B18))
                                    2 -> listOf(Color(0xFF1B2F2E), Color(0xFF0B1413))
                                    else -> listOf(Color(0xFF35241C), Color(0xFF160D09))
                                }
                            )
                        )
                        .clickable { isPlaying = !isPlaying }
                ) {
                    // Center animated pulse play indicator
                    if (!isPlaying) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(XRecorderOrange.copy(alpha = 0.9f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    // Watermark / Indicator
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(14.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isPlaying) XRecorderAccentGreen else Color.Yellow)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (item.isScreenshot) "Screenshot View" else if (isPlaying) "Playing" else "Paused",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Bottom Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    if (!item.isScreenshot) {
                        // Slider and Timecodes
                        val currentMs = (currentProgressFraction * durationMs).toLong()
                        val curSec = currentMs / 1000
                        val totSec = durationMs / 1000
                        val timeStr = String.format("%02d:%02d / %02d:%02d", curSec / 60, curSec % 60, totSec / 60, totSec % 60)

                        Slider(
                            value = currentProgressFraction,
                            onValueChange = {
                                currentProgressFraction = it
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = XRecorderOrange,
                                activeTrackColor = XRecorderOrange,
                                inactiveTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("player_timeline_slider")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(timeStr, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            Text("${item.resolution} • ${item.fps}fps", color = Color.LightGray, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Actions Bar (Play, Trim, Delete)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!item.isScreenshot) {
                            OutlinedButton(
                                onClick = onTrim,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(XRecorderOrange, XRecorderOrangeBright))),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.testTag("player_trim_btn")
                            ) {
                                Icon(Icons.Default.ContentCut, contentDescription = "Trim", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Trim Video", fontSize = 13.sp)
                            }

                            // Big Play/Pause Toggle
                            IconButton(
                                onClick = { isPlaying = !isPlaying },
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(XRecorderOrange)
                                    .testTag("player_play_pause_btn")
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = XRecorderAccentRed),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("player_delete_btn")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Delete", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
