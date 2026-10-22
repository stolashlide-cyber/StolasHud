package com.hudlauncher.app.hud

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hudlauncher.app.data.DmbTimer
import com.hudlauncher.app.ui.theme.HudCondensedBold
import com.hudlauncher.app.ui.theme.HudCyan
import com.hudlauncher.app.ui.theme.HudNeonPink
import kotlinx.coroutines.delay

@Composable
fun DmbTimerPanel(startDay: Long, modifier: Modifier = Modifier) {
    var today by remember { mutableLongStateOf(DmbTimer.today()) }
    // Re-check the date every minute so the counter rolls over at midnight.
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            today = DmbTimer.today()
        }
    }
    val left = DmbTimer.daysLeft(startDay, today)
    val progress = DmbTimer.progress(startDay, today)

    HudPanel(modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "ДМБ",
                    style = TextStyle(color = HudNeonPink, fontSize = 14.sp, fontFamily = HudCondensedBold, fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    modifier = Modifier.weight(1f),
                )
                Text(
                    if (left == 0) "ДМБ НАСТУПИЛ!" else "ОСТАЛОСЬ $left ДН.",
                    style = TextStyle(color = Color.White, fontSize = 14.sp, fontFamily = HudCondensedBold, fontWeight = FontWeight.Bold),
                )
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.15f)),
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(HudCyan),
                )
            }
            Text(
                "СЛУЖБА ${DmbTimer.SERVICE_DAYS} ДН. · ПРОЙДЕНО ${(progress * 100).toInt()}%",
                style = TextStyle(color = Color.White.copy(alpha = 0.65f), fontSize = 10.sp, fontFamily = HudCondensedBold),
            )
        }
    }
}
