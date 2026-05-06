package com.wingman.launcher.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontLoadingStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.wingman.launcher.R

// OptionalLocal: if press_start_2p.ttf is absent, Compose falls back to the
// system monospace font rather than crashing. Drop the font file into
// app/src/main/res/font/press_start_2p.ttf to enable the pixel font.
val PressStart2PFamily = FontFamily(
    Font(
        resId = R.font.press_start_2p,
        weight = FontWeight.Normal,
        loadingStrategy = FontLoadingStrategy.OptionalLocal
    )
)

// ── Typography scale ─────────────────────────────────────────────────────────
data class WingmanTypography(
    val heading: TextStyle = TextStyle(
        fontFamily = PressStart2PFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 1.sp
    ),
    val body: TextStyle = TextStyle(
        fontFamily = PressStart2PFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    ),
    val caption: TextStyle = TextStyle(
        fontFamily = PressStart2PFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 7.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.sp
    ),
    val label: TextStyle = TextStyle(
        fontFamily = PressStart2PFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 8.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)

val LocalWingmanTypography = staticCompositionLocalOf { WingmanTypography() }
