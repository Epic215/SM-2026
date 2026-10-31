package com.example.habittracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PurpleLight,
    onPrimary = Color.White,
    primaryContainer = PurpleContainerLight,
    onPrimaryContainer = PurpleLight,
    secondary = MintLight,
    onSecondary = Color.White,
    secondaryContainer = PurpleContainerLight,
    onSecondaryContainer = PurpleLight,
    tertiary = CoralLight,
    onTertiary = Color.White,
    background = BackgroundLight,
    onBackground = TextLight,
    surface = SurfaceLight,
    onSurface = TextLight,
    surfaceVariant = BackgroundLight,
    onSurfaceVariant = TextMutedLight,
    surfaceContainer = SurfaceLight,
    surfaceContainerLow = SurfaceLight,
    surfaceContainerHigh = SurfaceLight,
    outline = OutlineLight,
    outlineVariant = OutlineLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = PurpleDark,
    onPrimary = BackgroundDark,
    primaryContainer = PurpleContainerDark,
    onPrimaryContainer = PurpleDark,
    secondary = MintDark,
    onSecondary = BackgroundDark,
    secondaryContainer = PurpleContainerDark,
    onSecondaryContainer = PurpleDark,
    tertiary = CoralDark,
    onTertiary = BackgroundDark,
    background = BackgroundDark,
    onBackground = TextDark,
    surface = SurfaceDark,
    onSurface = TextDark,
    surfaceVariant = BackgroundDark,
    onSurfaceVariant = TextMutedDark,
    surfaceContainer = SurfaceDark,
    surfaceContainerLow = SurfaceDark,
    surfaceContainerHigh = SurfaceDark,
    outline = OutlineDark,
    outlineVariant = OutlineDark,
)

@Composable
fun HabitTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}