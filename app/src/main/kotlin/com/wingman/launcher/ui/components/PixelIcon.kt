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
import com.wingman.launcher.data.model.PixelIconType

/**
 * Draws a pixel-art icon using Canvas — no bitmaps or vector drawables.
 * Each [PixelIconType] is drawn procedurally in a pixel-grid style.
 */
@Composable
fun PixelIcon(
    type: PixelIconType,
    tint: Color,
    sizeDp: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
    ) {
        val s = sizeDp.toPx()
        when (type) {
            PixelIconType.RADAR      -> drawRadar(s, tint)
            PixelIconType.FOLDER     -> drawFolder(s, tint)
            PixelIconType.BOOK       -> drawBook(s, tint)
            PixelIconType.MUSIC_NOTE -> drawMusicNote(s, tint)
            PixelIconType.GEAR       -> drawGear(s, tint)
            PixelIconType.GRID       -> drawGrid(s, tint)
            PixelIconType.CAMERA     -> drawCamera(s, tint)
        }
    }
}

// ── Icon drawing functions ────────────────────────────────────────────────────

private fun DrawScope.drawRadar(s: Float, tint: Color) {
    val cx = s / 2f
    val cy = s / 2f
    val stroke = Stroke(width = s * 0.1f)
    // concentric arcs to suggest radar sweep
    drawCircle(tint, radius = s * 0.45f, center = Offset(cx, cy), style = stroke)
    drawCircle(tint, radius = s * 0.28f, center = Offset(cx, cy), style = stroke)
    drawCircle(tint, radius = s * 0.12f, center = Offset(cx, cy))
    // sweep line
    drawLine(tint, Offset(cx, cy), Offset(cx + s * 0.42f, cy - s * 0.2f), strokeWidth = s * 0.1f)
}

private fun DrawScope.drawFolder(s: Float, tint: Color) {
    val p = s * 0.08f
    // tab on top-left
    drawRect(
        color = tint,
        topLeft = Offset(p, p * 2f),
        size = Size(s * 0.35f, s * 0.15f)
    )
    // main folder body
    drawRect(
        color = tint,
        topLeft = Offset(p, p * 2f + s * 0.13f),
        size = Size(s - p * 2f, s * 0.6f)
    )
    // inner cutout (dark, so use background "punch")
    drawRect(
        color = Color.Transparent,
        topLeft = Offset(p * 2.5f, p * 2f + s * 0.23f),
        size = Size(s - p * 5f, s * 0.4f)
    )
}

private fun DrawScope.drawBook(s: Float, tint: Color) {
    val p = s * 0.08f
    val stroke = Stroke(width = s * 0.08f)
    // book body
    drawRect(
        color = tint,
        topLeft = Offset(s * 0.2f, p),
        size = Size(s * 0.65f, s - p * 2f)
    )
    // spine
    drawLine(tint, Offset(s * 0.22f, p), Offset(s * 0.22f, s - p), strokeWidth = s * 0.12f)
    // lines on pages
    for (i in 1..3) {
        val y = p + (s - p * 2f) * i / 4f
        drawLine(Color.Transparent, Offset(s * 0.32f, y), Offset(s * 0.78f, y), strokeWidth = s * 0.07f)
    }
}

private fun DrawScope.drawMusicNote(s: Float, tint: Color) {
    val r = s * 0.18f
    // note head
    drawOval(tint, topLeft = Offset(s * 0.1f, s * 0.6f), size = Size(r * 2f, r * 1.4f))
    // stem
    drawLine(tint, Offset(s * 0.46f, s * 0.66f), Offset(s * 0.46f, s * 0.18f), strokeWidth = s * 0.09f)
    // flag
    drawLine(tint, Offset(s * 0.46f, s * 0.18f), Offset(s * 0.76f, s * 0.32f), strokeWidth = s * 0.09f)
    drawLine(tint, Offset(s * 0.76f, s * 0.32f), Offset(s * 0.46f, s * 0.44f), strokeWidth = s * 0.09f)
}

private fun DrawScope.drawGrid(s: Float, tint: Color) {
    val p   = s * 0.08f
    val gap = s * 0.10f
    val cell = (s - p * 2f - gap) / 2f
    for (row in 0..1) for (col in 0..1) {
        drawRect(tint,
            topLeft = Offset(p + col * (cell + gap), p + row * (cell + gap)),
            size    = Size(cell, cell)
        )
    }
}

private fun DrawScope.drawCamera(s: Float, tint: Color) {
    val p = s * 0.08f
    // viewfinder bump
    drawRect(tint, topLeft = Offset(s * 0.34f, s * 0.17f), size = Size(s * 0.28f, s * 0.14f))
    // body
    drawRect(tint, topLeft = Offset(p, s * 0.29f), size = Size(s - p * 2f, s * 0.54f))
    // lens ring
    val cx = s * 0.50f; val cy = s * 0.56f
    drawCircle(Color.Black.copy(alpha = 0.85f), radius = s * 0.18f, center = Offset(cx, cy))
    drawCircle(tint, radius = s * 0.18f, center = Offset(cx, cy), style = Stroke(width = s * 0.07f))
    drawCircle(tint, radius = s * 0.07f, center = Offset(cx, cy))
}

private fun DrawScope.drawGear(s: Float, tint: Color) {
    val cx = s / 2f
    val cy = s / 2f
    val stroke = Stroke(width = s * 0.13f)
    // gear body (outer circle)
    drawCircle(tint, radius = s * 0.38f, center = Offset(cx, cy), style = stroke)
    // center hole
    drawCircle(Color.Transparent, radius = s * 0.13f, center = Offset(cx, cy))
    // teeth (4 rectangles at cardinal directions)
    val toothW = s * 0.12f
    val toothH = s * 0.16f
    val teeth = listOf(
        Offset(cx - toothW / 2f, s * 0.04f),
        Offset(cx - toothW / 2f, s - s * 0.04f - toothH),
        Offset(s * 0.04f, cy - toothW / 2f),
        Offset(s - s * 0.04f - toothH, cy - toothW / 2f)
    )
    for ((i, t) in teeth.withIndex()) {
        val w = if (i < 2) toothW else toothH
        val h = if (i < 2) toothH else toothW
        drawRect(tint, topLeft = t, size = Size(w, h))
    }
}
