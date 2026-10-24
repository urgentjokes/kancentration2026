package com.example.kancentration.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1E3A5F),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = GoldAccent,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF37474F),
    onSecondaryContainer = Color(0xFFECEFF1),
    tertiary = VermilionAccent,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = Color(0xFFEDE0D4),
    onSurface = Color(0xFFEDE0D4),
    onSurfaceVariant = Color(0xFFB0BEC5)
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3EDF8),
    onPrimaryContainer = IndigoPrimary,
    secondary = VermilionAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF0ED),
    onSecondaryContainer = Color(0xFF7A1C1C),
    tertiary = GoldAccent,
    background = WashiBackground,
    surface = Color.White,
    surfaceVariant = Color(0xFFECEAE4),
    onBackground = SumiBlack,
    onSurface = SumiBlack,
    onSurfaceVariant = Color(0xFF555555)
)

@Composable
fun KancentrationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
