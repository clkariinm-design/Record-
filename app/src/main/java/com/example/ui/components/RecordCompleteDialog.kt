package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.RecordingItem
import com.example.ui.theme.*

@Composable
fun RecordCompleteDialog(
    item: RecordingItem,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onTrim: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = XRecorderSurfaceElevated),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("record_complete_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success Badge & Title
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(XRecorderAccentGreen.copy(alpha = 0.18f))
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = XRecorderAccentGreen,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (item.isScreenshot) "Screenshot Saved!" else "Recording Saved!",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Saved to Gallery • Movies/XRecorder",
                    color = XRecorderTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                )

                // Video Thumbnail Card Preview
                Card(
                    colors = CardDefaults.cardColors(containerColor = XRecorderSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPlay() }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF22293E), Color(0xFF141724))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (item.isScreenshot) Icons.Default.Image else Icons.Default.PlayCircleFilled,
                            contentDescription = "Play preview",
                            tint = XRecorderOrange,
                            modifier = Modifier.size(48.dp)
                        )

                        // Duration Badge Bottom Right
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp)
                                .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.formattedDuration,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Resolution Badge Top Left
                        Badge(
                            containerColor = XRecorderOrange,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(10.dp)
                        ) {
                            Text(item.resolution, color = Color.White, fontSize = 10.sp)
                        }
                    }

                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(item.formattedSize, color = XRecorderTextSecondary, fontSize = 11.sp)
                            if (item.fps > 0) {
                                Text("${item.fps} FPS", color = XRecorderTextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons Row: Play, Trim, Share, Delete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DialogActionButton(
                        icon = Icons.Default.PlayArrow,
                        label = "Play",
                        tint = Color.White,
                        bgColor = XRecorderOrange,
                        onClick = onPlay,
                        testTag = "dialog_play_btn"
                    )

                    if (!item.isScreenshot) {
                        DialogActionButton(
                            icon = Icons.Default.ContentCut,
                            label = "Trim",
                            tint = Color.White,
                            bgColor = XRecorderSurface,
                            onClick = onTrim,
                            testTag = "dialog_trim_btn"
                        )
                    }

                    DialogActionButton(
                        icon = Icons.Default.Share,
                        label = "Share",
                        tint = Color.White,
                        bgColor = XRecorderSurface,
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = if (item.isScreenshot) "image/*" else "video/*"
                                putExtra(Intent.EXTRA_SUBJECT, item.title)
                                putExtra(Intent.EXTRA_TEXT, "Recorded with XRecorder: ${item.title}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share recording"))
                        },
                        testTag = "dialog_share_btn"
                    )

                    DialogActionButton(
                        icon = Icons.Default.Delete,
                        label = "Delete",
                        tint = XRecorderAccentRed,
                        bgColor = XRecorderSurface,
                        onClick = onDelete,
                        testTag = "dialog_delete_btn"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_dismiss_btn")
                ) {
                    Text("Close", color = XRecorderTextSecondary, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun DialogActionButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    bgColor: Color,
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
                .size(46.dp)
                .clip(CircleShape)
                .background(bgColor)
                .testTag(testTag)
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        }
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
