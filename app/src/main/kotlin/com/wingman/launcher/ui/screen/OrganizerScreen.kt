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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.wingman.launcher.data.model.Note
import com.wingman.launcher.data.model.SystemStatus
import com.wingman.launcher.data.model.Task
import com.wingman.launcher.ui.components.NoiseOverlay
import com.wingman.launcher.ui.components.ScanlineOverlay
import com.wingman.launcher.ui.components.TopBar
import com.wingman.launcher.ui.components.WingmanText
import com.wingman.launcher.ui.input.InputEvent
import com.wingman.launcher.ui.input.handleWingmanInput
import com.wingman.launcher.ui.input.rememberInputHandler
import com.wingman.launcher.ui.theme.WingmanTheme
import com.wingman.launcher.viewmodel.OrganizerViewModel

@Composable
fun OrganizerScreen(
    onBack: () -> Unit,
    effectIntensity: Float,
    systemStatus: SystemStatus,
    viewModel: OrganizerViewModel = hiltViewModel()
) {
    BackHandler { onBack() }

    val uiState by viewModel.uiState.collectAsState()
    val colors = WingmanTheme.colors
    val typo = WingmanTheme.typography

    val totalItems = uiState.notes.size + uiState.tasks.size
    var selectedIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(uiState.notes.size, uiState.tasks.size) {
        val total = uiState.notes.size + uiState.tasks.size
        selectedIndex = if (total > 0) selectedIndex.coerceAtMost(total - 1) else 0
    }

    val handleInput = rememberInputHandler { event ->
        when (event) {
            is InputEvent.Up   -> selectedIndex = (selectedIndex - 1).coerceAtLeast(0)
            is InputEvent.Down -> selectedIndex = (selectedIndex + 1).coerceAtMost((totalItems - 1).coerceAtLeast(0))
            is InputEvent.Select -> {
                val taskOffset = uiState.notes.size
                if (selectedIndex >= taskOffset) {
                    val taskIdx = selectedIndex - taskOffset
                    if (taskIdx in uiState.tasks.indices) {
                        viewModel.toggleTask(uiState.tasks[taskIdx])
                    }
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
                text = "[ ORGANIZER MODULE ]",
                style = typo.heading,
                color = colors.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            LazyColumn(modifier = Modifier.fillMaxWidth()) {

                // ── NOTES section ──
                item {
                    WingmanText(
                        text = "-- NOTES --",
                        style = typo.label,
                        color = colors.secondary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                itemsIndexed(uiState.notes, key = { _, n -> "note_${n.id}" }) { index, note ->
                    NoteRow(
                        note       = note,
                        isSelected = index == selectedIndex,
                        onClick    = { selectedIndex = index }
                    )
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }

                // ── TASKS section ──
                item {
                    WingmanText(
                        text = "-- TASKS --",
                        style = typo.label,
                        color = colors.secondary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                itemsIndexed(uiState.tasks, key = { _, t -> "task_${t.id}" }) { index, task ->
                    val globalIndex = uiState.notes.size + index
                    TaskRow(
                        task       = task,
                        isSelected = globalIndex == selectedIndex,
                        onClick    = {
                            if (selectedIndex == globalIndex) viewModel.toggleTask(task)
                            else selectedIndex = globalIndex
                        }
                    )
                }
            }
        }

        ScanlineOverlay(intensity = effectIntensity)
        NoiseOverlay(intensity = effectIntensity)
    }
}

@Composable
private fun NoteRow(note: Note, isSelected: Boolean, onClick: () -> Unit) {
    val colors = WingmanTheme.colors
    val typo = WingmanTheme.typography

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) colors.highlightBar else Color.Transparent)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        WingmanText(
            text  = note.title,
            style = typo.body,
            color = if (isSelected) colors.black else colors.primary
        )
        Spacer(modifier = Modifier.height(2.dp))
        WingmanText(
            text  = note.body,
            style = typo.caption,
            color = if (isSelected) colors.black.copy(alpha = 0.8f) else colors.secondary,
            maxLines = 2
        )
    }
}

@Composable
private fun TaskRow(task: Task, isSelected: Boolean, onClick: () -> Unit) {
    val colors = WingmanTheme.colors
    val typo = WingmanTheme.typography

    val checkMark = if (task.isCompleted) "[X] " else "[ ] "
    val textColor = when {
        isSelected       -> colors.black
        task.isCompleted -> colors.dimText
        else             -> colors.secondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) colors.highlightBar else Color.Transparent)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        WingmanText(
            text  = checkMark,
            style = typo.body,
            color = if (isSelected) colors.black else colors.primary
        )
        WingmanText(
            text  = task.label,
            style = typo.body,
            color = textColor
        )
    }
}
