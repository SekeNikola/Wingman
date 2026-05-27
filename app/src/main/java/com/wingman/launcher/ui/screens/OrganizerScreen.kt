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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.wingman.launcher.data.model.Priority
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
import com.wingman.launcher.ui.theme.TerminalSmall
import com.wingman.launcher.viewmodel.OrganizerViewModel

/**
 * ORGANIZER section screen.
 * Task list: title + priority badge + done indicator.
 * HIGH priority: "!!" amber prefix.
 * Done tasks: strikethrough.
 * Enter on a task toggles isDone.
 */
@Composable
fun OrganizerScreen(
    viewModel: OrganizerViewModel,
    settings: SettingsState,
    onBack: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    val selectedIndex by viewModel.selectedIndex.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(selectedIndex) {
        if (tasks.isNotEmpty()) listState.animateScrollToItem(selectedIndex)
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
            TopBar(username = "ORGANIZER")

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(tasks) { index, task ->
                    val isActive = index == selectedIndex
                    val bgColor = if (isActive) AMBER_PRIMARY.copy(alpha = 0.12f) else BG_SURFACE
                    val baseColor = if (isActive) AMBER_PRIMARY else GREEN_PRIMARY
                    val labelColor = if (task.isDone) GREEN_MUTED else baseColor
                    val textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None

                    val priorityPrefix = when (task.priority) {
                        Priority.HIGH -> "!! "
                        Priority.NORMAL -> "   "
                        Priority.LOW -> "   "
                    }
                    val priorityColor = when (task.priority) {
                        Priority.HIGH -> AMBER_PRIMARY
                        else -> GREEN_MUTED
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .background(bgColor)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Priority prefix
                        TerminalText(
                            text = priorityPrefix.trim().ifEmpty { "  " },
                            style = TerminalBody,
                            color = priorityColor
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Task title — possibly strikethrough
                        Column(modifier = Modifier.weight(1f)) {
                            TerminalText(
                                text = task.title,
                                style = TerminalBody.copy(textDecoration = textDecoration),
                                color = labelColor
                            )
                            if (task.body.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                TerminalText(
                                    text = task.body.take(40),
                                    style = TerminalSmall,
                                    color = GREEN_MUTED.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Done indicator
                        if (task.isDone) {
                            TerminalText(
                                text = "[X]",
                                style = TerminalSmall,
                                color = GREEN_MUTED
                            )
                        } else {
                            TerminalText(
                                text = "[ ]",
                                style = TerminalSmall,
                                color = GREEN_MUTED.copy(alpha = 0.4f)
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
    }
}
