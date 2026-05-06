package com.wingman.launcher.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wingman.launcher.ui.theme.LocalWingmanColors
import com.wingman.launcher.util.WingmanConstants

/**
 * Wraps [content] in a Box with an orange glow drawn behind it using layered
 * semi-transparent drawRoundRect calls. This approach works correctly on all API
 * levels with hardware acceleration (setShadowLayer has no effect on
 * hardware-accelerated canvas).
 *
 * When [isActive] is false the glow is invisible.
 * [intensity] scales the glow alpha (0f = off, 1f = full glow).
 */
@Composable
fun GlowBox(
    isActive: Boolean,
    intensity: Float,
    modifier: Modifier = Modifier,
    glowColor: Color = LocalWingmanColors.current.glowOrange,
    glowRadius: Dp = WingmanConstants.GLOW_SPREAD_DP.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val baseAlpha = if (isActive) intensity.coerceIn(0f, 1f) else 0f

    Box(
        modifier = modifier.drawBehind {
            if (baseAlpha <= 0f) return@drawBehind

            val radiusPx = glowRadius.toPx()

            // Each layer expands outward with a decreasing alpha value.
            // Layering multiple translucent rects simulates a soft glow bloom.
            val layers = listOf(
                Pair(radiusPx * 0.25f, 0.15f),
                Pair(radiusPx * 0.50f, 0.10f),
                Pair(radiusPx * 0.75f, 0.06f),
                Pair(radiusPx * 1.00f, 0.03f)
            )

            for ((outset, layerAlpha) in layers) {
                drawRoundRect(
                    color = glowColor.copy(alpha = layerAlpha * baseAlpha),
                    topLeft = Offset(-outset, -outset),
                    size = Size(size.width + outset * 2f, size.height + outset * 2f),
                    cornerRadius = CornerRadius(outset, outset)
                )
            }
        },
        content = content
    )
}
