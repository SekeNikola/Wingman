package com.wingman.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wingman.launcher.data.model.SystemStatus
import com.wingman.launcher.ui.theme.WingmanTheme

/**
 * Status bar row rendered directly over the device background image.
 * No opaque fill — a subtle black veil keeps text legible over any image colour.
 */
@Composable
fun TopBar(
    status: SystemStatus,
    modifier: Modifier = Modifier
) {
    val colors = WingmanTheme.colors
    val typo   = WingmanTheme.typography

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color.Black.copy(alpha = 0.45f))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        WingmanText(
            text  = status.username,
            style = typo.heading,
            color = colors.primary
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WingmanText(
                text  = status.timeLabel,
                style = typo.caption,
                color = colors.secondary
            )

            SignalBarsIcon(
                bars     = status.signalBars,
                color    = colors.primary,
                dimColor = colors.dimText,
                sizeDp   = 14.dp,
                modifier = Modifier.size(14.dp)
            )

            BatteryIcon(
                batteryPercent = status.batteryPercent,
                isCharging     = status.isCharging,
                color          = colors.primary,
                sizeDp         = 20.dp,
                modifier       = Modifier
                    .width(20.dp)
                    .height(12.dp)
            )
        }
    }

    // Single amber separator line
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(colors.primary.copy(alpha = 0.5f))
    )
}
