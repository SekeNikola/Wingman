package com.wingman.launcher.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.wingman.launcher.util.WingmanConstants

/**
 * Tiles a pre-baked 64x64 noise bitmap across the full composable area.
 * The bitmap is generated once with [remember] — no per-frame allocations.
 * [intensity] 0f = invisible, 1f = full noise alpha (~0.12 max, very subtle).
 */
@Composable
fun NoiseOverlay(
    intensity: Float,
    modifier: Modifier = Modifier
) {
    if (intensity <= 0f) return

    val noiseBitmap = remember { generateNoiseBitmap(WingmanConstants.NOISE_BITMAP_SIZE_PX) }
    val alpha = (intensity * 0.12f).coerceIn(0f, 0.12f)

    Canvas(modifier = modifier.fillMaxSize()) {
        val bW = noiseBitmap.width
        val bH = noiseBitmap.height
        val srcOffset = IntOffset.Zero
        val srcSize   = IntSize(bW, bH)

        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                this.alpha = alpha
            }
            var y = 0
            while (y < size.height.toInt()) {
                var x = 0
                while (x < size.width.toInt()) {
                    canvas.drawImageRect(
                        image     = noiseBitmap,
                        srcOffset = srcOffset,
                        srcSize   = srcSize,
                        dstOffset = IntOffset(x, y),
                        dstSize   = srcSize,
                        paint     = paint
                    )
                    x += bW
                }
                y += bH
            }
        }
    }
}

/**
 * Generates a [size]x[size] grey-noise ImageBitmap.
 * Each pixel is a random grey value. Seeded deterministically so the bitmap
 * is always the same (no re-generation surprises).
 */
private fun generateNoiseBitmap(size: Int): ImageBitmap {
    val pixels = IntArray(size * size)
    val rng = java.util.Random(0xDEADBEEFL)
    for (i in pixels.indices) {
        val grey = rng.nextInt(256)
        pixels[i] = android.graphics.Color.argb(255, grey, grey, grey)
    }
    val bmp = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    bmp.setPixels(pixels, 0, size, 0, 0, size, size)
    return bmp.asImageBitmap()
}
