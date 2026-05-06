package com.wingman.launcher.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.wingman.launcher.data.model.PixelIconType
import com.wingman.launcher.ui.navigation.AppDestination
import com.wingman.launcher.ui.theme.WingmanTheme

private val ChromeBg     = Color(0xFF060B06)
private val ChromeDark   = Color(0xFF080D08)
private val AmberOn      = Color(0xFFD4821A)
private val AmberDim     = Color(0xFF3A1E08)
private val LedRed       = Color(0xFFCC2222)
private val LedGlow      = Color(0xFFFF5555)
private val CassetteShell = Color(0xFF1A2A1A)
private val CassetteRim  = Color(0xFF2D3D2D)

/**
 * Device chrome section rendered below the main menu.
 * Contains: tape counter row, cassette deck visualization, quick-nav dock.
 */
@Composable
fun CassetteChrome(
    effectIntensity: Float,
    counter: Int,
    onNavigate: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = WingmanTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ChromeBg)
    ) {
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.secondary.copy(alpha = 0.35f))
        )

        TapeCounterRow(counter = counter, effectIntensity = effectIntensity)

        CassetteVisualization(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 12.dp)
        )

        Spacer(Modifier.height(6.dp))

        Spacer(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.primary.copy(alpha = 0.45f))
        )

        BottomDock(onNavigate = onNavigate)
    }
}

@Composable
private fun TapeCounterRow(counter: Int, effectIntensity: Float) {
    val typo = WingmanTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            WingmanText(text = "<<", style = typo.caption, color = AmberOn.copy(alpha = 0.6f))

            SevenSegmentDisplay(
                value = counter,
                digits = 5,
                modifier = Modifier
                    .width(72.dp)
                    .height(24.dp)
            )

            WingmanText(text = ">>", style = typo.caption, color = AmberOn.copy(alpha = 0.6f))
        }

        // Red LED indicator
        Canvas(modifier = Modifier.size(14.dp)) {
            val r = size.minDimension / 2f
            val c = Offset(r, r)
            if (effectIntensity > 0f) {
                drawCircle(LedGlow.copy(alpha = 0.28f * effectIntensity), r * 1.7f, c)
                drawCircle(LedGlow.copy(alpha = 0.14f * effectIntensity), r * 2.2f, c)
            }
            drawCircle(LedRed, r, c)
            drawCircle(Color.White.copy(alpha = 0.22f), r * 0.42f, c - Offset(r * 0.22f, r * 0.22f))
        }
    }
}

@Composable
private fun SevenSegmentDisplay(value: Int, digits: Int, modifier: Modifier = Modifier) {
    val str = value.toString().padStart(digits, '0').takeLast(digits)

    Box(
        modifier = modifier
            .background(Color(0xFF080800))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            str.forEach { ch ->
                val digit = ch.digitToIntOrNull() ?: 0
                Canvas(
                    modifier = Modifier
                        .width(11.dp)
                        .fillMaxHeight()
                ) {
                    drawSevenSegmentDigit(digit, size.width, size.height, AmberOn, AmberDim)
                }
            }
        }
    }
}

private fun DrawScope.drawSevenSegmentDigit(
    digit: Int,
    w: Float,
    h: Float,
    on: Color,
    off: Color
) {
    val segments = booleanArrayOf(
        digit in listOf(0, 2, 3, 5, 6, 7, 8, 9),   // a: top
        digit in listOf(0, 1, 2, 3, 4, 7, 8, 9),    // b: upper-right
        digit in listOf(0, 1, 3, 4, 5, 6, 7, 8, 9), // c: lower-right
        digit in listOf(0, 2, 3, 5, 6, 8, 9),       // d: bottom
        digit in listOf(0, 2, 6, 8),                 // e: lower-left
        digit in listOf(0, 4, 5, 6, 8, 9),           // f: upper-left
        digit in listOf(2, 3, 4, 5, 6, 8, 9)        // g: middle
    )

    val sw = (w * 0.18f).coerceAtLeast(1f)
    val half = h / 2f
    val gap = sw * 0.28f

    fun c(i: Int) = if (segments[i]) on else off

    // a top
    drawRect(c(0), Offset(sw + gap, 0f),          Size(w - 2*(sw+gap), sw))
    // b upper-right
    drawRect(c(1), Offset(w - sw, sw + gap),       Size(sw, half - sw - 2*gap))
    // c lower-right
    drawRect(c(2), Offset(w - sw, half + gap),     Size(sw, half - sw - 2*gap))
    // d bottom
    drawRect(c(3), Offset(sw + gap, h - sw),       Size(w - 2*(sw+gap), sw))
    // e lower-left
    drawRect(c(4), Offset(0f, half + gap),         Size(sw, half - sw - 2*gap))
    // f upper-left
    drawRect(c(5), Offset(0f, sw + gap),           Size(sw, half - sw - 2*gap))
    // g middle
    drawRect(c(6), Offset(sw + gap, half - sw/2f), Size(w - 2*(sw+gap), sw))
}

