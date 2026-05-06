package com.wingman.launcher.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wingman.launcher.data.model.SystemStatus
import com.wingman.launcher.ui.components.NoiseOverlay
import com.wingman.launcher.ui.components.ScanlineOverlay
import com.wingman.launcher.ui.components.TopBar
import com.wingman.launcher.ui.components.WingmanText
import com.wingman.launcher.ui.input.InputEvent
import com.wingman.launcher.ui.input.handleWingmanInput
import com.wingman.launcher.ui.input.rememberInputHandler
import com.wingman.launcher.ui.theme.WingmanTheme
import kotlinx.coroutines.launch

private val TUTORIAL_TEXT = """
WINGMAN FIELD MANUAL — SYSTEM OPERATIONS

SECTION 1: SYSTEM OVERVIEW
The WINGMAN terminal is a secure mobile operations hub designed for field use. All data transmitted through this device is encrypted using military-grade protocols. Unauthorized access attempts are logged and reported automatically.

SECTION 2: NAVIGATION
Use the directional controls to navigate menus. Press CENTER or ENTER to activate the selected item. Press BACK or ESCAPE to return to the previous screen. The cursor is always visible and highlights the currently selected element.

SECTION 3: SCANS MODULE
The SCANS section provides access to reconnaissance data files. Each entry displays the filename, file size in kilobytes, timestamp, and current processing status. Status codes: [OK] indicates verified data, [ERR] indicates file corruption, [...] indicates pending verification.

SECTION 4: ORGANIZER MODULE
The ORGANIZER module contains mission notes and task checklists. Notes are read-only field entries. Tasks are interactive — select a task and press ENTER to toggle its completion status. Completed tasks show [X] prefix; incomplete tasks show [ ] prefix.

SECTION 5: MUSIC MODULE
The MUSIC section provides access to encrypted audio transmissions. Use PREV and NEXT controls to navigate the track queue. Press PLAY to begin transmission. The ASCII progress bar indicates playback position. Track duration is displayed in MM:SS format.

SECTION 6: SETTINGS MODULE
The SETTINGS module allows adjustment of terminal display parameters. EFFECT INTENSITY controls the strength of all visual overlays including scanlines, noise grain, and item glow. Drag the slider to 0 to disable effects entirely or to 1.0 for maximum intensity. THEME VARIANT switches between STANDARD (warm amber on dark green) and HIGH CONTRAST (bright yellow on black) display modes.

SECTION 7: INPUT METHODS
This terminal accepts three input methods:
- HARDWARE D-PAD: directional keys control cursor movement.
- KEYBOARD: W/S for up/down, ENTER for activation, ESCAPE for back.
- SCROLL WHEEL: rotate to move selection up or down.
- TOUCH: single tap to select, second tap on selected item to activate.
Key repeat acceleration: hold UP or DOWN for 300ms to enable fast repeat mode at 80ms intervals.

SECTION 8: SECURITY PROTOCOLS
Do not leave this terminal unattended in unsecured locations. All module access events are logged. Encrypted channels are verified against current session keys. If a transmission appears corrupted, use the SCANS module to check file integrity before proceeding with any operations.

SECTION 9: BOOT SEQUENCE
On power-on, the terminal runs a self-diagnostic sequence. Core systems, mission profiles, encrypted channels, sensor array calibration, and module database access are all verified in sequence. If any component fails self-test, the status will show an error code. Contact your mission handler for further instructions.

SECTION 10: MAINTENANCE
Keep the terminal firmware updated. Visual effects can be reduced on lower-power devices by decreasing EFFECT INTENSITY in SETTINGS. The terminal is designed to operate on Android 8.0 and above. Display resolution is adaptive to screen size.

END OF FIELD MANUAL — CLASSIFICATION: RESTRICTED
""".trimIndent()

@Composable
fun TutorialsScreen(
    onBack: () -> Unit,
    effectIntensity: Float,
    systemStatus: SystemStatus
) {
    BackHandler { onBack() }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val colors = WingmanTheme.colors
    val typo = WingmanTheme.typography

    val handleInput = rememberInputHandler { event ->
        when (event) {
            is InputEvent.Up   -> coroutineScope.launch { scrollState.animateScrollBy(-80f) }
            is InputEvent.Down -> coroutineScope.launch { scrollState.animateScrollBy(80f) }
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
                text = "[ TUTORIALS MODULE ]",
                style = typo.heading,
                color = colors.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                TUTORIAL_TEXT.split("\n").forEach { line ->
                    if (line.startsWith("SECTION") || line.startsWith("WINGMAN") || line.startsWith("END")) {
                        WingmanText(
                            text  = line,
                            style = typo.label,
                            color = colors.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    } else if (line.isBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    } else {
                        WingmanText(
                            text  = line,
                            style = typo.caption,
                            color = colors.secondary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        ScanlineOverlay(intensity = effectIntensity)
        NoiseOverlay(intensity = effectIntensity)
    }
}
