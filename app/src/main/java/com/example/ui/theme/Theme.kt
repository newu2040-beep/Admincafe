package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CafeGold,
    onPrimary = Color(0xFF231402),
    primaryContainer = Color(0xFF422808),
    onPrimaryContainer = CafeGoldLight,
    secondary = CafeTerracotta,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4A1F11),
    onSecondaryContainer = Color(0xFFFFDBCF),
    background = CafeEspresso,
    onBackground = Color(0xFFF3ECE4),
    surface = CafeDarkSurface,
    onSurface = Color(0xFFF3ECE4),
    surfaceVariant = CafeDarkSurfaceElevated,
    onSurfaceVariant = Color(0xFFC7BCB0),
    outline = CafeDarkBorder,
    error = StatusCancelledColor,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = CafeGoldDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFECCF),
    onPrimaryContainer = Color(0xFF2C1600),
    secondary = CafeTerracotta,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDBCF),
    onSecondaryContainer = Color(0xFF380D02),
    background = CafeLightBg,
    onBackground = Color(0xFF1E1A17),
    surface = CafeLightSurface,
    onSurface = Color(0xFF1E1A17),
    surfaceVariant = Color(0xFFF0EBE1),
    onSurfaceVariant = Color(0xFF4E463F),
    outline = CafeLightBorder,
    error = StatusCancelledColor,
    onError = Color.White
)

@Composable
fun AdminCafeTheme(
    darkTheme: Boolean = true, // Default to sleek executive dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    AdminCafeTheme(darkTheme = darkTheme, content = content)
}
