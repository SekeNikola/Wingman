package com.wingman.launcher.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.wingman.launcher.ui.theme.BG_TOPBAR
import com.wingman.launcher.ui.theme.GREEN_DIM
import com.wingman.launcher.ui.theme.GREEN_MUTED
import com.wingman.launcher.ui.theme.TerminalSmall
import com.wingman.launcher.ui.theme.WHITE_PIXEL
import androidx.compose.foundation.layout.Column

/**
 * Top bar: username left-aligned, signal bars + pixel battery right-aligned.
 * Height: 32dp. No Material TopAppBar.
 * Thin 1dp GREEN_DIM separator line below.
 */
@Composable
fun TopBar(
    username: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .background(BG_TOPBAR)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Username — left-aligned
                TerminalText(
                    text = username,
                    style = TerminalSmall,
                    color = WHITE_PIXEL,
                    modifier = Modifier.weight(1f)
                )

                // Signal bars (3 bars, pixel style)
                SignalBars(
                    color = GREEN_MUTED,
                    modifier = Modifier.size(width = 18.dp, height = 14.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Battery icon (pixel style) + percentage
                BatteryIcon(
                    percent = 87,
                    color = GREEN_MUTED,
                    modifier = Modifier.size(width = 24.dp, height = 14.dp)
                )
            }
        }

        // Separator line
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
        ) {
            drawLine(
                color = GREEN_DIM,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 1f
            )
        }
    }
}

// Three ascending pixel bars — classic signal indicator
@Composable
private fun SignalBars(color: Color, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier,
        contentDescription = "SIGNAL"
    ) {
        val barCount = 3
        val barWidth = size.width / (barCount * 2 - 1)
        val maxHeight = size.height

        for (i in 0 until barCount) {
            val barHeight = maxHeight * ((i + 1).toFloat() / barCount)
            val x = i * barWidth * 2f
            val top = size.height - barHeight
            drawRect(
                color = color.copy(alpha = if (i < 2) 1f else 0.6f), // dim last bar slightly for realism
                topLeft = Offset(x, top),
                size = Size(barWidth * 0.85f, barHeight)
            )
        }
    }
}

// Pixel battery outline with fill level
@Composable
private fun BatteryIcon(percent: Int, color: Color, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier,
        contentDescription = "BATTERY $percent%"
    ) {
        val bodyW = size.width * 0.82f
        val bodyH = size.height
        val tipW = size.width * 0.1f
        val tipH = bodyH * 0.45f
        val sw = 1.5f

        // Battery body outline
        drawRect(
            color = color,
            topLeft = Offset(0f, 0f),
            size = Size(bodyW, bodyH),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = sw)
        )

        // Battery tip (nub)
        drawRect(
            color = color,
            topLeft = Offset(bodyW, (bodyH - tipH) / 2),
            size = Size(tipW, tipH)
        )

        // Fill level
        val fillW = (bodyW - sw * 2) * (percent / 100f)
        val fillColor = when {
            percent > 50 -> color
            percent > 20 -> color.copy(alpha = 0.8f)
            else -> Color(0xFFCC3300)
        }
        if (fillW > 0f) {
            drawRect(
                color = fillColor,
                topLeft = Offset(sw, sw),
                size = Size(fillW, bodyH - sw * 2)
            )
        }
    }
}
