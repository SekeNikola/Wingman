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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wingman.launcher.data.model.ScanFile
import com.wingman.launcher.data.model.ScanStatus
import com.wingman.launcher.data.model.ScansUiState
import com.wingman.launcher.data.model.SystemStatus
import com.wingman.launcher.ui.components.NoiseOverlay
import com.wingman.launcher.ui.components.ScanlineOverlay
import com.wingman.launcher.ui.components.TopBar
import com.wingman.launcher.ui.components.WingmanText
import com.wingman.launcher.ui.input.InputEvent
import com.wingman.launcher.ui.input.handleWingmanInput
import com.wingman.launcher.ui.input.rememberInputHandler
import com.wingman.launcher.ui.theme.WingmanTheme
import com.wingman.launcher.viewmodel.ScansViewModel

@Composable
fun ScansScreen(
    onBack: () -> Unit,
    effectIntensity: Float,
    systemStatus: SystemStatus,
    viewModel: ScansViewModel = hiltViewModel()
) {
    BackHandler { onBack() }

    val uiState by viewModel.uiState.collectAsState()
    val colors = WingmanTheme.colors
    val typo = WingmanTheme.typography

    var selectedIndex by remember { mutableIntStateOf(0) }

    val scans = when (val s = uiState) {
        is ScansUiState.Success -> s.files
        else -> emptyList()
    }

    LaunchedEffect(scans.size) {
        selectedIndex = if (scans.isNotEmpty()) selectedIndex.coerceAtMost(scans.size - 1) else 0
    }

    val handleInput = rememberInputHandler { event ->
        when (event) {
            is InputEvent.Up   -> selectedIndex = (selectedIndex - 1).coerceAtLeast(0)
            is InputEvent.Down -> selectedIndex = (selectedIndex + 1).coerceAtMost((scans.size - 1).coerceAtLeast(0))
            is InputEvent.Back -> onBack()
            else -> {}
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
                text = "[ SCANS MODULE ]",
                style = typo.heading,
                color = colors.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            WingmanText(
                text = "ACCESSING MODULE...",
                style = typo.caption,
                color = colors.secondary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            when (val s = uiState) {
                is ScansUiState.Loading -> {
                    WingmanText(
                        text = ">> LOADING DATA...",
                        style = typo.body,
                        color = colors.dimText,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                is ScansUiState.Error -> {
                    WingmanText(
                        text = "ERROR: ${s.message}",
                        style = typo.body,
                        color = Color.Red,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                is ScansUiState.Success -> {
                    LazyColumn {
                        itemsIndexed(s.files, key = { _, f -> f.id }) { index, file ->
                            ScanFileRow(
                                file       = file,
                                isSelected = index == selectedIndex,
                                onClick    = { selectedIndex = index }
                            )
                        }
                    }
                }
            }
        }

        ScanlineOverlay(intensity = effectIntensity)
        NoiseOverlay(intensity = effectIntensity)
    }
}

@Composable
private fun ScanFileRow(
    file: ScanFile,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = WingmanTheme.colors
    val typo = WingmanTheme.typography

    val bg = if (isSelected) colors.highlightBar else Color.Transparent
    val textColor = if (isSelected) colors.black else colors.secondary

    val statusColor = when (file.status) {
        ScanStatus.COMPLETE -> colors.secondary
        ScanStatus.CORRUPT  -> Color(0xFFCC3333)
        ScanStatus.PENDING  -> colors.dimText
    }
    val statusLabel = when (file.status) {
        ScanStatus.COMPLETE -> "[OK]  "
        ScanStatus.CORRUPT  -> "[ERR] "
        ScanStatus.PENDING  -> "[...] "
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row {
            WingmanText(
                text  = statusLabel,
                style = typo.caption,
                color = if (isSelected) colors.black else statusColor
            )
            WingmanText(
                text  = file.filename,
                style = typo.body,
                color = textColor
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Row {
            WingmanText(
                text  = "${file.sizeKb}KB  ",
                style = typo.caption,
                color = if (isSelected) colors.black.copy(alpha = 0.7f) else colors.dimText
            )
            WingmanText(
                text  = file.timestamp,
                style = typo.caption,
                color = if (isSelected) colors.black.copy(alpha = 0.7f) else colors.dimText
            )
        }
    }
}
