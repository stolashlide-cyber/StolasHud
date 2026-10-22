package com.hudlauncher.app.hud

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import kotlin.random.Random

private class Star(val x: Float, val y: Float, val r: Float, val a: Float, val sparkle: Boolean)

private fun generateStars(): List<Star> {
    val rnd = Random(42)
    return List(110) {
        Star(
            x = rnd.nextFloat(),
            y = rnd.nextFloat(),
            r = 0.6f + rnd.nextFloat() * 1.8f,
            a = 0.25f + rnd.nextFloat() * 0.7f,
            sparkle = it % 14 == 0,
        )
    }
}

/** Static, cheap wallpaper drawn with Canvas; no animation, no blur. */
@Composable
fun HudWallpaper(
    intensity: Float,
    dim: Float,
    modifier: Modifier = Modifier,
) {
    val stars = remember { generateStars() }
    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val i = intensity.coerceIn(0f, 1f)

        drawRect(
            Brush.verticalGradient(
                0f to Color(0xFF4B22B0),
                0.30f to Color(0xFF8A2BD8),
                0.55f to Color(0xFFB531C9),
                0.80f to Color(0xFF6A1A8E),
                1f to Color(0xFF2A0B4F),
            ),
        )
        glow(Offset(w * 0.12f, h * 0.10f), w * 0.75f, Color(0xFFFF4FD8), 0.55f * i)
        glow(Offset(w * 0.90f, h * 0.38f), w * 0.70f, Color(0xFF6C5CFF), 0.55f * i)
        glow(Offset(w * 0.45f, h * 0.88f), w * 0.85f, Color(0xFFFF2DAA), 0.50f * i)
        glow(Offset(w * 0.55f, h * 0.05f), w * 0.55f, Color(0xFF14082B), 0.65f)
        glow(Offset(w * 0.05f, h * 0.60f), w * 0.5f, Color(0xFF2B6BFF), 0.28f * i)

        // big translucent discs
        drawCircle(Color.White.copy(alpha = 0.10f * i), radius = w * 0.42f, center = Offset(w * 0.92f, h * 0.90f))
        drawCircle(Color.White.copy(alpha = 0.06f * i), radius = w * 0.30f, center = Offset(w * 0.08f, h * 0.36f))

        // glowing curves
        curve(
            Path().apply {
                moveTo(-w * 0.05f, h * 0.82f)
                cubicTo(w * 0.30f, h * 0.60f, w * 0.65f, h * 0.98f, w * 1.05f, h * 0.66f)
            },
            Color(0xFFFFB3F5), i,
        )
        curve(
            Path().apply {
                moveTo(w * 0.15f, h * 1.02f)
                cubicTo(w * 0.20f, h * 0.70f, w * 0.80f, h * 0.78f, w * 1.05f, h * 0.48f)
            },
            Color(0xFFE6D2FF), i * 0.8f,
        )
        curve(
            Path().apply {
                moveTo(-w * 0.05f, h * 0.30f)
                cubicTo(w * 0.25f, h * 0.18f, w * 0.40f, h * 0.45f, w * 0.75f, h * 0.30f)
            },
            Color(0xFFA9C4FF), i * 0.55f,
        )

        // stars and sparkles
        for (s in stars) {
            val c = Offset(s.x * w, s.y * h)
            val a = s.a * i
            if (s.sparkle) sparkle(c, s.r * 5f, a) else drawCircle(Color.White.copy(alpha = a), s.r, c)
        }

        // dim + de-saturation when intensity is low
        if (i < 1f) drawRect(Color(0xFF1B0B33).copy(alpha = (1f - i) * 0.55f))
        if (dim > 0f) drawRect(Color.Black.copy(alpha = dim.coerceIn(0f, 0.9f)))
    }
}

private fun DrawScope.glow(center: Offset, radius: Float, color: Color, alpha: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            listOf(color.copy(alpha = alpha), Color.Transparent),
            center = center,
            radius = radius,
        ),
        radius = radius,
        center = center,
    )
}

private fun DrawScope.curve(path: Path, color: Color, intensity: Float) {
    drawPath(path, color.copy(alpha = 0.10f * intensity), style = Stroke(width = 26f, cap = StrokeCap.Round))
    drawPath(path, color.copy(alpha = 0.22f * intensity), style = Stroke(width = 10f, cap = StrokeCap.Round))
    drawPath(path, color.copy(alpha = 0.75f * intensity), style = Stroke(width = 3f, cap = StrokeCap.Round))
}

private fun DrawScope.sparkle(c: Offset, r: Float, alpha: Float) {
    val p = Path().apply {
        moveTo(c.x, c.y - r)
        quadraticTo(c.x, c.y, c.x + r, c.y)
        quadraticTo(c.x, c.y, c.x, c.y + r)
        quadraticTo(c.x, c.y, c.x - r, c.y)
        quadraticTo(c.x, c.y, c.x, c.y - r)
        close()
    }
    drawPath(p, Color.White.copy(alpha = alpha))
}