@Composable
private fun CassetteVisualization(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Outer shell
        drawRoundRect(
            color = CassetteShell,
            topLeft = Offset(0f, 0f),
            size = Size(w, h),
            cornerRadius = CornerRadius(10f)
        )
        drawRoundRect(
            color = CassetteRim,
            topLeft = Offset(0f, 0f),
            size = Size(w, h),
            cornerRadius = CornerRadius(10f),
            style = Stroke(width = 1.5f)
        )

        // Top notches (cassette write-protect tabs)
        drawRect(Color(0xFF0A150A), Offset(10f, 0f), Size(14f, 7f))
        drawRect(Color(0xFF0A150A), Offset(w - 24f, 0f), Size(14f, 7f))

        // Tape window (center aperture)
        val winW = w * 0.46f
        val winH = h * 0.40f
        val winX = (w - winW) / 2f
        val winY = (h - winH) / 2f - 3f
        drawRoundRect(
            color = Color(0xFF040604),
            topLeft = Offset(winX, winY),
            size = Size(winW, winH),
            cornerRadius = CornerRadius(5f)
        )
        drawRoundRect(
            color = AmberOn.copy(alpha = 0.25f),
            topLeft = Offset(winX, winY),
            size = Size(winW, winH),
            cornerRadius = CornerRadius(5f),
            style = Stroke(width = 1f)
        )

        // Tape line across window bottom
        val tapeY = winY + winH * 0.72f
        drawLine(AmberOn.copy(alpha = 0.45f), Offset(winX + 2f, tapeY), Offset(winX + winW - 2f, tapeY), 1.8f)

        // Left reel (partially inside window)
        val reelR = h * 0.22f
        val lCx = winX - reelR * 0.05f
        val rCy = winY + winH / 2f
        drawReel(lCx, rCy, reelR)

        // Right reel
        val rCx = winX + winW + reelR * 0.05f
        drawReel(rCx, rCy, reelR)

        // Hub screw dots (center of housing)
        val screwY = h - 10f
        listOf(w * 0.3f, w * 0.5f, w * 0.7f).forEach { sx ->
            drawCircle(AmberOn.copy(alpha = 0.3f), 2.5f, Offset(sx, screwY))
        }

        // Label area (amber strip across center)
        drawRoundRect(
            color = AmberOn.copy(alpha = 0.08f),
            topLeft = Offset(winX + winW + reelR * 0.05f + reelR * 0.9f + 4f, winY),
            size = Size(w - (winX + winW + reelR * 0.05f + reelR * 0.9f + 4f) - 8f, winH),
            cornerRadius = CornerRadius(3f)
        )
        drawRoundRect(
            color = AmberOn.copy(alpha = 0.08f),
            topLeft = Offset(8f, winY),
            size = Size((winX - reelR * 0.9f - 4f - 8f).coerceAtLeast(0f), winH),
            cornerRadius = CornerRadius(3f)
        )
    }
}

private fun DrawScope.drawReel(cx: Float, cy: Float, r: Float) {
    // Outer ring
    drawCircle(AmberOn.copy(alpha = 0.40f), r, Offset(cx, cy), style = Stroke(1.5f))
    // Tyre (slightly inset)
    drawCircle(AmberOn.copy(alpha = 0.15f), r * 0.85f, Offset(cx, cy), style = Stroke(2.5f))
    // Hub disc
    drawCircle(AmberOn.copy(alpha = 0.55f), r * 0.30f, Offset(cx, cy))
    // 3 spokes
    for (i in 0..2) {
        val rad = Math.toRadians(i * 120.0)
        val cos = Math.cos(rad).toFloat()
        val sin = Math.sin(rad).toFloat()
        drawLine(
            AmberOn.copy(alpha = 0.38f),
            Offset(cx + cos * r * 0.30f, cy + sin * r * 0.30f),
            Offset(cx + cos * r * 0.82f, cy + sin * r * 0.82f),
            strokeWidth = 1.5f
        )
    }
}

@Composable
private fun BottomDock(onNavigate: (AppDestination) -> Unit) {
    val colors = WingmanTheme.colors
    val typo   = WingmanTheme.typography

    data class DockItem(val label: String, val icon: PixelIconType, val dest: AppDestination)

    val items = listOf(
        DockItem("SCANS", PixelIconType.RADAR,      AppDestination.Scans),
        DockItem("ORG",   PixelIconType.FOLDER,     AppDestination.Organizer),
        DockItem("MUSIC", PixelIconType.MUSIC_NOTE, AppDestination.Music),
        DockItem("SYS",   PixelIconType.GEAR,       AppDestination.Settings)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(ChromeDark),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { onNavigate(item.dest) }
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                PixelIcon(
                    type    = item.icon,
                    tint    = colors.secondary,
                    sizeDp  = 15.dp,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.height(3.dp))
                WingmanText(
                    text  = item.label,
                    style = typo.caption,
                    color = colors.dimText
                )
            }
        }
    }
}
