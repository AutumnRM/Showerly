package com.showerly.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = WaterPrimary,
    onPrimary = WaterOnPrimary,
    primaryContainer = WaterPrimaryContainer,
    onPrimaryContainer = WaterOnPrimaryContainer,
    secondary = WaterSecondary,
    onSecondary = WaterOnSecondary,
    secondaryContainer = WaterSecondaryContainer,
    onSecondaryContainer = WaterOnSecondaryContainer,
    tertiary = WaterTertiary,
    background = WaterBackground,
    surface = WaterSurface,
    onSurface = WaterOnSurface,
    onBackground = WaterOnSurface,
    outline = WaterOutline
)

private val DarkColors = darkColorScheme(
    primary = WaterPrimaryDark,
    onPrimary = WaterOnPrimaryDark,
    primaryContainer = WaterPrimaryContainerDark,
    onPrimaryContainer = WaterOnPrimaryContainerDark,
    secondary = WaterSecondaryDark,
    onSecondary = WaterOnSecondaryDark,
    secondaryContainer = WaterSecondaryContainerDark,
    onSecondaryContainer = WaterOnSecondaryContainerDark,
    tertiary = WaterTertiaryDark,
    background = WaterBackgroundDark,
    surface = WaterSurfaceDark,
    onSurface = WaterOnSurfaceDark,
    onBackground = WaterOnSurfaceDark,
    outline = WaterOutlineDark
)

@Composable
fun ShowerlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = ShowerlyTypography,
        content = content
    )
}
