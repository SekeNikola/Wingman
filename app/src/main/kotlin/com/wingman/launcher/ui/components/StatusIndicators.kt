package com.wingman.launcher.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wingman.launcher.ui.theme.LocalWingmanColors

/**
 * Pixel-art battery icon drawn entirely with Canvas.
 *
 * @param batteryPercent 0–100
 * @param isCharging     whether to draw a small "+" symbol in fill
 */
@Composable
fun BatteryIcon(
    batteryPercent: Int,
    isCharging: Boolean,
    color: Color = LocalWingmanColors.current.primary,
    sizeDp: Dp = 20.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = sizeDp.toPx()
        val h = w * 0.55f
        val tipW = w * 0.08f
        val tipH = h * 0.4f
        val stroke = w * 0.07f

        // battery body outline
        drawRect(color, topLeft = Offset(0f, 0f), size = Size(w - tipW, h), style = Stroke(stroke))
        // battery tip
        drawRect(
            color,
            topLeft = Offset(w - tipW, h * 0.3f),
            size = Size(tipW, tipH)
        )
        // fill bar
        val fillMax = (w - tipW) - stroke * 2f
        val fillW = fillMax * (batteryPercent / 100f).coerceIn(0f, 1f)
        if (fillW > 0f) {
            drawRect(
                color = color,
                topLeft = Offset(stroke, stroke),
                size = Size(fillW, h - stroke * 2f)
            )
        }
        // charging indicator
        if (isCharging) {
            val cx = (w - tipW) / 2f
            val cy = h / 2f
            val arm = h * 0.22f
            drawLine(Color.Black, Offset(cx, cy - arm), Offset(cx, cy + arm), strokeWidth = stroke)
            drawLine(Color.Black, Offset(cx - arm * 0.6f, cy), Offset(cx + arm * 0.6f, cy), strokeWidth = stroke)
        }
    }
}

/**
 * Pixel-art signal bars drawn with Canvas.
 *
 * @param bars 0–4
 */
@Composable
fun SignalBarsIcon(
    bars: Int,
    color: Color = LocalWingmanColors.current.primary,
    dimColor: Color = LocalWingmanColors.current.dimText,
    sizeDp: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = sizeDp.toPx()
        val h = w
        val barCount = 4
        val barW = w * 0.16f
        val gap = w * 0.06f
        val totalW = barCount * barW + (barCount - 1) * gap
        val startX = (w - totalW) / 2f

        for (i in 0 until barCount) {
            val barH = h * 0.25f * (i + 1)
            val x = startX + i * (barW + gap)
            val y = h - barH
            val c = if (i < bars) color else dimColor
            drawRect(c, topLeft = Offset(x, y), size = Size(barW, barH))
        }
    }
}
