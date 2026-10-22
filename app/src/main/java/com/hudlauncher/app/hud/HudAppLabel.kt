package com.hudlauncher.app.hud

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hudlauncher.app.ui.theme.HudCondensed

private const val MIN_LABEL_SP = 5f

/**
 * Compact white ALL-CAPS caption. Always one line: if it does not fit the available
 * width the font shrinks in 0.5sp steps instead of using an ellipsis.
 */
@Composable
fun HudAppLabel(
    text: String,
    modifier: Modifier = Modifier,
    fontSizeSp: Float = 8f,
    letterSpacingSp: Float = 0.2f,
) {
    var sizeSp by remember(text, fontSizeSp) { mutableStateOf(fontSizeSp) }
    var fitted by remember(text, fontSizeSp) { mutableStateOf(false) }
    Text(
        text = text.uppercase(),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 1.dp)
            .drawWithContent { if (fitted) drawContent() },
        style = TextStyle(
            color = Color.White,
            fontFamily = HudCondensed,
            fontSize = sizeSp.sp,
            letterSpacing = letterSpacingSp.sp,
            textAlign = TextAlign.Center,
            shadow = Shadow(Color(0x99000000), Offset(0f, 1f), 2f),
        ),
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip,
        onTextLayout = { result ->
            if (result.didOverflowWidth && sizeSp > MIN_LABEL_SP) {
                sizeSp = (sizeSp - 0.5f).coerceAtLeast(MIN_LABEL_SP)
            } else {
                fitted = true
            }
        },
    )
}
