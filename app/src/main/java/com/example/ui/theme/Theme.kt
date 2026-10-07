package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val MobbinColorScheme = lightColorScheme(
    primary = MobbinPrimary,
    onPrimary = MobbinOnPrimary,
    primaryContainer = TagWetGreenBg,
    onPrimaryContainer = MobbinInk,
    secondary = MobbinAccent,
    onSecondary = MobbinCanvas,
    secondaryContainer = MobbinCanvasSoft,
    onSecondaryContainer = MobbinInk,
    tertiary = TagWetGreen,
    onTertiary = MobbinCanvas,
    background = MobbinCanvas,
    surface = MobbinSurface,
    surfaceVariant = MobbinCanvasSoft,
    onBackground = MobbinInk,
    onSurface = MobbinInk,
    onSurfaceVariant = MobbinTextMuted,
    outline = MobbinHairline,
    outlineVariant = MobbinHairlineSoft,
    error = TagHazardRed,
    onError = MobbinCanvas
)

@Composable
fun CleanCityTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MobbinColorScheme,
        typography = Typography,
        content = content
    )
}
