package com.example.wherewegoing.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ChickColors = lightColorScheme(
    primary = Color(0xFF92511F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE2A3),
    onPrimaryContainer = Color(0xFF382109),
    secondary = Color(0xFFAD5A4A),
    secondaryContainer = Color(0xFFFFE4D8),
    onSecondaryContainer = Color(0xFF4B241B),
    background = Color(0xFFFFFAF0),
    onBackground = Color(0xFF30251C),
    surface = Color(0xFFFFFAF0),
    onSurface = Color(0xFF30251C),
    surfaceVariant = Color(0xFFF5EBDD),
    onSurfaceVariant = Color(0xFF584C40),
    surfaceContainerLow = Color(0xFFFFF5E7),
    surfaceContainer = Color(0xFFF9EFDF),
    surfaceContainerHigh = Color(0xFFF3E6D5),
    outline = Color(0xFF9F8F7D)
)

@Composable
fun WhereWeGoingTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ChickColors, typography = Typography, content = content)
}
