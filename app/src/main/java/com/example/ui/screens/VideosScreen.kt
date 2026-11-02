package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.model.MediaFilter
import com.example.model.RecordingItem
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VideosScreen(
    recordings: List<RecordingItem>,
    currentFilter: MediaFilter,
    onFilterSelect: (MediaFilter) -> Unit,
    onPlay: (RecordingItem) -> Unit,
    onTrim: (RecordingItem) -> Unit,
    onRename: (RecordingItem) -> Unit,
    onDelete: (RecordingItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val filteredList = remember(recordings, currentFilter) {
        when (currentFilter) {
            MediaFilter.ALL -> recordings
            MediaFilter.VIDEOS -> recordings.filter { !it.isScreenshot }
            MediaFilter.SCREENSHOTS -> recordings.filter { it.isScreenshot }
        }
    }

    val totalVideoCount = recordings.count { !it.isScreenshot }
    val totalScreenshotCount = recordings.count { it.isScreenshot }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(XRecorderDarkBg)
    ) {
        // Top Storage Banner (Iconic XRecorder header)
        StorageEstimateCard(
            totalRecordings = recordings.size,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = currentFilter == MediaFilter.ALL,
                onClick = { onFilterSelect(MediaFilter.ALL) },
                label = { Text("All (${recordings.size})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = XRecorderOrange,
                    selectedLabelColor = Color.White,
                    containerColor = XRecorderSurface,
                    labelColor = XRecorderTextSecondary
                ),
                shape = RoundedCornerShape(20.dp),
                border = null,
                modifier = Modifier.testTag("filter_chip_all")
            )

            FilterChip(
                selected = currentFilter == MediaFilter.VIDEOS,
                onClick = { onFilterSelect(MediaFilter.VIDEOS) },
                label = { Text("Videos ($totalVideoCount)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = XRecorderOrange,
                    selectedLabelColor = Color.White,
                    containerColor = XRecorderSurface,
                    labelColor = XRecorderTextSecondary
                ),
                shape = RoundedCornerShape(20.dp),
                border = null,
                modifier = Modifier.testTag("filter_chip_videos")
            )

            FilterChip(
                selected = currentFilter == MediaFilter.SCREENSHOTS,
                onClick = { onFilterSelect(MediaFilter.SCREENSHOTS) },
                label = { Text("Screenshots ($totalScreenshotCount)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = XRecorderOrange,
                    selectedLabelColor = Color.White,
                    containerColor = XRecorderSurface,
                    labelColor = XRecorderTextSecondary
                ),
                shape = RoundedCornerShape(20.dp),
                border = null,
                modifier = Modifier.testTag("filter_chip_screenshots")
            )
        }

        // List or Empty State
        if (filteredList.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(XRecorderSurface)
                    ) {
                        Icon(
                            imageVector = if (currentFilter == MediaFilter.SCREENSHOTS) Icons.Default.PhotoCamera else Icons.Default.Videocam,
                            contentDescription = null,
                            tint = XRecorderOrangeLight,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (currentFilter == MediaFilter.SCREENSHOTS) "No screenshots yet" else "No recordings yet",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap the glowing orange button below to capture your screen!",
                        color = XRecorderTextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 6.dp),
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList, key = { it.id }) { item ->
                    RecordingCard(
                        item = item,
                        onPlay = { onPlay(item) },
                        onTrim = { onTrim(item) },
                        onRename = { onRename(item) },
                        onDelete = { onDelete(item) },
                        onShare = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = if (item.isScreenshot) "image/*" else "video/*"
                                putExtra(Intent.EXTRA_SUBJECT, item.title)
                                putExtra(Intent.EXTRA_TEXT, "Recorded with XRecorder: ${item.title}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share recording"))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StorageEstimateCard(
    totalRecordings: Int,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = XRecorderSurface),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(XRecorderOrange.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.SdStorage,
                            contentDescription = "Storage",
                            tint = XRecorderOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Storage: 45.2 GB Free",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Approx. 14 hrs recording remaining",
                            color = XRecorderTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Badge(containerColor = XRecorderSurfaceElevated) {
                    Text(
                        text = "$totalRecordings files",
                        color = XRecorderOrangeLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Storage Progress Bar
            LinearProgressIndicator(
                progress = { 0.38f },
                color = XRecorderOrange,
                trackColor = XRecorderSurfaceElevated,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
private fun RecordingCard(
    item: RecordingItem,
    onPlay: () -> Unit,
    onTrim: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()) }
    val formattedDate = remember(item.timestamp) { dateFormat.format(Date(item.timestamp)) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = XRecorderSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() }
            .testTag("recording_item_${item.id}")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail Box with badges
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(width = 100.dp, height = 75.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.verticalGradient(
                            when (item.thumbnailGradientIndex) {
                                0 -> listOf(Color(0xFF2A2E44), Color(0xFF141724))
                                1 -> listOf(Color(0xFF382348), Color(0xFF170E20))
                                2 -> listOf(Color(0xFF1D3B3A), Color(0xFF0C1A19))
                                else -> listOf(Color(0xFF452B1E), Color(0xFF1F110A))
                            }
                        )
                    )
            ) {
                Icon(
                    imageVector = if (item.isScreenshot) Icons.Default.Image else Icons.Default.PlayCircleFilled,
                    contentDescription = "Play",
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(32.dp)
                )

                // Resolution Badge Top Left
                Badge(
                    containerColor = XRecorderOrange,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                ) {
                    Text(item.resolution, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }

                // Duration Badge Bottom Right
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.formattedDuration,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Metadata Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.formattedSize,
                        color = XRecorderTextSecondary,
                        fontSize = 11.sp
                    )
                    if (item.fps > 0) {
                        Text(
                            text = "${item.fps} FPS",
                            color = XRecorderTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formattedDate,
                    color = XRecorderTextSecondary.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }

            // Overflow Actions Menu
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.testTag("item_menu_btn_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More actions",
                        tint = XRecorderTextSecondary
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(XRecorderSurfaceElevated)
                ) {
                    DropdownMenuItem(
                        text = { Text("Play Video", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = XRecorderOrange) },
                        onClick = {
                            menuExpanded = false
                            onPlay()
                        }
                    )
                    if (!item.isScreenshot) {
                        DropdownMenuItem(
                            text = { Text("Trim & Cut", color = Color.White) },
                            leadingIcon = { Icon(Icons.Default.ContentCut, contentDescription = null, tint = XRecorderOrange) },
                            onClick = {
                                menuExpanded = false
                                onTrim()
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Share", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = Color.LightGray) },
                        onClick = {
                            menuExpanded = false
                            onShare()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Rename", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color.LightGray) },
                        onClick = {
                            menuExpanded = false
                            onRename()
                        }
                    )
                    HorizontalDivider(color = XRecorderDivider)
                    DropdownMenuItem(
                        text = { Text("Delete", color = XRecorderAccentRed) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = XRecorderAccentRed) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}
