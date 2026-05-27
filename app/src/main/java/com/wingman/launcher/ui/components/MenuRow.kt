package com.wingman.launcher.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.wingman.launcher.data.model.MenuSection
import com.wingman.launcher.data.model.SettingsState
import com.wingman.launcher.ui.effects.terminalGlow
import com.wingman.launcher.ui.theme.AMBER_PRIMARY
import com.wingman.launcher.ui.theme.AMBER_GLOW
import com.wingman.launcher.ui.theme.GREEN_PRIMARY
import com.wingman.launcher.ui.theme.TerminalBody
import com.wingman.launcher.ui.theme.TerminalSmall

/**
 * Single menu row.
 * Active: amber highlight bar (via ActiveRowHighlight), amber text, ">" arrow right
 * Inactive: transparent bg, green text, no arrow
 *
 * Row height: 52dp minimum (Fitts's law — fat tap target)
 * Icon: 20dp PixelIcon, left of label
 * Label: always uppercase via TerminalText
 */
@Composable
fun MenuRow(
    section: MenuSection,
    isActive: Boolean,
    settings: SettingsState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = if (isActive) AMBER_PRIMARY else GREEN_PRIMARY
    val iconColor = if (isActive) AMBER_PRIMARY else GREEN_PRIMARY.copy(alpha = 0.55f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .then(
                if (isActive && settings.glowEnabled) {
                    Modifier.terminalGlow(
                        color = AMBER_GLOW,
                        radius = 8.dp,
                        glowAlpha = 0.4f
                    )
                } else Modifier
            )
            .semantics { contentDescription = section.label }
            .clickable(onClick = onClick)
    ) {
        // Active full-width highlight bar (behind content)
        if (isActive) {
            ActiveRowHighlight(modifier = Modifier.matchParentSize())
        }

        // Row content
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pixel icon
            PixelIcon(
                section = section,
                color = iconColor,
                size = 20.dp,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Section label
            TerminalText(
                text = section.label,
                style = TerminalBody,
                color = textColor,
                flickerEnabled = false,
                modifier = Modifier.weight(1f)
            )

            // Active arrow indicator
            if (isActive) {
                TerminalText(
                    text = ">",
                    style = TerminalBody,
                    color = AMBER_PRIMARY,
                    flickerEnabled = false
                )
            }
        }
    }
}
