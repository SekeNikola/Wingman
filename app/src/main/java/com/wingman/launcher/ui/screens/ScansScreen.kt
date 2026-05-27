package com.wingman.launcher.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wingman.launcher.data.model.ScanStatus
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
import com.wingman.launcher.ui.theme.STATUS_CLEAN
import com.wingman.launcher.ui.theme.STATUS_FLAGGED
import com.wingman.launcher.ui.theme.STATUS_QUARANTINED
import com.wingman.launcher.ui.theme.TerminalBody
import com.wingman.launcher.ui.theme.TerminalSmall
import com.wingman.launcher.viewmodel.ScansViewModel

/**
 * SCANS section screen.
 * List of ScanFile rows: filename left, status badge right.
 * Active row highlighted amber. DPAD navigates list.
 * Back returns to Home via [onBack].
 */
@Composable
fun ScansScreen(
    viewModel: ScansViewModel,
    settings: SettingsState,
    onBack: () -> Unit
) {
    val scans by viewModel.scans.collectAsState()
    val selectedIndex by viewModel.selectedIndex.collectAsState()
    val listState = rememberLazyListState()

    // Auto-scroll to keep selected item visible
    LaunchedEffect(selectedIndex) {
        listState.animateScrollToItem(selectedIndex)
    }

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
            // Section header in TopBar position
            TopBar(username = "SCANS")

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(scans) { index, scan ->
                    val isActive = index == selectedIndex
                    val bgColor = if (isActive) AMBER_PRIMARY.copy(alpha = 0.12f) else BG_SURFACE
                    val textColor = if (isActive) AMBER_PRIMARY else GREEN_PRIMARY

                    val statusColor = when (scan.status) {
                        ScanStatus.CLEAN -> STATUS_CLEAN
                        ScanStatus.FLAGGED -> STATUS_FLAGGED
                        ScanStatus.QUARANTINED -> STATUS_QUARANTINED
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .background(bgColor)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            TerminalText(
                                text = scan.filename,
                                style = TerminalBody,
                                color = textColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            TerminalText(
                                text = "${scan.timestamp}  ${scan.sizeKb}KB",
                                style = TerminalSmall,
                                color = GREEN_MUTED
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Status badge
                        TerminalText(
                            text = scan.status.name,
                            style = TerminalSmall,
                            color = statusColor
                        )
                    }

                    // Row separator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(BG_PRIMARY)
                    )
                }
            }
        }
    }
}
