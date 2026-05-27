package com.wingman.launcher.ui.effects

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Draws a blurred glow halo behind the composable — CRT phosphor glow.
 *
 * Implementation: 4 expanding semi-transparent rects with decreasing alpha
 * simulate a blur gradient. Hardware-accelerated compatible — no BlurMaskFilter.
 * Each rect is 2px wider on all sides than the previous, alpha halved each step.
 *
 * At glowAlpha 0.0 or radius 0.dp nothing is drawn.
 */
fun Modifier.terminalGlow(
    color: Color,
    radius: Dp = 8.dp,
    glowAlpha: Float = 0.4f
): Modifier = this.drawBehind {
    if (glowAlpha <= 0f || radius.value <= 0f) return@drawBehind

    val radiusPx = radius.toPx()
    val steps = 5
    val stepSize = radiusPx / steps

    // Draw concentric expanding rects with decreasing alpha to simulate blur
    for (i in 1..steps) {
        val expand = stepSize * i
        val alpha = (glowAlpha * (1f - i.toFloat() / steps)).coerceIn(0f, 1f)
        if (alpha <= 0f) continue
        drawRect(
            color = color.copy(alpha = alpha),
            topLeft = Offset(-expand, -expand),
            size = Size(
                size.width + expand * 2,
                size.height + expand * 2
            )
        )
    }
}
