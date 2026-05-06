package com.wingman.launcher.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wingman.launcher.data.model.PixelIconType
import com.wingman.launcher.ui.theme.WingmanTheme
import com.wingman.launcher.util.WingmanConstants

/**
 * A single row in the main Home menu.
 *
 * Selection is animated via [animateColorAsState] (80 ms tween) — no ripple.
 * [GlowBox] wraps the row to provide orange ambient glow when selected.
 */
@Composable
fun MenuRow(
    label: String,
    iconType: PixelIconType,
    isSelected: Boolean,
    effectIntensity: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = WingmanTheme.colors
    val typo = WingmanTheme.typography

    val animatedBg by animateColorAsState(
        targetValue = if (isSelected) colors.highlightBar else Color.Transparent,
        animationSpec = tween(durationMillis = WingmanConstants.SELECTION_ANIMATION_MS),
        label = "menuRowBg"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) colors.black else colors.dimText,
        animationSpec = tween(durationMillis = WingmanConstants.SELECTION_ANIMATION_MS),
        label = "menuRowText"
    )

    val iconTint by animateColorAsState(
        targetValue = if (isSelected) colors.black else colors.secondary,
        animationSpec = tween(durationMillis = WingmanConstants.SELECTION_ANIMATION_MS),
        label = "menuRowIcon"
    )

    GlowBox(
        isActive = isSelected,
        intensity = effectIntensity,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(animatedBg)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onClick() }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // pixel icon
            PixelIcon(
                type = iconType,
                tint = iconTint,
                sizeDp = 18.dp,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            // label text
            WingmanText(
                text = label,
                style = typo.body,
                color = textColor
            )
        }
    }
}
