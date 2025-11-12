package com.example.paxradio.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.paxradio.data.AppTheme

// Neon Theme Colors
val DeepNavy = Color(0xFF000020)
val NeonBlue = Color(0xFF00FFFF)
val NeonPurple = Color(0xFFBF00FF)

// Bordeaux Theme Colors
val BordeauxRed = Color(0xFF6D0000)
val Amber = Color(0xFFFFBF00)
val FlameOrange = Color(0xFFD35400)

// Common Colors
val CardBackground = Color(0xFF2D2D2D)
val LightGray = Color(0xFFB0B0B0)

private val NeonColorScheme = darkColorScheme(
    primary = NeonBlue,
    onPrimary = Color.Black,
    secondary = NeonPurple,
    onSecondary = Color.Black,
    background = DeepNavy,
    onBackground = Color.White,
    surface = CardBackground,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF3A3A3A),
    onSurfaceVariant = LightGray
)

private val BordeauxColorScheme = darkColorScheme(
    primary = Amber,
    onPrimary = Color.Black,
    secondary = FlameOrange,
    onSecondary = Color.Black,
    background = BordeauxRed,
    onBackground = Color.White,
    surface = Color(0xFF3A2E2E), // Darker red for cards
    onSurface = Color.White,
    surfaceVariant = Color(0xFF4F3C3C),
    onSurfaceVariant = LightGray
)

@Composable
fun PaxRadioTheme(
    appTheme: AppTheme = AppTheme.NEON,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.NEON -> NeonColorScheme
        AppTheme.BORDEAUX -> BordeauxColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
