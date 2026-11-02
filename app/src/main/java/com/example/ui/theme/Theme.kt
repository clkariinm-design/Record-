package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = XRecorderOrange,
    onPrimary = Color.White,
    primaryContainer = XRecorderOrangeBright,
    onPrimaryContainer = Color.White,
    secondary = XRecorderOrangeLight,
    onSecondary = Color.Black,
    tertiary = XRecorderAccentBlue,
    background = XRecorderDarkBg,
    onBackground = XRecorderTextPrimary,
    surface = XRecorderSurface,
    onSurface = XRecorderTextPrimary,
    surfaceVariant = XRecorderSurfaceVariant,
    onSurfaceVariant = XRecorderTextSecondary,
    outline = XRecorderDivider,
    error = XRecorderAccentRed
  )

private val LightColorScheme = DarkColorScheme // XRecorder is iconic for its dark studio aesthetic

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false, // Keep consistent XRecorder brand aesthetic
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
