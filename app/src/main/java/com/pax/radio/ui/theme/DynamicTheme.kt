package com.pax.radio.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

fun colorFromHex(hex: String): Color {
    return Color(android.graphics.Color.parseColor(hex))
}

fun dynamicColorScheme(
    backgroundColor: String,
    primaryTextColor: String,
    secondaryTextColor: String
) = darkColorScheme(
    primary = colorFromHex(primaryTextColor),
    onPrimary = colorFromHex(backgroundColor),
    secondary = colorFromHex(secondaryTextColor),
    onSecondary = colorFromHex(backgroundColor),
    background = colorFromHex(backgroundColor),
    onBackground = colorFromHex(primaryTextColor),
    surface = colorFromHex(backgroundColor).copy(alpha = 0.8f),
    onSurface = colorFromHex(primaryTextColor),
    surfaceVariant = colorFromHex(backgroundColor).copy(alpha = 0.7f),
    onSurfaceVariant = colorFromHex(secondaryTextColor)
)

