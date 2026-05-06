package com.wingman.launcher.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wingman.launcher.util.WingmanConstants

/**
 * Full-area scanline overlay drawn with Canvas.
 * Horizontal semi-transparent lines spaced [lineSpacingDp] apart simulate CRT scanlines.
 * [intensity] 0f = invisible, 1f = maximum opacity (~0.35 alpha to keep them non-distracting).
 */
@Composable
fun ScanlineOverlay(
    intensity: Float,
    lineSpacingDp: Dp = WingmanConstants.SCANLINE_SPACING_DP.dp,
    modifier: Modifier = Modifier
) {
    if (intensity <= 0f) return

    val lineAlpha = (intensity * 0.35f).coerceIn(0f, 0.35f)

    Canvas(modifier = modifier.fillMaxSize()) {
        val spacingPx = lineSpacingDp.toPx()
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = Color.Black.copy(alpha = lineAlpha),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += spacingPx
        }
    }
}
