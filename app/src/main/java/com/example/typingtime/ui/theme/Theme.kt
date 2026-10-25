package com.example.typingtime.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = NavySurfaceVariant,
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = AccentCyan,
    onSecondary = Color(0xFF00363F),
    secondaryContainer = Color(0xFF0E7490),
    onSecondaryContainer = Color(0xFFCFFAFE),
    tertiary = AccentEmerald,
    background = NavyDark,
    onBackground = DarkTextPrimary,
    surface = NavySurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = NavySurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = NavyBorder
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlueVariant,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = Color(0xFF059669),
    background = SlateLightBg,
    onBackground = SlateLightTextPrimary,
    surface = SlateLightSurface,
    onSurface = SlateLightTextPrimary,
    surfaceVariant = SlateLightSurfaceVariant,
    onSurfaceVariant = SlateLightTextSecondary,
    outline = SlateLightBorder
)

@Composable
fun TypingTimeTheme(
    themePreference: String = "System",
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themePreference) {
        "Light" -> false
        "Dark" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
