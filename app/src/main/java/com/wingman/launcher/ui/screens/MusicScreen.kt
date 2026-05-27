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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wingman.launcher.data.model.MusicTrack
import com.wingman.launcher.data.model.SettingsState
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
import com.wingman.launcher.ui.theme.TerminalLarge
import com.wingman.launcher.ui.theme.TerminalSmall
import com.wingman.launcher.viewmodel.MusicViewModel

/**
 * MUSIC section screen.
 *
 * Layout:
 *  - TopBar with "MUSIC" label
 *  - Now-playing panel: track name, artist, pixel progress bar, play/pause indicator
 *  - Track list below with active track highlighted amber
 *  - DPAD selects track, Enter plays it
 *
 * Progress bar: pixel segmented style, amber fill.
 * No actual audio — progress is simulated via MusicViewModel tick.
 */
@Composable
fun MusicScreen(
    viewModel: MusicViewModel,
    settings: SettingsState,
    onBack: () -> Unit
) {
    val playerState by viewModel.playerState.collectAsState()
    val selectedIndex by viewModel.selectedIndex.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(selectedIndex) {
        if (playerState.tracks.isNotEmpty()) listState.animateScrollToItem(selectedIndex)
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
            TopBar(username = "MUSIC")

            // Now-playing panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BG_SURFACE)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val track = playerState.currentTrack
                    if (track != null) {
                        // Play/pause indicator + track info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Pixel play/pause indicator
                            PlayPauseIndicator(
                                isPlaying = playerState.isPlaying,
                                color = AMBER_PRIMARY,
                                modifier = Modifier.padding(end = 10.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                TerminalText(
                                    text = track.title,
                                    style = TerminalLarge,
                                    color = AMBER_PRIMARY
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                TerminalText(
                                    text = track.artist,
                                    style = TerminalSmall,
                                    color = GREEN_MUTED
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Pixel segmented progress bar
                        PixelProgressBar(
                            progress = if (track.durationSeconds > 0) {
                                playerState.progressSeconds.toFloat() / track.durationSeconds
                            } else 0f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Time display
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TerminalText(
                                text = formatTime(playerState.progressSeconds),
                                style = TerminalSmall,
                                color = GREEN_MUTED
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            TerminalText(
                                text = formatTime(track.durationSeconds),
                                style = TerminalSmall,
                                color = GREEN_MUTED
                            )
                        }
                    } else {
                        TerminalText(
                            text = "NO TRACK LOADED",
                            style = TerminalBody,
                            color = GREEN_MUTED
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        TerminalText(
                            text = "SELECT TRACK AND PRESS ENTER",
                            style = TerminalSmall,
                            color = GREEN_MUTED.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Track list
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(playerState.tracks) { index, track ->
                    val isSelected = index == selectedIndex
                    val isCurrentTrack = track.id == playerState.currentTrack?.id
                    val bgColor = when {
                        isSelected && isCurrentTrack -> AMBER_PRIMARY.copy(alpha = 0.18f)
                        isSelected -> AMBER_PRIMARY.copy(alpha = 0.10f)
                        isCurrentTrack -> AMBER_PRIMARY.copy(alpha = 0.06f)
                        else -> BG_SURFACE
                    }
                    val textColor = if (isSelected || isCurrentTrack) AMBER_PRIMARY else GREEN_PRIMARY

                    TrackRow(
                        track = track,
                        isSelected = isSelected,
                        isPlaying = isCurrentTrack && playerState.isPlaying,
                        textColor = textColor,
                        bgColor = bgColor
                    )

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

@Composable
private fun TrackRow(
    track: MusicTrack,
    isSelected: Boolean,
    isPlaying: Boolean,
    textColor: Color,
    bgColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(bgColor)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Indicator
        TerminalText(
            text = when {
                isPlaying -> ">"
                isSelected -> ">"
                else -> " "
            },
            style = TerminalBody,
            color = AMBER_PRIMARY
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            TerminalText(
                text = track.title,
                style = TerminalBody,
                color = textColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            TerminalText(
                text = track.artist,
                style = TerminalSmall,
                color = GREEN_MUTED
            )
        }

        TerminalText(
            text = formatTime(track.durationSeconds),
            style = TerminalSmall,
            color = GREEN_MUTED
        )
    }
}

/** Pixel-style segmented progress bar — amber filled blocks, dim empty blocks. */
@Composable
private fun PixelProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier, contentDescription = "PROGRESS") {
        val segments = 20
        val gapPx = 2f
        val segW = (size.width - gapPx * (segments - 1)) / segments
        val clamped = progress.coerceIn(0f, 1f)
        val filledCount = (clamped * segments).toInt()

        for (i in 0 until segments) {
            val x = i * (segW + gapPx)
            val color = if (i < filledCount) AMBER_PRIMARY else AMBER_DIM.copy(alpha = 0.3f)
            drawRect(
                color = color,
                topLeft = Offset(x, 0f),
                size = Size(segW, size.height)
            )
        }
    }
}

/** Pixel play/pause triangle or pause bars. */
@Composable
private fun PlayPauseIndicator(
    isPlaying: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.padding(2.dp),
        contentDescription = if (isPlaying) "PLAYING" else "PAUSED"
    ) {
        val s = minOf(size.width, size.height)
        if (!isPlaying) {
            // Pause: two vertical bars
            val barW = s * 0.28f
            val barH = s * 0.8f
            val top = (s - barH) / 2
            drawRect(color, Offset(s * 0.1f, top), Size(barW, barH))
            drawRect(color, Offset(s * 0.62f, top), Size(barW, barH))
        } else {
            // Play: filled triangle
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(s * 0.1f, s * 0.05f)
                lineTo(s * 0.95f, s * 0.5f)
                lineTo(s * 0.1f, s * 0.95f)
                close()
            }
            drawPath(path, color = color)
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
