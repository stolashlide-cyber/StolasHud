package com.hudlauncher.app.hud

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.hudlauncher.app.R
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import com.hudlauncher.app.data.HudIconType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val Paper = Color(0xFFFFFFFF)
private val Red = Color(0xFFD3101F)
private val Ink = Color(0xFF15101F)

/** Draws one stylised HUD icon (container included) filling the available square. */
@Composable
fun HudIconRenderer(type: HudIconType, modifier: Modifier = Modifier) {
    if (type == HudIconType.VOXTUBE) {
        Image(
            painter = painterResource(R.drawable.ic_voxtube),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = modifier,
        )
        return
    }
    Canvas(modifier) { drawHudIcon(type) }
}

private fun DrawScope.f(v: Float) = v * size.minDimension
private fun DrawScope.p(x: Float, y: Float) = Offset(f(x), f(y))

private fun DrawScope.container(brush: Brush, border: Color = Color.White.copy(alpha = 0.55f)) {
    val r = CornerRadius(f(0.23f))
    drawRoundRect(brush, cornerRadius = r)
    drawRoundRect(border, cornerRadius = r, style = Stroke(f(0.012f)))
}

private fun DrawScope.container(color: Color, border: Color = Color.White.copy(alpha = 0.55f)) =
    container(SolidColor(color), border)

private fun DrawScope.text(
    s: String, cx: Float, baseline: Float, sizeFrac: Float, color: Color,
    style: Int = Typeface.BOLD,
) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color.toArgb()
        textSize = f(sizeFrac)
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, style)
    }
    drawIntoCanvas { it.nativeCanvas.drawText(s, f(cx), f(baseline), paint) }
}

private fun heart(cx: Float, cy: Float, w: Float, h: Float) = Path().apply {
    moveTo(cx, cy + h * 0.5f)
    cubicTo(cx - w * 0.75f, cy + h * 0.05f, cx - w * 0.45f, cy - h * 0.55f, cx, cy - h * 0.15f)
    cubicTo(cx + w * 0.45f, cy - h * 0.55f, cx + w * 0.75f, cy + h * 0.05f, cx, cy + h * 0.5f)
    close()
}

