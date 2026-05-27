package com.wingman.launcher.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.wingman.launcher.ui.theme.AMBER_PRIMARY

/**
 * Full-width amber highlight bar with 45° diagonal stripe texture.
 * Stripes at 3px pitch, ~10% opacity — applied as a drawBehind to avoid
 * clipping the row content.
 */
@Composable
fun ActiveRowHighlight(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                // Solid amber fill
                drawRect(color = AMBER_PRIMARY)

                // Diagonal stripe texture — 45° lines every 6px, 10% opacity
                val stripeColor = Color(1f, 1f, 1f, 0.10f)
                val pitch = 6f
                val total = size.width + size.height
                var offset = 0f
                while (offset < total) {
                    drawLine(
                        color = stripeColor,
                        start = Offset(offset, 0f),
                        end = Offset(offset - size.height, size.height),
                        strokeWidth = 2f
                    )
                    offset += pitch
                }
            }
    )
}
