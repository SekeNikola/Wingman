package com.wingman.launcher.ui.effects

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Draws repeating horizontal scanlines over composable content.
 * Lines at [lineSpacing] pitch, semi-transparent black at [alpha].
 * No Bitmap — pure Canvas drawLine on each frame.
 *
 * At intensity 0.0 nothing is drawn.
 */
fun Modifier.scanlines(
    alpha: Float = 0.15f,
    lineSpacing: Dp = 2.dp
): Modifier = this.drawWithContent {
    drawContent()
    if (alpha <= 0f) return@drawWithContent
    val pitch = lineSpacing.toPx()
    val lineColor = Color(0f, 0f, 0f, alpha.coerceIn(0f, 1f))
    var y = 0f
    while (y < size.height) {
        drawLine(
            color = lineColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f
        )
        y += pitch
    }
}