private fun DrawScope.drawZ(x: Float, y: Float, z: Float, color: Color) {
    val path = Path().apply {
        moveTo(f(x), f(y)); lineTo(f(x + z), f(y)); lineTo(f(x), f(y + z)); lineTo(f(x + z), f(y + z))
    }
    drawPath(path, color, style = Stroke(f(0.035f), cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.drawHudIcon(type: HudIconType) {
    when (type) {
        HudIconType.CALENDAR -> {
            container(Paper)
            val x = 0.17f; val y = 0.2f; val w = 0.66f; val h = 0.62f
            drawRoundRect(Red, p(x, y), Size(f(w), f(h)), CornerRadius(f(0.04f)), style = Stroke(f(0.04f)))
            drawRect(Red, p(x, y), Size(f(w), f(0.12f)))
            for (i in 1..3) {
                val gx = x + w * i / 4
                drawLine(Red, p(gx, y + 0.12f), p(gx, y + h), f(0.02f))
                val gy = y + 0.12f + (h - 0.12f) * i / 4
                drawLine(Red, p(x, gy), p(x + w, gy), f(0.02f))
            }
            drawRoundRect(Ink, p(0.3f, 0.14f), Size(f(0.05f), f(0.12f)), CornerRadius(f(0.025f)))
            drawRoundRect(Ink, p(0.65f, 0.14f), Size(f(0.05f), f(0.12f)), CornerRadius(f(0.025f)))
        }

        HudIconType.CAMERA -> {
            container(Paper)
            drawRoundRect(Red, p(0.14f, 0.3f), Size(f(0.72f), f(0.46f)), CornerRadius(f(0.07f)))
            drawRoundRect(Red, p(0.34f, 0.22f), Size(f(0.3f), f(0.12f)), CornerRadius(f(0.03f)))
            drawCircle(Paper, f(0.15f), p(0.5f, 0.53f))
            drawCircle(Ink, f(0.1f), p(0.5f, 0.53f))
            drawCircle(Color(0xFFFF5A6A), f(0.045f), p(0.47f, 0.5f))
            drawCircle(Paper, f(0.025f), p(0.76f, 0.4f))
        }

        HudIconType.GALLERY -> {
            container(Paper)
            drawRoundRect(Ink, p(0.3f, 0.3f), Size(f(0.54f), f(0.54f)), CornerRadius(f(0.05f)))
            drawRoundRect(Ink, p(0.16f, 0.16f), Size(f(0.54f), f(0.54f)), CornerRadius(f(0.05f)))
            drawRoundRect(Red, p(0.2f, 0.2f), Size(f(0.46f), f(0.46f)), CornerRadius(f(0.03f)))
            val m = Path().apply {
                moveTo(f(0.2f), f(0.66f)); lineTo(f(0.34f), f(0.44f)); lineTo(f(0.43f), f(0.56f))
                lineTo(f(0.52f), f(0.4f)); lineTo(f(0.66f), f(0.66f)); close()
            }
            drawPath(m, Paper)
            drawCircle(Paper, f(0.045f), p(0.55f, 0.29f))
        }

        HudIconType.EMAILS -> {
            container(Paper)
            val horn = Path().apply { moveTo(f(0.22f), f(0.3f)); lineTo(f(0.2f), f(0.14f)); lineTo(f(0.34f), f(0.26f)); close() }
            drawPath(horn, Ink)
            val horn2 = Path().apply { moveTo(f(0.78f), f(0.3f)); lineTo(f(0.8f), f(0.14f)); lineTo(f(0.66f), f(0.26f)); close() }
            drawPath(horn2, Ink)
            drawRoundRect(Red, p(0.14f, 0.26f), Size(f(0.72f), f(0.48f)), CornerRadius(f(0.05f)))
            drawRoundRect(Ink, p(0.14f, 0.26f), Size(f(0.72f), f(0.48f)), CornerRadius(f(0.05f)), style = Stroke(f(0.02f)))
            val flap = Path().apply { moveTo(f(0.16f), f(0.3f)); lineTo(f(0.5f), f(0.56f)); lineTo(f(0.84f), f(0.3f)) }
            drawPath(flap, Paper, style = Stroke(f(0.035f), cap = StrokeCap.Round, join = StrokeJoin.Round))
            val tail = Path().apply { moveTo(f(0.62f), f(0.74f)); cubicTo(f(0.7f), f(0.92f), f(0.9f), f(0.8f), f(0.86f), f(0.9f)) }
            drawPath(tail, Ink, style = Stroke(f(0.03f), cap = StrokeCap.Round))
        }

        HudIconType.MESSAGES -> {
            container(Paper)
            val red = Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(p(0.42f, 0.36f), Size(f(0.46f), f(0.38f))))
                moveTo(f(0.75f), f(0.7f)); lineTo(f(0.84f), f(0.86f)); lineTo(f(0.64f), f(0.74f)); close()
            }
            drawPath(red, Red)
            val bubble = Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(p(0.12f, 0.16f), Size(f(0.64f), f(0.5f))))
                moveTo(f(0.26f), f(0.58f)); lineTo(f(0.18f), f(0.78f)); lineTo(f(0.42f), f(0.64f)); close()
            }
            drawPath(bubble, Ink)
            for (i in 0..2) drawCircle(Color(0xFFFF3B4E), f(0.04f), p(0.28f + i * 0.14f, 0.41f))
        }

        HudIconType.FIND_ME -> {
            container(Paper)
            val c = p(0.5f, 0.5f)
            drawRoundRect(Ink, p(0.16f, 0.16f), Size(f(0.68f), f(0.68f)), CornerRadius(f(0.12f)))
            drawCircle(Red, f(0.27f), c, style = Stroke(f(0.018f)))
            drawCircle(Red, f(0.15f), c, style = Stroke(f(0.018f)))
            drawLine(Red, p(0.16f, 0.5f), p(0.84f, 0.5f), f(0.014f))
            drawLine(Red, p(0.5f, 0.16f), p(0.5f, 0.84f), f(0.014f))
            drawLine(Red, p(0.26f, 0.26f), p(0.74f, 0.74f), f(0.01f))
            drawCircle(Color(0xFFFF4B5C), f(0.04f), p(0.38f, 0.36f))
        }

        HudIconType.MAPS -> {
            container(Paper)
            val ring = listOf(
                Color(0xFFE53935), Color(0xFFFB8C00), Color(0xFFFDD835),
                Color(0xFF43A047), Color(0xFF1E88E5), Color(0xFF8E24AA),
            )
            ring.forEachIndexed { i, col ->
                val inset = 0.12f + i * 0.035f
                drawOval(
                    col,
                    p(inset, 0.30f + i * 0.02f),
                    Size(f(1f - inset * 2), f(0.5f - i * 0.04f)),
                    style = Stroke(f(0.03f)),
                )
            }
            val pin = Path().apply {
                moveTo(f(0.5f), f(0.84f))
                cubicTo(f(0.28f), f(0.56f), f(0.3f), f(0.16f), f(0.5f), f(0.16f))
                cubicTo(f(0.7f), f(0.16f), f(0.72f), f(0.56f), f(0.5f), f(0.84f))
                close()
            }
            drawPath(pin, Ink)
            drawCircle(Paper, f(0.07f), p(0.5f, 0.38f))
        }

        HudIconType.SLEEPY_PILLZ -> {
            container(Brush.linearGradient(listOf(Color(0xFFD67BFF), Color(0xFF9B30E8)), Offset.Zero, Offset(size.width, size.height)))
            rotate(-35f, p(0.45f, 0.62f)) {
                drawRoundRect(Paper, p(0.18f, 0.5f), Size(f(0.54f), f(0.25f)), CornerRadius(f(0.125f)))
                drawRoundRect(
                    Color(0xFF5B1A9A), p(0.45f, 0.5f), Size(f(0.27f), f(0.25f)),
                    CornerRadius(f(0.125f), f(0.125f)),
                )
                drawRect(Color(0xFF5B1A9A), p(0.45f, 0.5f), Size(f(0.1f), f(0.25f)))
                drawLine(Color(0xFFD9C2F2), p(0.45f, 0.5f), p(0.45f, 0.75f), f(0.012f))
            }
            drawZ(0.56f, 0.12f, 0.1f, Paper)
            drawZ(0.7f, 0.2f, 0.13f, Paper)
            drawZ(0.16f, 0.14f, 0.08f, Color(0xFFF2D9FF))
        }

        HudIconType.VOXFLIX -> {
            container(Color(0xFF0B1620), Color(0xFF53E8F5).copy(alpha = 0.5f))
            val cyan = Color(0xFF53E8F5)
            drawRoundRect(cyan, p(0.17f, 0.32f), Size(f(0.66f), f(0.46f)), CornerRadius(f(0.07f)), style = Stroke(f(0.035f)))
            drawLine(cyan, p(0.5f, 0.32f), p(0.36f, 0.18f), f(0.03f), StrokeCap.Round)
            drawLine(cyan, p(0.5f, 0.32f), p(0.64f, 0.18f), f(0.03f), StrokeCap.Round)
            val v = Path().apply {
                moveTo(f(0.3f), f(0.42f)); lineTo(f(0.4f), f(0.66f)); lineTo(f(0.5f), f(0.46f))
                lineTo(f(0.6f), f(0.66f)); lineTo(f(0.7f), f(0.42f))
            }
            drawPath(v, cyan, style = Stroke(f(0.045f), cap = StrokeCap.Round, join = StrokeJoin.Round))
        }

        HudIconType.PLANT_LOVER -> {
            val leaf = Path().apply {
                moveTo(f(0.18f), f(0.86f))
                cubicTo(f(0.0f), f(0.4f), f(0.4f), f(0.1f), f(0.9f), f(0.1f))
                cubicTo(f(0.96f), f(0.6f), f(0.6f), f(0.98f), f(0.18f), f(0.86f))
                close()
            }
            drawPath(leaf, Brush.linearGradient(listOf(Color(0xFFB8F23A), Color(0xFF2FB52A)), p(0.2f, 0.1f), p(0.9f, 0.9f)))
            drawPath(leaf, Color(0xFF1F7D1C), style = Stroke(f(0.02f)))
            drawLine(Color(0xFF1F7D1C), p(0.16f, 0.88f), p(0.7f, 0.3f), f(0.025f), StrokeCap.Round)
            drawLine(Color(0xFF1F7D1C), p(0.4f, 0.64f), p(0.5f, 0.74f), f(0.015f), StrokeCap.Round)
            drawLine(Color(0xFF1F7D1C), p(0.5f, 0.52f), p(0.62f, 0.62f), f(0.015f), StrokeCap.Round)
            sparkle(p(0.14f, 0.2f), f(0.08f), Color(0xFFFFF176))
            sparkle(p(0.3f, 0.08f), f(0.04f), Color.White)
        }

        HudIconType.AUDIO_TOMES -> {
            container(Color(0xFF1C1033), Color(0xFF8A5BD6).copy(alpha = 0.6f))
            drawCircle(Color(0xFF3B2160), f(0.1f), p(0.5f, 0.22f))
            drawRoundRect(Color(0xFF3B2160), p(0.4f, 0.3f), Size(f(0.2f), f(0.2f)), CornerRadius(f(0.08f)))
            val teal = Color(0xFF17B3B6)
            val left = Path().apply { moveTo(f(0.5f), f(0.5f)); lineTo(f(0.16f), f(0.44f)); lineTo(f(0.16f), f(0.78f)); lineTo(f(0.5f), f(0.84f)); close() }
            val right = Path().apply { moveTo(f(0.5f), f(0.5f)); lineTo(f(0.84f), f(0.44f)); lineTo(f(0.84f), f(0.78f)); lineTo(f(0.5f), f(0.84f)); close() }
            drawPath(left, teal); drawPath(right, Color(0xFF0E8F94))
            drawLine(Color(0xFFBFF6F6), p(0.5f, 0.5f), p(0.5f, 0.84f), f(0.015f))
            drawRect(Color(0xFFFF7BB5), p(0.54f, 0.5f), Size(f(0.045f), f(0.2f)))
        }

        HudIconType.MONEYMAKERZ_BANK -> {
            container(Color(0xFF5DFF3B), Color.White.copy(alpha = 0.5f))
            val horn = Path().apply {
                moveTo(f(0.5f), f(0.55f)); lineTo(f(0.16f), f(0.2f)); lineTo(f(0.3f), f(0.46f)); close()
            }
            drawPath(horn, Ink)
            val horn2 = Path().apply {
                moveTo(f(0.5f), f(0.55f)); lineTo(f(0.84f), f(0.2f)); lineTo(f(0.7f), f(0.46f)); close()
            }
            drawPath(horn2, Ink)
            drawCircle(Color.White, f(0.035f), p(0.16f, 0.2f)); drawCircle(Color.White, f(0.035f), p(0.84f, 0.2f))
            drawPath(heart(0.5f * size.width, 0.58f * size.height, f(0.62f), f(0.56f)), Ink)
            drawCircle(Color(0xFF5DFF3B), f(0.14f), p(0.5f, 0.55f))
            text("$", 0.5f, 0.62f, 0.2f, Ink)
            val tail = Path().apply { moveTo(f(0.5f), f(0.86f)); cubicTo(f(0.4f), f(0.96f), f(0.6f), f(0.96f), f(0.5f), f(1.0f)) }
            drawPath(tail, Ink, style = Stroke(f(0.02f)))
        }

        HudIconType.SINSTAGRAM -> {
            container(
                Brush.linearGradient(
                    listOf(Color(0xFFFFC21A), Color(0xFFFF2D6F), Color(0xFFC200D6)),
                    Offset(0f, size.height), Offset(size.width, 0f),
                ),
            )
            drawRoundRect(Paper, p(0.2f, 0.2f), Size(f(0.6f), f(0.6f)), CornerRadius(f(0.17f)), style = Stroke(f(0.04f)))
            drawCircle(Paper, f(0.19f), p(0.5f, 0.5f), style = Stroke(f(0.035f)))
            val pts = List(5) { k ->
                val a = (-90.0 + k * 72.0) * PI / 180.0
                Offset(f(0.5f) + f(0.15f) * cos(a).toFloat(), f(0.5f) + f(0.15f) * sin(a).toFloat())
            }
            val star = Path().apply {
                moveTo(pts[0].x, pts[0].y)
                for (k in intArrayOf(2, 4, 1, 3)) lineTo(pts[k].x, pts[k].y)
                close()
            }
            drawPath(star, Paper, style = Stroke(f(0.022f), join = StrokeJoin.Round))
            drawCircle(Paper, f(0.03f), p(0.71f, 0.29f))
        }

        HudIconType.ENVEE -> {
            container(Color(0xFF050308), Color.White.copy(alpha = 0.4f))
            val cx = size.width * 0.5f; val cy = size.height * 0.4f
            val w = f(0.62f); val h = f(0.52f)
            val cyan = Color(0xFF3BE8F0); val pink = Color(0xFFFF4FA8)
            drawPath(heart(cx - f(0.025f), cy, w, h), cyan, style = Stroke(f(0.05f), join = StrokeJoin.Round))
            drawPath(heart(cx + f(0.025f), cy, w, h), pink, style = Stroke(f(0.05f), join = StrokeJoin.Round))
            drawPath(heart(cx, cy, w, h), Color.White, style = Stroke(f(0.03f), join = StrokeJoin.Round))
            drawLine(Color.White, p(0.5f, 0.3f), p(0.46f, 0.44f), f(0.025f), StrokeCap.Round)
            text("NV", 0.5f, 0.86f, 0.2f, Color.White)
        }

        HudIconType.LUCIMASTER -> {
            container(Paper)
            val apple = Path().apply {
                moveTo(f(0.5f), f(0.3f))
                cubicTo(f(0.34f), f(0.18f), f(0.1f), f(0.3f), f(0.14f), f(0.58f))
                cubicTo(f(0.18f), f(0.84f), f(0.4f), f(0.94f), f(0.5f), f(0.88f))
                cubicTo(f(0.6f), f(0.94f), f(0.82f), f(0.84f), f(0.86f), f(0.58f))
                cubicTo(f(0.9f), f(0.3f), f(0.66f), f(0.18f), f(0.5f), f(0.3f))
                close()
            }
            drawPath(apple, Color(0xFFE5182B))
            drawLine(Color(0xFF6B3A1E), p(0.5f, 0.3f), p(0.54f, 0.14f), f(0.035f), StrokeCap.Round)
            val leaf = Path().apply {
                moveTo(f(0.54f), f(0.18f)); cubicTo(f(0.6f), f(0.06f), f(0.78f), f(0.08f), f(0.8f), f(0.12f))
                cubicTo(f(0.74f), f(0.22f), f(0.62f), f(0.24f), f(0.54f), f(0.18f)); close()
            }
            drawPath(leaf, Color(0xFF3FBF3A))
            text("lm", 0.5f, 0.68f, 0.3f, Paper, Typeface.BOLD_ITALIC)
        }

        HudIconType.DIARY -> {
            container(Color(0xFFFFF4D9), Color(0xFFD9C79A))
            for (i in 0..4) drawLine(Color(0xFFD9C79A), p(0.14f, 0.3f + i * 0.13f), p(0.86f, 0.3f + i * 0.13f), f(0.01f))
            val feather = Path().apply {
                moveTo(f(0.2f), f(0.84f))
                cubicTo(f(0.1f), f(0.5f), f(0.5f), f(0.1f), f(0.86f), f(0.12f))
                cubicTo(f(0.84f), f(0.5f), f(0.5f), f(0.8f), f(0.2f), f(0.84f))
                close()
            }
            drawPath(feather, Ink)
            drawLine(Color(0xFFFFF4D9), p(0.2f, 0.84f), p(0.64f, 0.34f), f(0.015f), StrokeCap.Round)
            val sig = Path().apply {
                moveTo(f(0.4f), f(0.88f)); cubicTo(f(0.5f), f(0.78f), f(0.56f), f(0.94f), f(0.64f), f(0.84f))
                cubicTo(f(0.7f), f(0.78f), f(0.76f), f(0.9f), f(0.84f), f(0.82f))
            }
            drawPath(sig, Ink, style = Stroke(f(0.015f), cap = StrokeCap.Round))
        }

        HudIconType.VOXTUBE -> Unit // drawn from the bitmap resource in HudIconRenderer

        HudIconType.BEE_EATS -> {
            container(Brush.verticalGradient(listOf(Color(0xFFFFB84D), Color(0xFFFF6B8A))))
            text("Bee", 0.5f, 0.42f, 0.27f, Color.White)
            drawPath(heart(size.width * 0.5f, size.height * 0.5f, f(0.2f), f(0.2f)), Color.White)
            text("Eats", 0.5f, 0.82f, 0.27f, Color(0xFF4FD9E8))
        }
    }
}

private fun DrawScope.sparkle(c: Offset, r: Float, color: Color) {
    val path = Path().apply {
        moveTo(c.x, c.y - r)
        quadraticTo(c.x, c.y, c.x + r, c.y)
        quadraticTo(c.x, c.y, c.x, c.y + r)
        quadraticTo(c.x, c.y, c.x - r, c.y)
        quadraticTo(c.x, c.y, c.x, c.y - r)
        close()
    }
    drawPath(path, color)
}

@Preview(widthDp = 96, heightDp = 96)
@Composable
private fun HudIconRendererPreview() {
    HudIconRenderer(HudIconType.CAMERA, Modifier.size(96.dp))
}
