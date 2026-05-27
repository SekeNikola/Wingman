package com.wingman.launcher.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.wingman.launcher.ui.components.TerminalText
import com.wingman.launcher.ui.components.TopBar
import com.wingman.launcher.ui.effects.noiseGrain
import com.wingman.launcher.ui.effects.scanlines
import com.wingman.launcher.ui.theme.AMBER_DIM
import com.wingman.launcher.ui.theme.AMBER_PRIMARY
import com.wingman.launcher.ui.theme.BG_PRIMARY
import com.wingman.launcher.ui.theme.BG_SURFACE
import com.wingman.launcher.ui.theme.GREEN_MUTED
import com.wingman.launcher.ui.theme.GREEN_PRIMARY
import com.wingman.launcher.ui.theme.TerminalBody
import com.wingman.launcher.ui.theme.TerminalSmall
import com.wingman.launcher.viewmodel.SettingsViewModel

/**
 * SETTINGS section screen.
 *
 * 5 navigable rows (matching SettingsViewModel rowCount):
 *  0 — EFFECTS (toggle scanlines + grain)
 *  1 — GLOW    (toggle active item glow)
 *  2 — FLICKER (toggle screen flicker)
 *  3 — SOUND   (toggle SFX)
 *  4 — INTENSITY (pixel segmented slider, cycles 0.0→1.0)
 *
 * DPAD navigates rows, Enter toggles/adjusts.
 * Changes persist via SettingsRepository/DataStore.
 * Disabling effects live-updates the app (settings observed globally).
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val selectedIndex by viewModel.selectedIndex.collectAsState()

    // Build settings rows definition
    data class SettingRow(val label: String, val value: String)

    val rows = listOf(
        SettingRow("EFFECTS", if (settings.effectsEnabled) "ON" else "OFF"),
        SettingRow("GLOW", if (settings.glowEnabled) "ON" else "OFF"),
        SettingRow("FLICKER", if (settings.flickerEnabled) "ON" else "OFF"),
        SettingRow("SOUND", if (settings.soundEnabled) "ON" else "OFF"),
        SettingRow("INTENSITY", "%.1f".format(settings.themeIntensity))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BG_PRIMARY)
            .then(
                if (settings.effectsEnabled) Modifier.scanlines(
                    alpha = 0.12f * settings.themeIntensity
                ) else Modifier
            )
            .then(
                if (settings.effectsEnabled) Modifier.noiseGrain(
                    intensity = 0.03f * settings.themeIntensity
                ) else Modifier
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TopBar(username = "SETTINGS")

            rows.forEachIndexed { index, row ->
                val isActive = index == selectedIndex
                val bgColor = if (isActive) AMBER_PRIMARY.copy(alpha = 0.12f) else BG_SURFACE
                val labelColor = if (isActive) AMBER_PRIMARY else GREEN_PRIMARY
                val valueColor = if (isActive) AMBER_PRIMARY else GREEN_MUTED

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(bgColor)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isActive) {
                        TerminalText(">", TerminalBody, AMBER_PRIMARY)
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Spacer(modifier = Modifier.width(16.dp))
                    }

                    TerminalText(
                        text = row.label,
                        style = TerminalBody,
                        color = labelColor,
                        modifier = Modifier.weight(1f)
                    )

                    // Special rendering for intensity row
                    if (index == 4) {
                        IntensityBar(
                            intensity = settings.themeIntensity,
                            isActive = isActive,
                            modifier = Modifier
                                .width(80.dp)
                                .height(10.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    TerminalText(
                        text = row.value,
                        style = TerminalSmall,
                        color = valueColor
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(BG_PRIMARY)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Hint text
            TerminalText(
                text = "ENTER TO TOGGLE / CYCLE",
                style = TerminalSmall,
                color = GREEN_MUTED.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            TerminalText(
                text = "BACK TO RETURN TO MENU",
                style = TerminalSmall,
                color = GREEN_MUTED.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

/** Pixel-style segmented intensity bar — 10 segments. */
@Composable
private fun IntensityBar(
    intensity: Float,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier, contentDescription = "INTENSITY ${(intensity * 100).toInt()}%") {
        val segments = 10
        val gapPx = 2f
        val segW = (size.width - gapPx * (segments - 1)) / segments
        val filled = (intensity * segments).toInt()
        val activeColor = if (isActive) AMBER_PRIMARY else GREEN_PRIMARY

        for (i in 0 until segments) {
            val x = i * (segW + gapPx)
            val color = if (i < filled) activeColor else AMBER_DIM.copy(alpha = 0.2f)
            drawRect(
                color = color,
                topLeft = Offset(x, 0f),
                size = Size(segW, size.height)
            )
        }
    }
}
