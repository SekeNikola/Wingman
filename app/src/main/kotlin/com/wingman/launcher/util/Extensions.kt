package com.wingman.launcher.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/**
 * Utility extension functions used across the Wingman project.
 */

fun Drawable.toImageBitmap(): ImageBitmap {
    val bmp = if (this is BitmapDrawable && bitmap != null) {
        bitmap
    } else {
        val w = intrinsicWidth.coerceAtLeast(1)
        val h = intrinsicHeight.coerceAtLeast(1)
        Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).also { b ->
            val c = Canvas(b)
            setBounds(0, 0, c.width, c.height)
            draw(c)
        }
    }
    return bmp.asImageBitmap()
}

/**
 * Formats an integer number of seconds as "MM:SS" for display in the music player.
 * Example: 187 -> "3:07"
 */
fun Int.toTimeString(): String {
    val minutes = this / 60
    val seconds = this % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

/**
 * Clamps a Float value to the range [min, max].
 * Convenience alias for coerceIn().
 */
fun Float.clampTo(min: Float, max: Float): Float = coerceIn(min, max)

/**
 * Returns a progress fraction (0f..1f) for a given current/total pair.
 * Guards against division by zero.
 */
fun progressFraction(current: Int, total: Int): Float =
    if (total <= 0) 0f else (current.toFloat() / total.toFloat()).coerceIn(0f, 1f)
