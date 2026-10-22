package com.hudlauncher.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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

object HudStyle {
    @Composable
    fun switchColors() = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = HudNeonPink,
        checkedBorderColor = HudNeonPink,
        uncheckedThumbColor = HudOnPanel,
        uncheckedTrackColor = HudBarPurple,
        uncheckedBorderColor = HudViolet,
    )

    @Composable
    fun sliderColors() = SliderDefaults.colors(
        thumbColor = HudNeonPink,
        activeTrackColor = HudNeonPink,
        inactiveTrackColor = HudBarPurple,
    )

    @Composable
    fun buttonColors() = ButtonDefaults.buttonColors(containerColor = HudNeonPink, contentColor = HudDeepPurple)
}
