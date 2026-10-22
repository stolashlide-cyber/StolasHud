package com.hudlauncher.app.hud

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.BatteryManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.union
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hudlauncher.app.ui.theme.HudBarPurple
import com.hudlauncher.app.ui.theme.HudCondensedBold

private val BatteryGreen = Color(0xFF4DFF6A)
private val BatteryLow = Color(0xFFFF4B4B)

@Composable
fun rememberBatteryLevel(): State<Int> {
    val inspection = LocalInspectionMode.current
    val context = LocalContext.current
    val level = remember { mutableStateOf(95) }
    if (inspection) return level
    DisposableEffect(context) {
        fun read(intent: Intent?) {
            val l = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val s = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
            if (l >= 0 && s > 0) level.value = (l * 100 / s).coerceIn(0, 100)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) = read(intent)
        }
        read(context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED)))
        onDispose { context.unregisterReceiver(receiver) }
    }
    return level
}

@Composable
fun rememberNetworkAvailable(): State<Boolean> {
    val inspection = LocalInspectionMode.current
    val context = LocalContext.current
    val available = remember { mutableStateOf(true) }
    if (inspection) return available
    DisposableEffect(context) {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        if (cm == null) {
            onDispose { }
        } else {
            val caps = cm.getNetworkCapabilities(cm.activeNetwork)
            available.value = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) { available.value = true }
                override fun onLost(network: Network) { available.value = false }
            }
            val registered = try {
                cm.registerDefaultNetworkCallback(callback)
                true
            } catch (e: Exception) {
                false
            }
            onDispose { if (registered) runCatching { cm.unregisterNetworkCallback(callback) } }
        }
    }
    return available
}

@Composable
fun HudStatusBar(
    showSignal: Boolean,
    showBatteryPercent: Boolean,
    modifier: Modifier = Modifier,
    onLongPress: () -> Unit = {},
) {
    val battery by rememberBatteryLevel()
    val network by rememberNetworkAvailable()
    val currentLongPress by rememberUpdatedState(onLongPress)

    Box(
        modifier
            .fillMaxWidth()
            .background(HudBarPurple)
            .pointerInput(Unit) { detectTapGestures(onLongPress = { currentLongPress() }) },
    ) {
        Row(
            Modifier
                .windowInsetsPadding(
                    WindowInsets.statusBars.union(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)),
                )
                .fillMaxWidth()
                .height(34.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showSignal) {
                SignalBars(active = network)
                Spacer(Modifier.width(6.dp))
            }
            LockIcon()
            Spacer(Modifier.weight(1f))
            if (showBatteryPercent) {
                Text(
                    text = "$battery%",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = HudCondensedBold,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Spacer(Modifier.width(6.dp))
            }
            BatteryIcon(level = battery)
        }
    }
}

@Composable
private fun SignalBars(active: Boolean) {
    Canvas(Modifier.size(width = 20.dp, height = 14.dp)) {
        val barW = size.width / 4.6f
        val gap = (size.width - barW * 4) / 3
        val color = if (active) Color.White else Color.White.copy(alpha = 0.3f)
        for (i in 0 until 4) {
            val hFrac = 0.35f + 0.65f * (i / 3f)
            val barH = size.height * hFrac
            drawRoundRect(
                color,
                topLeft = Offset(i * (barW + gap), size.height - barH),
                size = Size(barW, barH),
                cornerRadius = CornerRadius(barW * 0.25f),
            )
        }
    }
}

@Composable
private fun LockIcon() {
    Canvas(Modifier.size(width = 11.dp, height = 14.dp)) {
        val stroke = size.width * 0.16f
        val bodyTop = size.height * 0.45f
        drawRoundRect(
            Color.White,
            topLeft = Offset(0f, bodyTop),
            size = Size(size.width, size.height - bodyTop),
            cornerRadius = CornerRadius(size.width * 0.18f),
        )
        drawArc(
            Color.White,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(size.width * 0.2f, stroke / 2),
            size = Size(size.width * 0.6f, size.height * 0.62f),
            style = Stroke(stroke, cap = StrokeCap.Butt),
        )
    }
}

@Composable
private fun BatteryIcon(level: Int) {
    Canvas(Modifier.size(width = 36.dp, height = 16.dp)) {
        val nub = size.width * 0.07f
        val bodyW = size.width - nub
        val stroke = 1.6.dp.toPx()
        drawRoundRect(
            Color.White,
            topLeft = Offset(stroke / 2, stroke / 2),
            size = Size(bodyW - stroke, size.height - stroke),
            cornerRadius = CornerRadius(3.dp.toPx()),
            style = Stroke(stroke),
        )
        drawRoundRect(
            Color.White,
            topLeft = Offset(bodyW, size.height * 0.3f),
            size = Size(nub, size.height * 0.4f),
            cornerRadius = CornerRadius(nub / 2),
        )
        val pad = stroke + 1.2.dp.toPx()
        val fullW = bodyW - pad * 2
        val fillW = (fullW * level / 100f).coerceAtLeast(if (level > 0) 2.dp.toPx() else 0f)
        drawRoundRect(
            if (level <= 15) BatteryLow else BatteryGreen,
            topLeft = Offset(pad, pad),
            size = Size(fillW, size.height - pad * 2),
            cornerRadius = CornerRadius(1.5.dp.toPx()),
        )
    }
}

@Preview(widthDp = 360)
@Composable
private fun HudStatusBarPreview() {
    HudStatusBar(showSignal = true, showBatteryPercent = true)
}
