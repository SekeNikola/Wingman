package com.wingman.launcher.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.BasicText

/**
 * Terminal-styled text with Press Start 2P font.
 * All text is uppercased at the composable level.
 *
 * If [flickerEnabled] = true, alpha oscillates between 0.88–1.0 over ~4 seconds
 * using an infiniteTransition — no Bitmap, no frame-by-frame recompose noise.
 */
@Composable
fun TerminalText(
    text: String,
    style: TextStyle,
    color: Color,
    flickerEnabled: Boolean = false,
    modifier: Modifier = Modifier
) {
    val alpha: Float = if (flickerEnabled) {
        val transition = rememberInfiniteTransition(label = "flicker")
        val animatedAlpha by transition.animateFloat(
            initialValue = 1.0f,
            targetValue = 0.88f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 3800,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "flickerAlpha"
        )
        animatedAlpha
    } else {
        1.0f
    }

    BasicText(
        text = text.uppercase(),
        style = style.copy(color = color.copy(alpha = color.alpha * alpha)),
        modifier = modifier,
        overflow = TextOverflow.Clip,
        softWrap = true
    )
}
