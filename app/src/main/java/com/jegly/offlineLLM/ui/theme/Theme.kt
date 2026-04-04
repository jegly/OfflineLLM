package com.jegly.offlineLLM.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class ThemeMode(val label: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark"),
    AMOLED("AMOLED Black"),
}

data class AccentColor(
    val key: String,
    val label: String,
    val seed: Color,
)

val accentColors = listOf(
    AccentColor("dynamic", "Device Default", Color(0xFF6750A4)),
    AccentColor("blue", "Blue", Color(0xFF1976D2)),
    AccentColor("green", "Green", Color(0xFF388E3C)),
    AccentColor("purple", "Purple", Color(0xFF7B1FA2)),
    AccentColor("orange", "Orange", Color(0xFFE65100)),
    AccentColor("red", "Red", Color(0xFFD32F2F)),
    AccentColor("teal", "Teal", Color(0xFF00796B)),
    AccentColor("pink", "Pink", Color(0xFFC2185B)),
    AccentColor("amber", "Amber", Color(0xFFFFA000)),
)

private fun buildColorScheme(seed: Color, isDark: Boolean): ColorScheme {
    val primary = seed
    val onPrimary = if (isDark) Color.Black else Color.White
    val primaryContainer = if (isDark) seed.copy(alpha = 0.3f) else seed.copy(alpha = 0.15f)
    val onPrimaryContainer = if (isDark) seed.copy(alpha = 0.9f) else seed.copy(alpha = 0.85f)

    return if (isDark) {
        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
        )
    }
}

@Composable
fun OfflineLLMTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accentColorKey: String = "dynamic",
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.AMOLED -> true
    }

    val accent = accentColors.find { it.key == accentColorKey }

    val colorScheme = when {
        // AMOLED with custom accent
        themeMode == ThemeMode.AMOLED && accent != null && accent.key != "dynamic" -> {
            buildColorScheme(accent.seed, true).copy(
                background = Color.Black,
                surface = Color.Black,
                surfaceVariant = Color(0xFF1A1A1A),
                surfaceContainer = Color(0xFF0D0D0D),
                surfaceContainerHigh = Color(0xFF1A1A1A),
                surfaceContainerHighest = Color(0xFF222222),
            )
        }
        // AMOLED with device dynamic
        themeMode == ThemeMode.AMOLED -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                dynamicDarkColorScheme(LocalContext.current).copy(
                    background = Color.Black,
                    surface = Color.Black,
                    surfaceVariant = Color(0xFF1A1A1A),
                )
            } else {
                darkColorScheme(
                    background = Color.Black,
                    surface = Color.Black,
                    surfaceVariant = Color(0xFF1A1A1A),
                )
            }
        }
        // Custom accent colour
        accent != null && accent.key != "dynamic" -> {
            buildColorScheme(accent.seed, isDark)
        }
        // Device dynamic colour (Android 12+)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Fallback
        isDark -> darkColorScheme()
        else -> lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
