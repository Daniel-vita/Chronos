package com.example.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Sleek Interface Light Theme (Primary design choice)
val SleekColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    secondary = Color(0xFF625B71),
    tertiary = Color(0xFF7D5260),
    background = Color(0xFFFEF7FF),
    surface = Color(0xFFFEF7FF),
    surfaceVariant = Color(0xFFF3EDF7),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1D1B20),
    onSurface = Color(0xFF1D1B20),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFFCAC4D0)
)

// Premium Slate Dark
val SlateColorScheme = darkColorScheme(
    primary = Color(0xFFE2E8F0),
    secondary = Color(0xFF94A3B8),
    tertiary = Color(0xFF64748B),
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFF334155),
    onPrimary = Color(0xFF0F172A),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC)
)

// Premium Teal Dark
val TealColorScheme = darkColorScheme(
    primary = Color(0xFF2DD4BF),
    secondary = Color(0xFF14B8A6),
    tertiary = Color(0xFF0D9488),
    background = Color(0xFF041E1A),
    surface = Color(0xFF0F3E36),
    surfaceVariant = Color(0xFF134E4A),
    onPrimary = Color(0xFF041E1A),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFFF0FDFA),
    onSurface = Color(0xFFF0FDFA)
)

// Premium Ocean Dark
val OceanColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    secondary = Color(0xFF0EA5E9),
    tertiary = Color(0xFF0284C7),
    background = Color(0xFF031424),
    surface = Color(0xFF0E3251),
    surfaceVariant = Color(0xFF074872),
    onPrimary = Color(0xFF031424),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFFF0F9FF),
    onSurface = Color(0xFFF0F9FF)
)

// Premium Sunset Dark (Amber / Gold)
val SunsetColorScheme = darkColorScheme(
    primary = Color(0xFFFBBF24),
    secondary = Color(0xFFF59E0B),
    tertiary = Color(0xFFD97706),
    background = Color(0xFF241203),
    surface = Color(0xFF4D2406),
    surfaceVariant = Color(0xFF633108),
    onPrimary = Color(0xFF241203),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFFFFFBEB),
    onSurface = Color(0xFFFFFBEB)
)

@Composable
fun ChronosTheme(
    themeName: String,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeName) {
        "Teal" -> TealColorScheme
        "Ocean" -> OceanColorScheme
        "Sunset" -> SunsetColorScheme
        "Slate" -> SlateColorScheme
        "Sleek" -> SleekColorScheme
        else -> SleekColorScheme // Default is the beautiful Sleek light theme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
