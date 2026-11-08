package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisColorScheme = darkColorScheme(
    primary = ArcCyan,
    onPrimary = VoidBlack,
    primaryContainer = HudSurfaceVariant,
    onPrimaryContainer = ArcCyan,
    secondary = StarkGold,
    onSecondary = VoidBlack,
    secondaryContainer = HudSurface,
    onSecondaryContainer = StarkGold,
    tertiary = DivineSaffron,
    onTertiary = Color.White,
    background = VoidBlack,
    onBackground = TextPrimary,
    surface = DarkHudBg,
    onSurface = TextPrimary,
    surfaceVariant = HudSurface,
    onSurfaceVariant = TextSecondary,
    outline = HudBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = JarvisTypography,
        content = content
    )
}
