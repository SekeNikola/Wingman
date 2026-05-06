package com.wingman.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wingman.launcher.ui.theme.WingmanTheme
import kotlinx.coroutines.delay

private val BOOT_LINES = listOf(
    "WINGMAN OS v2.4.1",
    "INITIALIZING CORE SYSTEMS...",
    "LOADING MISSION PROFILES...",
    "SCANNING ENCRYPTED CHANNELS...",
    "CALIBRATING SENSOR ARRAY...",
    "ACCESSING MODULE DATABASE...",
    "ESTABLISHING SECURE LINK...",
    "SYSTEM READY."
)

private const val CHAR_DELAY_MS = 30L
private const val LINE_PAUSE_MS = 120L

/**
 * Boot sequence composable.
 * Types out [BOOT_LINES] character by character, then calls [onBootComplete].
 * Total duration is approximately 3 seconds.
 *
 * [effectIntensity] is forwarded to [ScanlineOverlay] so the boot screen
 * respects the global effect-intensity setting (BUG-12).
 */
@Composable
fun BootScreen(
    onBootComplete: () -> Unit,
    effectIntensity: Float = 0.5f
) {
    val colors = WingmanTheme.colors
    val typo = WingmanTheme.typography

    val completedLines = remember { mutableStateListOf<String>() }
    var currentLine by remember { mutableStateOf("") }
    var lineIndex by remember { mutableIntStateOf(0) }
    var charIndex by remember { mutableIntStateOf(0) }
    var done by remember { mutableStateOf(false) }

    // typing effect coroutine
    LaunchedEffect(Unit) {
        while (lineIndex < BOOT_LINES.size) {
            val line = BOOT_LINES[lineIndex]
            while (charIndex <= line.length) {
                currentLine = line.substring(0, charIndex)
                charIndex++
                delay(CHAR_DELAY_MS)
            }
            completedLines.add(line)
            currentLine = ""
            charIndex = 0
            lineIndex++
            delay(LINE_PAUSE_MS)
        }
        done = true
        delay(300L)
        onBootComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            completedLines.forEach { line ->
                WingmanText(
                    text = "> $line",
                    style = typo.body,
                    color = colors.secondary
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            if (currentLine.isNotEmpty()) {
                WingmanText(
                    text = "> ${currentLine}_",
                    style = typo.body,
                    color = colors.primary
                )
            }
        }

        // scanline overlay on boot screen too
        ScanlineOverlay(intensity = effectIntensity)
    }
}
