package com.wingman.launcher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.wingman.launcher.data.model.SettingsState
import com.wingman.launcher.ui.components.TerminalText
import com.wingman.launcher.ui.components.TopBar
import com.wingman.launcher.ui.effects.noiseGrain
import com.wingman.launcher.ui.effects.scanlines
import com.wingman.launcher.ui.theme.AMBER_PRIMARY
import com.wingman.launcher.ui.theme.BG_PRIMARY
import com.wingman.launcher.ui.theme.BG_SURFACE
import com.wingman.launcher.ui.theme.GREEN_MUTED
import com.wingman.launcher.ui.theme.GREEN_PRIMARY
import com.wingman.launcher.ui.theme.TerminalBody
import com.wingman.launcher.ui.theme.TerminalLarge
import com.wingman.launcher.ui.theme.TerminalSmall
import com.wingman.launcher.viewmodel.TutorialsNavState
import com.wingman.launcher.viewmodel.TutorialsViewModel

/**
 * Two-level tutorials navigator.
 * List view: title list, DPAD navigates, Enter opens detail.
 * Detail view: title + scrollable content body.
 *   DPAD_UP/DOWN scrolls via offset. Back returns to list.
 * Back from list is handled by the caller (MainViewModel.navigateBack).
 */
@Composable
fun TutorialsScreen(
    viewModel: TutorialsViewModel,
    settings: SettingsState,
    onBack: () -> Unit
) {
    val tutorials by viewModel.tutorials.collectAsState()
    val selectedIndex by viewModel.selectedIndex.collectAsState()
    val navState by viewModel.tutorialsNavState.collectAsState()

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
        when (val nav = navState) {
            is TutorialsNavState.List -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TopBar(username = "TUTORIALS")

                    tutorials.forEachIndexed { index, entry ->
                        val isActive = index == selectedIndex
                        val bgColor = if (isActive) AMBER_PRIMARY.copy(alpha = 0.12f) else BG_SURFACE
                        val textColor = if (isActive) AMBER_PRIMARY else GREEN_PRIMARY

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
                            Column(modifier = Modifier.weight(1f)) {
                                TerminalText(
                                    text = entry.title,
                                    style = TerminalBody,
                                    color = textColor
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                TerminalText(
                                    text = entry.tags.joinToString(" "),
                                    style = TerminalSmall,
                                    color = GREEN_MUTED
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(BG_PRIMARY)
                        )
                    }
                }
            }

            is TutorialsNavState.Detail -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TopBar(username = nav.entry.title.take(12))

                    // Scrollable content via offset (DPAD controls scrollOffset)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset { IntOffset(0, -nav.scrollOffset) }
                        ) {
                            TerminalText(
                                text = nav.entry.title,
                                style = TerminalLarge,
                                color = AMBER_PRIMARY,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            nav.entry.content.split("\n").forEach { line ->
                                TerminalText(
                                    text = line.ifBlank { " " },
                                    style = TerminalBody,
                                    color = GREEN_PRIMARY,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            TerminalText(
                                text = "[BACK] TO LIST",
                                style = TerminalSmall,
                                color = GREEN_MUTED
                            )
                        }
                    }
                }
            }
        }
    }
}
