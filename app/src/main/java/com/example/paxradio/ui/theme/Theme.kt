package com.example.paxradio.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkBackground = Color(0xFF1A1A1A)
val CardBackground = Color(0xFF2D2D2D)
val DeepBlue = Color(0xFF0066CC)
val LightGray = Color(0xFFB0B0B0)

private val DarkColorScheme = darkColorScheme(
    primary = DeepBlue,
    onPrimary = Color.White,
    secondary = DeepBlue,
    onSecondary = Color.White,
    background = DarkBackground,
    onBackground = Color.White,
    surface = CardBackground,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF3A3A3A),
    onSurfaceVariant = LightGray
)

@Composable
fun PaxRadioTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}

