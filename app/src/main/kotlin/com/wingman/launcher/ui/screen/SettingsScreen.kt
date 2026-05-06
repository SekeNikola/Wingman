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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wingman.launcher.data.model.SystemStatus
import com.wingman.launcher.data.model.ThemeVariant
import com.wingman.launcher.ui.components.AppPickerOverlay
import com.wingman.launcher.ui.components.NoiseOverlay
import com.wingman.launcher.ui.components.ScanlineOverlay
import com.wingman.launcher.ui.components.TopBar
import com.wingman.launcher.ui.components.WingmanText
import com.wingman.launcher.ui.input.InputEvent
import com.wingman.launcher.ui.input.handleWingmanInput
import com.wingman.launcher.ui.input.rememberInputHandler
import com.wingman.launcher.ui.theme.WingmanTheme
import com.wingman.launcher.viewmodel.SettingsViewModel
import kotlin.math.roundToInt

private val SETTINGS_ITEMS = listOf("EFFECT INTENSITY", "THEME VARIANT", "ICON PACK", "ADJUST SCREENS")

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    effectIntensity: Float,
    systemStatus: SystemStatus,
    onOpenCalibration: () -> Unit = {},
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    BackHandler { onBack() }

    val settingsState   by settingsViewModel.settingsState.collectAsState()
    val iconPacks       by settingsViewModel.iconPacks.collectAsState()
    val selectedPack    by settingsViewModel.selectedIconPack.collectAsState()
    val colors = WingmanTheme.colors
    val typo   = WingmanTheme.typography

    var selectedIndex       by remember { mutableIntStateOf(0) }
    var showIconPackPicker  by remember { mutableStateOf(false) }

    val handleInput = rememberInputHandler { event ->
        when (event) {
            is InputEvent.Up ->
                selectedIndex = (selectedIndex - 1).coerceAtLeast(0)
            is InputEvent.Down ->
                selectedIndex = (selectedIndex + 1).coerceAtMost(SETTINGS_ITEMS.size - 1)
            is InputEvent.Select -> {
                when (selectedIndex) {
                    1 -> {
                        val next = if (settingsState.themeVariant == ThemeVariant.STANDARD)
                            ThemeVariant.HIGH_CONTRAST else ThemeVariant.STANDARD
                        settingsViewModel.updateThemeVariant(next)
                    }
                    2 -> showIconPackPicker = true
                    3 -> onOpenCalibration()
                }
            }
            is InputEvent.Back -> onBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            // BUG-07: Intercept Left/Right on the intensity row before passing to handleWingmanInput
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && selectedIndex == 0) {
                    when (keyEvent.key) {
                        Key.DirectionRight -> {
                            settingsViewModel.updateEffectIntensity(
                                (settingsState.effectIntensity + 0.1f).coerceIn(0f, 1f)
                            )
                            true
                        }
                        Key.DirectionLeft -> {
                            settingsViewModel.updateEffectIntensity(
                                (settingsState.effectIntensity - 0.1f).coerceIn(0f, 1f)
                            )
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
            .handleWingmanInput(handleInput)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            TopBar(status = systemStatus)

            Spacer(modifier = Modifier.height(8.dp))

            WingmanText(
                text = "[ SETTINGS MODULE ]",
                style = typo.heading,
                color = colors.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── EFFECT INTENSITY slider row ───────────────────────────────
            SettingsRow(
                label      = "EFFECT INTENSITY",
                isSelected = selectedIndex == 0,
                onClick    = { selectedIndex = 0 }
            ) {
                IntensitySlider(
                    value     = settingsState.effectIntensity,
                    isSelected = selectedIndex == 0,
                    onChange  = { settingsViewModel.updateEffectIntensity(it) }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── THEME VARIANT toggle row ───────────────────────────────────
            SettingsRow(
                label      = "THEME VARIANT",
                isSelected = selectedIndex == 1,
                onClick    = {
                    if (selectedIndex == 1) {
                        val next = if (settingsState.themeVariant == ThemeVariant.STANDARD)
                            ThemeVariant.HIGH_CONTRAST else ThemeVariant.STANDARD
                        settingsViewModel.updateThemeVariant(next)
                    } else {
                        selectedIndex = 1
                    }
                }
            ) {
                WingmanText(
                    text = settingsState.themeVariant.name,
                    style = typo.body,
                    color = if (selectedIndex == 1) colors.black else colors.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── ICON PACK row ─────────────────────────────────────────────────
            SettingsRow(
                label      = "ICON PACK",
                isSelected = selectedIndex == 2,
                onClick    = {
                    if (selectedIndex == 2) showIconPackPicker = true
                    else selectedIndex = 2
                }
            ) {
                val packLabel = if (selectedPack != null)
                    iconPacks.firstOrNull { it.packageName == selectedPack }?.label ?: selectedPack!!.take(14)
                else "DEFAULT"
                WingmanText(
                    text  = packLabel,
                    style = typo.caption,
                    color = if (selectedIndex == 2) colors.black else colors.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── ADJUST SCREENS row ────────────────────────────────────────────
            SettingsRow(
                label      = "ADJUST SCREENS",
                isSelected = selectedIndex == 3,
                onClick    = {
                    if (selectedIndex == 3) onOpenCalibration()
                    else selectedIndex = 3
                }
            ) {
                WingmanText(
                    text  = "[OPEN →]",
                    style = typo.caption,
                    color = if (selectedIndex == 3) colors.black else colors.dimText
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            WingmanText(
                text = "LEFT/RIGHT = adjust  |  UP/DOWN = navigate  |  ENTER = toggle",
                style = typo.caption,
                color = colors.dimText,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        ScanlineOverlay(intensity = effectIntensity)
        NoiseOverlay(intensity = effectIntensity)

        if (showIconPackPicker) {
            val packOptions = listOf(
                com.wingman.launcher.data.model.AppShortcut("", "DEFAULT (system icons)", null)
            ) + iconPacks
            AppPickerOverlay(
                apps      = packOptions,
                title     = "// ICON PACK",
                onSelect  = { pkg ->
                    settingsViewModel.setIconPack(pkg.ifEmpty { null })
                    showIconPackPicker = false
                },
                onDismiss = { showIconPackPicker = false }
            )
        }
    }
}

@Composable
private fun SettingsRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    valueContent: @Composable () -> Unit
) {
    val colors = WingmanTheme.colors
    val typo = WingmanTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(if (isSelected) colors.highlightBar else Color.Transparent)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WingmanText(
            text = label,
            style = typo.label,
            color = if (isSelected) colors.black else colors.secondary,
            modifier = Modifier.width(140.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        valueContent()
    }
}

@Composable
private fun IntensitySlider(
    value: Float,
    isSelected: Boolean,
    onChange: (Float) -> Unit
) {
    val colors = WingmanTheme.colors
    val typo = WingmanTheme.typography

    val pct = (value * 100).roundToInt()
    val barLen = 16
    val filled = (value * barLen).roundToInt().coerceIn(0, barLen)
    val bar = "[" + "#".repeat(filled) + ".".repeat(barLen - filled) + "]"

    Row(verticalAlignment = Alignment.CenterVertically) {
        WingmanText(
            text = bar,
            style = typo.caption,
            color = if (isSelected) colors.black else colors.secondary
        )
        Spacer(modifier = Modifier.width(8.dp))
        WingmanText(
            text = "$pct%",
            style = typo.caption,
            color = if (isSelected) colors.black else colors.primary
        )
    }
}
