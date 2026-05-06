package com.wingman.launcher.ui.components

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.wingman.launcher.ui.theme.LocalWingmanColors
import com.wingman.launcher.ui.theme.LocalWingmanTypography

/**
 * Non-Material text composable that uses WingmanTheme typography and colors.
 * Uses Compose's [BasicText] — never imports from material3.
 */
@Composable
fun WingmanText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalWingmanTypography.current.body,
    color: Color = LocalWingmanColors.current.secondary,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = style.copy(color = color),
        maxLines = maxLines,
        overflow = overflow
    )
}
