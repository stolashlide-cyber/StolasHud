package com.hudlauncher.app.ui.theme

import android.graphics.Typeface
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily

/** Slightly condensed sans used for HUD captions. */
val HudCondensed: FontFamily = FontFamily(Typeface.create("sans-serif-condensed", Typeface.NORMAL))
val HudCondensedBold: FontFamily = FontFamily(Typeface.create("sans-serif-condensed", Typeface.BOLD))

private fun TextStyle.hud() = copy(fontFamily = HudCondensed)

val HudTypography: Typography = Typography().let {
    it.copy(
        displayLarge = it.displayLarge.hud(), displayMedium = it.displayMedium.hud(),
        displaySmall = it.displaySmall.hud(), headlineLarge = it.headlineLarge.hud(),
        headlineMedium = it.headlineMedium.hud(), headlineSmall = it.headlineSmall.hud(),
        titleLarge = it.titleLarge.hud(), titleMedium = it.titleMedium.hud(),
        titleSmall = it.titleSmall.hud(), bodyLarge = it.bodyLarge.hud(),
        bodyMedium = it.bodyMedium.hud(), bodySmall = it.bodySmall.hud(),
        labelLarge = it.labelLarge.hud(), labelMedium = it.labelMedium.hud(),
        labelSmall = it.labelSmall.hud(),
    )
}
