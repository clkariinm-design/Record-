package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.RecordingItem
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoTrimmerSheet(
    item: RecordingItem,
    onClose: () -> Unit,
    onSaveTrim: (startFraction: Float, endFraction: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var startFraction by remember { mutableFloatStateOf(0.15f) }
    var endFraction by remember { mutableFloatStateOf(0.85f) }

    val totalDurationMs = item.durationMs.coerceAtLeast(1000L)
    val trimmedDurationMs = ((endFraction - startFraction) * totalDurationMs).toLong().coerceAtLeast(500L)

    val startMs = (startFraction * totalDurationMs).toLong()
    val endMs = (endFraction * totalDurationMs).toLong()

    fun formatMs(ms: Long): String {
        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return String.format("%02d:%02d", min, sec)
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(XRecorderDarkBg)
                .testTag("video_trimmer_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(36.dp)
                            .background(XRecorderSurfaceElevated, CircleShape)
                            .testTag("trimmer_cancel_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }

                    Text(
                        text = "Video Trimmer",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = { onSaveTrim(startFraction, endFraction) },
                        colors = ButtonDefaults.buttonColors(containerColor = XRecorderOrange),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("trimmer_save_btn")
                    ) {
                        Icon(Icons.Default.ContentCut, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // Video Preview Area
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(XRecorderSurface, Color(0xFF0F1015))
                            )
                        )
                        .border(1.dp, XRecorderDivider, RoundedCornerShape(16.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Preview Cut",
                        tint = XRecorderOrange.copy(alpha = 0.8f),
                        modifier = Modifier.size(64.dp)
                    )

                    // Overlay cut info badge
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Cut: ${formatMs(startMs)} ➔ ${formatMs(endMs)}",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Badge(containerColor = XRecorderOrange) {
                                Text(formatMs(trimmedDurationMs), color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Trimmer Controls Section
                Card(
                    colors = CardDefaults.cardColors(containerColor = XRecorderSurface),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Sliders for Start & End Trimming
                        Text(
                            "Drag Handles to Select Clip Range",
                            color = XRecorderTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        // Start Cut Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Start Marker", color = Color.White, fontSize = 12.sp)
                                Text(formatMs(startMs), color = XRecorderOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = startFraction,
                                onValueChange = {
                                    startFraction = it.coerceAtMost(endFraction - 0.05f)
                                },
                                valueRange = 0f..1f,
                                colors = SliderDefaults.colors(
                                    thumbColor = XRecorderOrange,
                                    activeTrackColor = XRecorderOrange,
                                    inactiveTrackColor = XRecorderSurfaceElevated
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("trimmer_start_slider")
                            )
                        }

                        // End Cut Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("End Marker", color = Color.White, fontSize = 12.sp)
                                Text(formatMs(endMs), color = XRecorderOrangeBright, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = endFraction,
                                onValueChange = {
                                    endFraction = it.coerceAtLeast(startFraction + 0.05f)
                                },
                                valueRange = 0f..1f,
                                colors = SliderDefaults.colors(
                                    thumbColor = XRecorderOrangeBright,
                                    activeTrackColor = XRecorderOrangeBright,
                                    inactiveTrackColor = XRecorderSurfaceElevated
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("trimmer_end_slider")
                            )
                        }

                        // Summary Info Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(XRecorderSurfaceElevated, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Original Length", color = XRecorderTextSecondary, fontSize = 11.sp)
                                Text(formatMs(totalDurationMs), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("New Output Length", color = XRecorderOrange, fontSize = 11.sp)
                                Text(formatMs(trimmedDurationMs), color = XRecorderOrange, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }
        }
    }
}
