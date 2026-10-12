package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val IdeDarkColorScheme = darkColorScheme(
  primary = IdeCyan,
  onPrimary = Color(0xFF003548),
  primaryContainer = Color(0xFF004D67),
  onPrimaryContainer = Color(0xFFBEE9FF),
  secondary = IdePurple,
  onSecondary = Color(0xFF381E72),
  background = IdeBackground,
  onBackground = IdeTextPrimary,
  surface = IdeSurface,
  onSurface = IdeTextPrimary,
  surfaceVariant = IdeSurfaceVariant,
  onSurfaceVariant = IdeTextSecondary,
  error = IdeRed,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to true for dark IDE experience
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = IdeDarkColorScheme,
    typography = Typography,
    content = content
  )
}
