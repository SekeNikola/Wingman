package com.wingman.launcher.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wingman.launcher.data.model.MenuSection

/**
 * Canvas-drawn pixel icons — one distinct icon per MenuSection.
 * No vector drawables. Renders cleanly at 16dp and 24dp.
 *
 * SCANS    : document with corner fold
 * ORGANIZER: checklist (3 bars + left tick on top)
 * TUTORIALS: stack of 3 pages
 * MUSIC    : eighth note
 * SETTINGS : wrench silhouette
 */
@Composable
fun PixelIcon(
    section: MenuSection,
    color: Color,
    size: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier,
        contentDescription = section.label
    ) {
        val s = size.toPx()
        val sw = (s / 8f).coerceAtLeast(1f) // stroke width scales with icon size

        when (section) {
            MenuSection.SCANS -> drawDocumentIcon(s, sw, color)
            MenuSection.ORGANIZER -> drawChecklistIcon(s, sw, color)
            MenuSection.TUTORIALS -> drawStackIcon(s, sw, color)
            MenuSection.MUSIC -> drawMusicNoteIcon(s, sw, color)
            MenuSection.SETTINGS -> drawWrenchIcon(s, sw, color)
        }
    }
}

// SCANS: document with top-right corner fold
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDocumentIcon(
    s: Float, sw: Float, color: Color
) {
    val foldSize = s * 0.25f
    val left = s * 0.1f
    val top = s * 0.05f
    val right = s * 0.9f
    val bottom = s * 0.95f

    // Outer document outline (with fold notch)
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(left, top)
        lineTo(right - foldSize, top)
        lineTo(right, top + foldSize)
        lineTo(right, bottom)
        lineTo(left, bottom)
        close()
    }
    drawPath(path, color = color, style = androidx.compose.ui.graphics.drawscope.Stroke(width = sw))

    // Corner fold triangle
    val foldPath = androidx.compose.ui.graphics.Path().apply {
        moveTo(right - foldSize, top)
        lineTo(right - foldSize, top + foldSize)
        lineTo(right, top + foldSize)
    }
    drawPath(foldPath, color = color, style = androidx.compose.ui.graphics.drawscope.Stroke(width = sw))

    // 3 content lines
    val lineLeft = left + s * 0.15f
    val lineRight = right - s * 0.15f
    val lineY1 = top + s * 0.42f
    val lineY2 = top + s * 0.58f
    val lineY3 = top + s * 0.74f
    drawLine(color, Offset(lineLeft, lineY1), Offset(lineRight - foldSize * 0.5f, lineY1), sw)
    drawLine(color, Offset(lineLeft, lineY2), Offset(lineRight, lineY2), sw)
    drawLine(color, Offset(lineLeft, lineY3), Offset(lineRight, lineY3), sw)
}

// ORGANIZER: 3 horizontal bars with a tick/check on the top-left
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawChecklistIcon(
    s: Float, sw: Float, color: Color
) {
    val left = s * 0.08f
    val right = s * 0.92f
    val barLeft = s * 0.35f
    val rows = listOf(s * 0.2f, s * 0.5f, s * 0.8f)
    val boxSize = s * 0.18f

    rows.forEachIndexed { idx, y ->
        // Small checkbox outline
        drawRect(
            color = color,
            topLeft = Offset(left, y - boxSize / 2),
            size = Size(boxSize, boxSize),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = sw)
        )
        // Tick on first row only
        if (idx == 0) {
            val tx = left + boxSize * 0.15f
            val ty = y - boxSize * 0.05f
            drawLine(color, Offset(tx, ty), Offset(left + boxSize * 0.45f, y + boxSize * 0.35f), sw, StrokeCap.Round)
            drawLine(color, Offset(left + boxSize * 0.45f, y + boxSize * 0.35f), Offset(left + boxSize * 0.9f, y - boxSize * 0.3f), sw, StrokeCap.Round)
        }
        // Label bar next to checkbox
        drawLine(color, Offset(barLeft, y), Offset(right, y), sw)
    }
}

// TUTORIALS: stack of 3 overlapping pages
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStackIcon(
    s: Float, sw: Float, color: Color
) {
    val offsets = listOf(s * 0.15f, s * 0.08f, 0f)
    val pageW = s * 0.75f
    val pageH = s * 0.55f
    val baseX = s * 0.12f
    val baseY = s * 0.25f

    offsets.forEachIndexed { i, off ->
        val alpha = if (i == 0) 0.4f else if (i == 1) 0.65f else 1.0f
        drawRect(
            color = color.copy(alpha = alpha),
            topLeft = Offset(baseX + off, baseY - off * 0.5f),
            size = Size(pageW, pageH),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = sw)
        )
    }
    // 2 lines on front page
    val px = baseX + s * 0.08f
    val py = baseY + pageH * 0.35f
    drawLine(color, Offset(px, py), Offset(px + pageW * 0.65f, py), sw)
    drawLine(color, Offset(px, py + pageH * 0.22f), Offset(px + pageW * 0.45f, py + pageH * 0.22f), sw)
}

// MUSIC: eighth note (stem + filled oval head)
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMusicNoteIcon(
    s: Float, sw: Float, color: Color
) {
    val headR = s * 0.14f
    val headCx = s * 0.35f
    val headCy = s * 0.72f
    val stemX = headCx + headR * 0.85f
    val stemTop = headCy - s * 0.52f
    val flagEndX = stemX + s * 0.28f
    val flagEndY = stemTop + s * 0.18f

    // Note head (filled oval, slightly tilted via rect)
    drawOval(
        color = color,
        topLeft = Offset(headCx - headR * 1.3f, headCy - headR * 0.85f),
        size = Size(headR * 2.6f, headR * 1.7f)
    )
    // Stem
    drawLine(color, Offset(stemX, headCy - headR * 0.4f), Offset(stemX, stemTop), sw * 1.2f)
    // Flag
    drawLine(color, Offset(stemX, stemTop), Offset(flagEndX, flagEndY), sw * 1.2f, StrokeCap.Round)
    drawLine(color, Offset(flagEndX, flagEndY), Offset(stemX, stemTop + s * 0.28f), sw * 1.2f, StrokeCap.Round)
}

// SETTINGS: simple wrench silhouette (head circle + handle)
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWrenchIcon(
    s: Float, sw: Float, color: Color
) {
    val headCx = s * 0.35f
    val headCy = s * 0.28f
    val headR = s * 0.2f

    // Wrench head (circle)
    drawCircle(
        color = color,
        radius = headR,
        center = Offset(headCx, headCy),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = sw)
    )

    // Jaw notch lines on head
    drawLine(color, Offset(headCx - headR, headCy), Offset(headCx - headR * 0.3f, headCy), sw)
    drawLine(color, Offset(headCx + headR * 0.3f, headCy), Offset(headCx + headR, headCy), sw)

    // Handle — angled down-right
    val handleStartX = headCx + headR * 0.7f
    val handleStartY = headCy + headR * 0.7f
    val handleEndX = s * 0.88f
    val handleEndY = s * 0.9f
    drawLine(color, Offset(handleStartX, handleStartY), Offset(handleEndX, handleEndY), sw * 2.5f, StrokeCap.Round)
}
