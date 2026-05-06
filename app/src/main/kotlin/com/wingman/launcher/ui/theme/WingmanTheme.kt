package com.wingman.launcher.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ── Color container ──────────────────────────────────────────────────────────
data class WingmanColors(
    val background: Color,
    val primary: Color,
    val secondary: Color,
    val dimText: Color,
    val highlightBar: Color,
    val black: Color,
    val white: Color,
    val glowOrange: Color
)

val StandardColors = WingmanColors(
    background   = ColorBackground,
    primary      = ColorPrimary,
    secondary    = ColorSecondary,
    dimText      = ColorDimText,
    highlightBar = ColorHighlightBar,
    black        = ColorBlack,
    white        = ColorWhite,
    glowOrange   = ColorGlowOrange
)

val HighContrastColors = WingmanColors(
    background   = ColorHCBackground,
    primary      = ColorHCPrimary,
    secondary    = ColorHCSecondary,
    dimText      = ColorSecondary,
    highlightBar = ColorHCPrimary,
    black        = ColorBlack,
    white        = ColorWhite,
    glowOrange   = ColorHCPrimary
)

val LocalWingmanColors = staticCompositionLocalOf { StandardColors }

// ── Theme wrapper ─────────────────────────────────────────────────────────────
@Composable
fun WingmanTheme(
    highContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (highContrast) HighContrastColors else StandardColors
    val typography = WingmanTypography()

    CompositionLocalProvider(
        LocalWingmanColors provides colors,
        LocalWingmanTypography provides typography
    ) {
        content()
    }
}

// ── Convenience accessor ──────────────────────────────────────────────────────
object WingmanTheme {
    val colors: WingmanColors
        @Composable get() = LocalWingmanColors.current
    val typography: WingmanTypography
        @Composable get() = LocalWingmanTypography.current
}
