package com.hudlauncher.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val HudColors = darkColorScheme(
    primary = HudNeonPink,
    onPrimary = HudDeepPurple,
    secondary = HudCyan,
    background = HudDeepPurple,
    onBackground = HudOnPanel,
    surface = HudPanel,
    onSurface = HudOnPanel,
    surfaceVariant = HudBarPurple,
    onSurfaceVariant = HudOnPanel,
    surfaceContainer = HudPanel,
    surfaceContainerHigh = HudPanel,
    surfaceContainerHighest = HudBarPurple,
)

/** Material 3 is used only as a technical base for sheets, dialogs and sliders. */
@Composable
fun HudTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = HudColors, typography = HudTypography, content = content)
}
