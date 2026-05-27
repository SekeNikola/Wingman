package com.wingman.launcher.ui.theme

import androidx.compose.ui.graphics.Color

// === Wingman Terminal Color System ===
// All values matched to reference device image.

val BG_PRIMARY    = Color(0xFF0A0C0A)   // near-black with slight green cast — main background
val BG_SURFACE    = Color(0xFF111411)   // slightly lighter — row backgrounds
val BG_TOPBAR     = Color(0xFF0D0F0D)   // top bar background

val AMBER_PRIMARY = Color(0xFFE8820C)   // active highlight bar, active text, arrow cursor
val AMBER_GLOW    = Color(0xFFF5A030)   // glow corona around active item
val AMBER_DIM     = Color(0xFF7A4406)   // dimmed amber for inactive decorative elements

val GREEN_PRIMARY = Color(0xFF4CAF50)   // inactive menu text
val GREEN_MUTED   = Color(0xFF2E5C30)   // secondary text, status indicators, dim labels
val GREEN_DIM     = Color(0xFF1A3320)   // barely visible tint for separators

val SCANLINE_TINT = Color(0x33000000)   // semi-transparent black for scanline overlay
val GRAIN_TINT    = Color(0x08FFFFFF)   // near-invisible white noise layer

val WHITE_PIXEL   = Color(0xFFD4D8D0)   // brightest text (username, headings)
val CURSOR_ARROW  = Color(0xFFE8820C)   // ">" glyph, same as AMBER_PRIMARY

// Status badge colours (used in ScansScreen)
val STATUS_CLEAN       = Color(0xFF4CAF50)
val STATUS_FLAGGED     = Color(0xFFE8820C)
val STATUS_QUARANTINED = Color(0xFFCC3300)
