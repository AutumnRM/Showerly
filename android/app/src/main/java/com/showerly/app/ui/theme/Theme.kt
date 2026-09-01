package com.showerly.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import com.showerly.app.domain.model.DarkModePref

private const val DEFAULT_SEED = 0xFF00657A

@Composable
fun ShowerlyTheme(
    darkPref: DarkModePref = DarkModePref.SYSTEM,
    content: @Composable () -> Unit
) {
    val dark = when (darkPref) {
        DarkModePref.LIGHT -> false
        DarkModePref.DARK -> true
        DarkModePref.SYSTEM -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = buildScheme(DEFAULT_SEED, dark),
        typography = ShowerlyTypography,
        content = content
    )
}

private fun buildScheme(argb: Long, dark: Boolean): ColorScheme {
    val seed = Color(argb)
    val onSeed = if (seed.luminance() > 0.5f) Color.Black else Color.White
    val container = lerp(seed, if (dark) Color.Black else Color.White, 0.82f)
    val onContainer = if (dark) Color.White else Color.Black
    val secondary = lerp(seed, if (dark) Color.White else Color.Black, 0.30f)
    val tertiary = lerp(seed, if (dark) Color.White else Color.Black, 0.55f)
    val background = if (dark) Color(0xFF121418) else Color(0xFFFBFCFE)
    val surface = background
    val surfaceVariant = if (dark) Color(0xFF262B2E) else Color(0xFFE1E3E5)
    val onSurface = if (dark) Color(0xFFE0E3E5) else Color(0xFF191C1E)
    val onSurfaceVariant = if (dark) Color(0xFFC0C7C9) else Color(0xFF5C6365)
    val outline = if (dark) Color(0xFF8A9397) else Color(0xFF70797C)
    return if (dark) {
        darkColorScheme(
            primary = seed, onPrimary = onSeed,
            primaryContainer = container, onPrimaryContainer = onContainer,
            secondary = secondary, tertiary = tertiary,
            background = background, onBackground = onSurface,
            surface = surface, onSurface = onSurface,
            surfaceVariant = surfaceVariant, onSurfaceVariant = onSurfaceVariant,
            outline = outline
        )
    } else {
        lightColorScheme(
            primary = seed, onPrimary = onSeed,
            primaryContainer = container, onPrimaryContainer = onContainer,
            secondary = secondary, tertiary = tertiary,
            background = background, onBackground = onSurface,
            surface = surface, onSurface = onSurface,
            surfaceVariant = surfaceVariant, onSurfaceVariant = onSurfaceVariant,
            outline = outline
        )
    }
}