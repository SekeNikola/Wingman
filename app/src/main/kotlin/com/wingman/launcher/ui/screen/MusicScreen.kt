package com.wingman.launcher.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wingman.launcher.data.model.PlayerState
import com.wingman.launcher.data.model.SystemStatus
import com.wingman.launcher.data.model.TrackInfo
import com.wingman.launcher.ui.components.NoiseOverlay
import com.wingman.launcher.ui.components.ScanlineOverlay
import com.wingman.launcher.ui.components.TopBar
import com.wingman.launcher.ui.components.WingmanText
import com.wingman.launcher.ui.input.InputEvent
import com.wingman.launcher.ui.input.handleWingmanInput
import com.wingman.launcher.ui.input.rememberInputHandler
import com.wingman.launcher.ui.theme.WingmanTheme
import com.wingman.launcher.viewmodel.MusicViewModel

private val CONTROLS = listOf("PREV", "PLAY/PAUSE", "NEXT")

@Composable
fun MusicScreen(
    onBack: () -> Unit,
    effectIntensity: Float,
    systemStatus: SystemStatus,
    viewModel: MusicViewModel = hiltViewModel()
) {
    BackHandler { onBack() }

    val playerState by viewModel.playerState.collectAsState()
    val colors = WingmanTheme.colors
    val typo = WingmanTheme.typography

    var selectedControl by remember { mutableIntStateOf(1) } // default to PLAY/PAUSE

    val handleInput = rememberInputHandler { event ->
        when (event) {
            is InputEvent.Up   -> selectedControl = (selectedControl - 1).coerceAtLeast(0)
            is InputEvent.Down -> selectedControl = (selectedControl + 1).coerceAtMost(CONTROLS.size - 1)
            is InputEvent.Select -> {
                when (selectedControl) {
                    0 -> viewModel.previous()
                    1 -> if (playerState.isPlaying) viewModel.pause() else viewModel.play()
                    2 -> viewModel.next()
                }
            }
            is InputEvent.Back -> onBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .handleWingmanInput(handleInput)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            TopBar(status = systemStatus)

            Spacer(modifier = Modifier.height(8.dp))

            WingmanText(
                text = "[ MUSIC MODULE ]",
                style = typo.heading,
                color = colors.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Track info
            playerState.currentTrack?.let { track ->
                TrackInfoPanel(track = track)
            } ?: WingmanText(
                text = "NO TRACK LOADED",
                style = typo.body,
                color = colors.dimText,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ASCII progress bar
            ProgressBar(
                playerState = playerState,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Controls
            WingmanText(
                text = "CONTROLS",
                style = typo.caption,
                color = colors.dimText,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            CONTROLS.forEachIndexed { index, label ->
                val isSelected = index == selectedControl
                val displayLabel = when (index) {
                    1 -> if (playerState.isPlaying) "|| PAUSE" else "|> PLAY"
                    0 -> "|< PREV"
                    2 -> "NEXT >|"
                    else -> label
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(if (isSelected) colors.highlightBar else colors.background)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            if (selectedControl == index) {
                                when (index) {
                                    0 -> viewModel.previous()
                                    1 -> if (playerState.isPlaying) viewModel.pause() else viewModel.play()
                                    2 -> viewModel.next()
                                }
                            } else {
                                selectedControl = index
                            }
                        }
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WingmanText(
                        text = displayLabel,
                        style = typo.body,
                        color = if (isSelected) colors.black else colors.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Queue info
            WingmanText(
                text = "TRACK ${playerState.queueIndex + 1} / ${playerState.totalTracks}",
                style = typo.caption,
                color = colors.dimText,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        ScanlineOverlay(intensity = effectIntensity)
        NoiseOverlay(intensity = effectIntensity)
    }
}

@Composable
private fun TrackInfoPanel(
    track: TrackInfo
) {
    val colors = WingmanTheme.colors
    val typo = WingmanTheme.typography
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        WingmanText(
            text  = track.title,
            style = typo.heading,
            color = colors.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        WingmanText(
            text  = track.artist,
            style = typo.body,
            color = colors.secondary
        )
        Spacer(modifier = Modifier.height(2.dp))
        WingmanText(
            text  = formatDuration(track.durationSeconds),
            style = typo.caption,
            color = colors.dimText
        )
    }
}

@Composable
private fun ProgressBar(
    playerState: PlayerState,
    modifier: Modifier = Modifier
) {
    val colors = WingmanTheme.colors
    val typo = WingmanTheme.typography
    val duration = playerState.currentTrack?.durationSeconds ?: 1
    val progress = if (duration > 0) playerState.progressSeconds.toFloat() / duration else 0f
    val barLength = 24
    val filled = (progress * barLength).toInt().coerceIn(0, barLength)
    val bar = "[" + "=".repeat(filled) + "-".repeat(barLength - filled) + "]"
    val timeStr = "${formatDuration(playerState.progressSeconds)} / ${formatDuration(duration)}"

    Column(modifier = modifier) {
        WingmanText(text = bar, style = typo.caption, color = colors.secondary)
        Spacer(modifier = Modifier.height(2.dp))
        WingmanText(text = timeStr, style = typo.caption, color = colors.dimText)
    }
}

private fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
