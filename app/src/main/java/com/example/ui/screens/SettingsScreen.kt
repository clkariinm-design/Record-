package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioSource
import com.example.model.Fps
import com.example.model.RecordingSettings
import com.example.model.Resolution
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    settings: RecordingSettings,
    onUpdateSettings: (RecordingSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var showResolutionDialog by remember { mutableStateOf(false) }
    var showFpsDialog by remember { mutableStateOf(false) }
    var showAudioDialog by remember { mutableStateOf(false) }
    var showCountdownDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(XRecorderDarkBg),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Video Settings
        item {
            SettingsCategoryHeader("Video Settings")
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = XRecorderSurface)
            ) {
                Column {
                    SettingsItem(
                        icon = Icons.Default.HighQuality,
                        title = "Resolution",
                        subtitle = settings.resolution.label,
                        onClick = { showResolutionDialog = true },
                        testTag = "setting_resolution"
                    )
                    HorizontalDivider(color = XRecorderDivider)
                    SettingsItem(
                        icon = Icons.Default.Speed,
                        title = "FPS (Frame Rate)",
                        subtitle = settings.fps.label,
                        onClick = { showFpsDialog = true },
                        testTag = "setting_fps"
                    )
                    HorizontalDivider(color = XRecorderDivider)
                    SettingsItem(
                        icon = Icons.Default.Tune,
                        title = "Quality (Bitrate)",
                        subtitle = settings.bitrate,
                        onClick = {
                            val next = if (settings.bitrate.startsWith("Auto")) "16 Mbps (High)" else "Auto (Recommended)"
                            onUpdateSettings(settings.copy(bitrate = next))
                        },
                        testTag = "setting_bitrate"
                    )
                    HorizontalDivider(color = XRecorderDivider)
                    SettingsItem(
                        icon = Icons.Default.ScreenRotation,
                        title = "Orientation",
                        subtitle = settings.orientation,
                        onClick = {
                            val next = when (settings.orientation) {
                                "Auto" -> "Portrait"
                                "Portrait" -> "Landscape"
                                else -> "Auto"
                            }
                            onUpdateSettings(settings.copy(orientation = next))
                        },
                        testTag = "setting_orientation"
                    )
                }
            }
        }

        // Section: Audio Settings
        item {
            SettingsCategoryHeader("Audio Settings")
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = XRecorderSurface)
            ) {
                Column {
                    SettingsItem(
                        icon = Icons.Default.Mic,
                        title = "Audio Source",
                        subtitle = "${settings.audioSource.label} (${settings.audioSource.description})",
                        onClick = { showAudioDialog = true },
                        testTag = "setting_audio_source"
                    )
                }
            }
        }

        // Section: Control Settings
        item {
            SettingsCategoryHeader("Control & Floating Ball")
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = XRecorderSurface)
            ) {
                Column {
                    SettingsItem(
                        icon = Icons.Default.Timer,
                        title = "Countdown before start",
                        subtitle = if (settings.countdownSeconds == 0) "Off" else "${settings.countdownSeconds}s",
                        onClick = { showCountdownDialog = true },
                        testTag = "setting_countdown"
                    )
                    HorizontalDivider(color = XRecorderDivider)
                    SettingsToggleItem(
                        icon = Icons.Default.RadioButtonChecked,
                        title = "Show Floating Ball",
                        subtitle = "Fast controls for screen recording",
                        checked = settings.showFloatingBall,
                        onCheckedChange = { onUpdateSettings(settings.copy(showFloatingBall = it)) },
                        testTag = "toggle_floating_ball"
                    )
                    HorizontalDivider(color = XRecorderDivider)
                    SettingsToggleItem(
                        icon = Icons.Default.Vibration,
                        title = "Shake phone to stop",
                        subtitle = "Gently shake device to finish capture",
                        checked = settings.shakeToStop,
                        onCheckedChange = { onUpdateSettings(settings.copy(shakeToStop = it)) },
                        testTag = "toggle_shake_to_stop"
                    )
                    HorizontalDivider(color = XRecorderDivider)
                    SettingsToggleItem(
                        icon = Icons.Default.PhoneAndroid,
                        title = "Keep recording when screen is off",
                        subtitle = "Do not interrupt recording if screen locks",
                        checked = settings.keepRecordingWhenScreenOff,
                        onCheckedChange = { onUpdateSettings(settings.copy(keepRecordingWhenScreenOff = it)) },
                        testTag = "toggle_screen_off_recording"
                    )
                }
            }
        }

        // Section: Storage & About
        item {
            SettingsCategoryHeader("Storage & About")
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = XRecorderSurface)
            ) {
                Column {
                    SettingsItem(
                        icon = Icons.Default.Folder,
                        title = "Save Location",
                        subtitle = "/Movies/XRecorder",
                        onClick = {},
                        showArrow = false,
                        testTag = "setting_save_location"
                    )
                    HorizontalDivider(color = XRecorderDivider)
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "XRecorder Version",
                        subtitle = "v2.4.0 Pro Edition (Native Kotlin & Compose)",
                        onClick = {},
                        showArrow = false,
                        testTag = "setting_version"
                    )
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Resolution Picker Dialog
    if (showResolutionDialog) {
        AlertDialog(
            onDismissRequest = { showResolutionDialog = false },
            title = { Text("Select Video Resolution", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Resolution.values().forEach { res ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onUpdateSettings(settings.copy(resolution = res))
                                    showResolutionDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.resolution == res,
                                onClick = {
                                    onUpdateSettings(settings.copy(resolution = res))
                                    showResolutionDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = XRecorderOrange)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(res.label, color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showResolutionDialog = false }) {
                    Text("Cancel", color = XRecorderOrange)
                }
            },
            containerColor = XRecorderSurfaceElevated
        )
    }

    // FPS Picker Dialog
    if (showFpsDialog) {
        AlertDialog(
            onDismissRequest = { showFpsDialog = false },
            title = { Text("Select Frame Rate (FPS)", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Fps.values().forEach { fps ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onUpdateSettings(settings.copy(fps = fps))
                                    showFpsDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.fps == fps,
                                onClick = {
                                    onUpdateSettings(settings.copy(fps = fps))
                                    showFpsDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = XRecorderOrange)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(fps.label, color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showFpsDialog = false }) {
                    Text("Cancel", color = XRecorderOrange)
                }
            },
            containerColor = XRecorderSurfaceElevated
        )
    }

    // Audio Source Picker Dialog
    if (showAudioDialog) {
        AlertDialog(
            onDismissRequest = { showAudioDialog = false },
            title = { Text("Select Audio Source", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AudioSource.values().forEach { src ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onUpdateSettings(settings.copy(audioSource = src))
                                    showAudioDialog = false
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.audioSource == src,
                                onClick = {
                                    onUpdateSettings(settings.copy(audioSource = src))
                                    showAudioDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = XRecorderOrange)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(src.label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(src.description, color = XRecorderTextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAudioDialog = false }) {
                    Text("Cancel", color = XRecorderOrange)
                }
            },
            containerColor = XRecorderSurfaceElevated
        )
    }

    // Countdown Dialog
    if (showCountdownDialog) {
        AlertDialog(
            onDismissRequest = { showCountdownDialog = false },
            title = { Text("Countdown Before Start", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0 to "Off (Instant start)", 3 to "3 seconds (Recommended)", 5 to "5 seconds", 10 to "10 seconds").forEach { (sec, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onUpdateSettings(settings.copy(countdownSeconds = sec))
                                    showCountdownDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.countdownSeconds == sec,
                                onClick = {
                                    onUpdateSettings(settings.copy(countdownSeconds = sec))
                                    showCountdownDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = XRecorderOrange)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCountdownDialog = false }) {
                    Text("Cancel", color = XRecorderOrange)
                }
            },
            containerColor = XRecorderSurfaceElevated
        )
    }
}

@Composable
private fun SettingsCategoryHeader(text: String) {
    Text(
        text = text,
        color = XRecorderOrangeLight,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showArrow: Boolean = true,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(XRecorderSurfaceElevated)
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = XRecorderOrange, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = XRecorderTextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
        }

        if (showArrow) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = XRecorderTextSecondary.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(XRecorderSurfaceElevated)
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = XRecorderOrange, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = XRecorderTextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = XRecorderOrange,
                uncheckedTrackColor = XRecorderSurfaceElevated
            )
        )
    }
}
