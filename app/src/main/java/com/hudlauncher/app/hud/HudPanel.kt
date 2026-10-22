package com.hudlauncher.app.hud

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hudlauncher.app.ui.theme.HudCyan
import com.hudlauncher.app.ui.theme.HudNeonPink

val HudPanelShape = RoundedCornerShape(20.dp)

val HudPanelBackground = Brush.verticalGradient(listOf(Color(0xF2341870), Color(0xF21B0B33)))
val HudPanelBorder = Brush.linearGradient(listOf(HudNeonPink, HudCyan))

/** Shared neon rounded panel used by dialogs, bars and other overlays. */
@Composable
fun HudPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier
            .clip(HudPanelShape)
            .background(HudPanelBackground)
            .border(1.5.dp, HudPanelBorder, HudPanelShape),
    ) { content() }
}
