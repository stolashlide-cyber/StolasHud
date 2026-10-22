package com.hudlauncher.app.hud

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hudlauncher.app.data.DefaultHudSlots
import com.hudlauncher.app.data.HudSlot
import com.hudlauncher.app.ui.theme.HudNeonPink
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HudAppIcon(
    slot: HudSlot,
    iconSize: Dp,
    labelSizeSp: Float,
    labelGap: Dp,
    showLabel: Boolean,
    editMode: Boolean,
    animationIntensity: Float,
    index: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val animated = animationIntensity > 0.05f
    val pressScale = remember { Animatable(1f) }
    val enter = remember { Animatable(if (animated) 0f else 1f) }
    val wiggle = remember { Animatable(0f) }
    val currentClick by rememberUpdatedState(onClick)
    val currentLongClick by rememberUpdatedState(onLongClick)

    // launch animation: fade + scale 0.92 -> 1.0 with a small stagger
    LaunchedEffect(Unit) {
        if (enter.value < 1f) {
            delay((index * 25L).coerceAtMost(500L))
            enter.animateTo(1f, tween(220))
        }
    }

    // edit-mode wobble, only runs while editing
    LaunchedEffect(editMode, animated, animationIntensity) {
        if (editMode && animated) {
            val amplitude = 1.3f * animationIntensity
            val duration = 190 + (index % 5) * 15
            wiggle.snapTo(-amplitude)
            while (true) {
                wiggle.animateTo(amplitude, tween(duration, easing = LinearEasing))
                wiggle.animateTo(-amplitude, tween(duration, easing = LinearEasing))
            }
        } else {
            wiggle.animateTo(0f, tween(100))
        }
    }

    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .graphicsLayer {
                    val s = pressScale.value * (0.92f + 0.08f * enter.value)
                    scaleX = s
                    scaleY = s
                    rotationZ = wiggle.value
                    alpha = enter.value
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            scope.launch {
                                if (animated) {
                                    pressScale.animateTo(0.90f, tween(70))
                                    currentClick()
                                    pressScale.animateTo(1f, tween(90))
                                } else {
                                    currentClick()
                                }
                            }
                        },
                        onLongPress = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            currentLongClick()
                        },
                    )
                },
        ) {
            Box(Modifier.size(iconSize)) {
                HudIconRenderer(slot.iconType, Modifier.size(iconSize))
                if (editMode) EditBadge(Modifier.align(Alignment.TopEnd))
            }
            if (showLabel) {
                Spacer(Modifier.height(labelGap))
                HudAppLabel(text = slot.displayName, fontSizeSp = labelSizeSp)
            }
        }
    }
}

@Composable
private fun EditBadge(modifier: Modifier = Modifier) {
    Canvas(modifier.size(18.dp)) {
        drawCircle(HudNeonPink, radius = size.minDimension / 2)
        drawCircle(Color.White, radius = size.minDimension / 2, style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f))
        drawLine(
            Color.White,
            Offset(size.width * 0.3f, size.height * 0.7f),
            Offset(size.width * 0.7f, size.height * 0.3f),
            strokeWidth = size.width * 0.12f,
            cap = StrokeCap.Round,
        )
    }
}

@Preview
@Composable
private fun HudAppIconPreview() {
    HudAppIcon(
        slot = DefaultHudSlots.all[11],
        iconSize = 72.dp,
        labelSizeSp = 8f,
        labelGap = 2.dp,
        showLabel = true,
        editMode = false,
        animationIntensity = 0f,
        index = 0,
        onClick = {},
        onLongClick = {},
    )
}
