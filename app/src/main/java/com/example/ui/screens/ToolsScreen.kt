package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RecordingItem
import com.example.ui.theme.*

@Composable
fun ToolsScreen(
    recordings: List<RecordingItem>,
    onOpenTrimmer: (RecordingItem) -> Unit,
    onTakeScreenshot: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val firstVideo = recordings.firstOrNull { !it.isScreenshot }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(XRecorderDarkBg)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Hero Banner: Video Editor Suite
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = XRecorderSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                XRecorderOrange.copy(alpha = 0.25f),
                                XRecorderAccentPurple.copy(alpha = 0.15f)
                            )
                        )
                    )
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Badge(containerColor = XRecorderOrange) {
                                Text("PRO TOOLS", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Video Editor & Studio",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cut, extract audio, compress, and edit recordings directly on device without internet.",
                            color = XRecorderTextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp),
                            lineHeight = 16.sp
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(XRecorderOrange)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = "Studio",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Video & Audio Utilities",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Grid of tools
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                ToolCard(
                    title = "Trim Video",
                    desc = "Cut start/end segments",
                    icon = Icons.Default.ContentCut,
                    iconTint = XRecorderOrange,
                    bgGradient = listOf(Color(0xFF38231C), XRecorderSurface),
                    onClick = {
                        if (firstVideo != null) {
                            onOpenTrimmer(firstVideo)
                        } else {
                            onShowMessage("Record a video first to trim!")
                        }
                    },
                    testTag = "tool_trim_video"
                )
            }

            item {
                ToolCard(
                    title = "Video to MP3",
                    desc = "Extract audio track",
                    icon = Icons.Default.Audiotrack,
                    iconTint = XRecorderAccentGreen,
                    bgGradient = listOf(Color(0xFF1B3624), XRecorderSurface),
                    onClick = {
                        onShowMessage("Audio extractor ready: ${firstVideo?.title ?: "Select video"}")
                    },
                    testTag = "tool_video_to_mp3"
                )
            }

            item {
                ToolCard(
                    title = "Compress Video",
                    desc = "Reduce MB file size",
                    icon = Icons.Default.Compress,
                    iconTint = XRecorderAccentBlue,
                    bgGradient = listOf(Color(0xFF192A40), XRecorderSurface),
                    onClick = {
                        onShowMessage("Compressor ready: save up to 60% disk storage!")
                    },
                    testTag = "tool_compress_video"
                )
            }

            item {
                ToolCard(
                    title = "Take Screenshot",
                    desc = "Instant screen grab",
                    icon = Icons.Default.PhotoCamera,
                    iconTint = XRecorderAccentPurple,
                    bgGradient = listOf(Color(0xFF2E1C38), XRecorderSurface),
                    onClick = onTakeScreenshot,
                    testTag = "tool_take_screenshot"
                )
            }

            item {
                ToolCard(
                    title = "Speed Modifier",
                    desc = "0.5x, 1x, 2x, 4x speed",
                    icon = Icons.Default.Speed,
                    iconTint = Color(0xFFFFD600),
                    bgGradient = listOf(Color(0xFF38351A), XRecorderSurface),
                    onClick = {
                        onShowMessage("Video speed modifier ready")
                    },
                    testTag = "tool_speed_modifier"
                )
            }

            item {
                ToolCard(
                    title = "Floating Brush",
                    desc = "Draw & annotate live",
                    icon = Icons.Default.Brush,
                    iconTint = Color(0xFFFF4081),
                    bgGradient = listOf(Color(0xFF3A1C2C), XRecorderSurface),
                    onClick = {
                        onShowMessage("Screen drawing brush tool enabled")
                    },
                    testTag = "tool_brush"
                )
            }
        }
    }
}

@Composable
private fun ToolCard(
    title: String,
    desc: String,
    icon: ImageVector,
    iconTint: Color,
    bgGradient: List<Color>,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = XRecorderSurface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .background(Brush.verticalGradient(bgGradient))
                .padding(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.18f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = desc,
                color = XRecorderTextSecondary,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
