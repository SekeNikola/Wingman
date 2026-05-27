package com.wingman.launcher.ui.effects

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

/**
 * Draws a pseudo-random noise grain pattern over composable content.
 * Pattern regenerates every 3 draw frames to avoid per-frame cost while
 * still producing visible grain animation.
 *
 * At intensity 0.0 nothing is drawn.
 */
fun Modifier.noiseGrain(
    intensity: Float = 0.04f
): Modifier = composed {
    var frameCount by remember { mutableIntStateOf(0) }
    var grainSeed by remember { mutableIntStateOf(0) }

    this.drawWithContent {
        drawContent()
        if (intensity <= 0f) return@drawWithContent

        // Regenerate seed every 3 frames
        frameCount++
        if (frameCount % 3 == 0) grainSeed = Random.nextInt()

        val rng = Random(grainSeed)
        val grainAlpha = (intensity * 0.5f).coerceIn(0f, 1f)
        val dotCount = ((size.width * size.height) / 800f).toInt().coerceAtMost(2000)

        repeat(dotCount) {
            val x = rng.nextFloat() * size.width
            val y = rng.nextFloat() * size.height
            val brightness = rng.nextFloat()
            drawRect(
                color = Color(brightness, brightness, brightness, grainAlpha),
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(1f, 1f)
            )
        }
    }
}
