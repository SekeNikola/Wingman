package com.wingman.launcher.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.wingman.launcher.R

// Press Start 2P — pixel monospaced. Loaded from res/font/
val PressStart2P = FontFamily(
    Font(R.font.press_start_2p, FontWeight.Normal)
)

// === Type Scale ===
// All text rendered ALL_CAPS at composable level (text.uppercase())
// Line height 1.6× minimum for pixel font breathing room.

val TerminalLarge = TextStyle(
    fontFamily = PressStart2P,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = (14 * 1.6).sp,
    letterSpacing = 0.1.em
)

val TerminalBody = TextStyle(
    fontFamily = PressStart2P,
    fontWeight = FontWeight.Normal,
    fontSize = 10.sp,
    lineHeight = (10 * 1.6).sp,
    letterSpacing = 0.05.em
)

val TerminalSmall = TextStyle(
    fontFamily = PressStart2P,
    fontWeight = FontWeight.Normal,
    fontSize = 8.sp,
    lineHeight = (8 * 1.6).sp,
    letterSpacing = 0.05.em
)

val TerminalMicro = TextStyle(
    fontFamily = PressStart2P,
    fontWeight = FontWeight.Normal,
    fontSize = 6.sp,
    lineHeight = (6 * 1.6).sp,
    letterSpacing = 0.em
)
