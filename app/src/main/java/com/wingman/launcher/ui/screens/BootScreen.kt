package com.wingman.launcher.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wingman.launcher.data.model.SettingsState
import com.wingman.launcher.ui.components.TerminalText
import com.wingman.launcher.ui.effects.noiseGrain
import com.wingman.launcher.ui.effects.scanlines
import com.wingman.launcher.ui.theme.AMBER_PRIMARY
import com.wingman.launcher.ui.theme.BG_PRIMARY
import com.wingman.launcher.ui.theme.GREEN_PRIMARY
import com.wingman.launcher.ui.theme.TerminalBody
import com.wingman.launcher.ui.theme.TerminalLarge
import kotlinx.coroutines.delay

/**
 * Boot sequence composable.
 *
 * Timing (ARCHITECTURE.md):
 *  0.0s  — Black screen (BG_PRIMARY)
 *  0.3s  — Header fades in: "WINGMAN OS v1.0"
 *  1.0s  — Line 1 types: "INITIALIZING SYSTEM..."
 *  1.6s  — Line 2: "LOADING MODULES..."
 *  2.2s  — Line 3: "ACCESSING LAUNCHER..."
 *  2.8s  — Line 4: "OK" (amber)
 *  3.0s  — CRT flash (white overlay, 150ms fade)
 *  3.2s  — calls onBootComplete()
 *
 * Each line types one character at a time via coroutine delay.
 */
@Composable
fun BootScreen(
    onBootComplete: () -> Unit,
    settings: SettingsState
) {
    // Boot sequence lines
    val lines = remember { mutableStateListOf<Pair<String, Color>>() }
    val flashAnimatable = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // 0.3s — header
        delay(300)
        lines.add("WINGMAN OS V1.0" to GREEN_PRIMARY)

        delay(100) // cursor blink start at 0.8s covered by delay before line 1

        // 1.0s — Line 1 typewriter
        delay(600)
        typewriteLine("INITIALIZING SYSTEM...", GREEN_PRIMARY, lines)

        // 1.6s — Line 2
        delay(200)
        typewriteLine("LOADING MODULES...", GREEN_PRIMARY, lines)

        // 2.2s — Line 3
        delay(200)
        typewriteLine("ACCESSING LAUNCHER...", GREEN_PRIMARY, lines)

        // 2.8s — "OK"
        delay(200)
        typewriteLine("OK", AMBER_PRIMARY, lines)

        // 3.0s — CRT flash
        delay(200)
        flashAnimatable.snapTo(1f)
        flashAnimatable.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 150)
        )

        // 3.2s — done
        delay(50)
        onBootComplete()
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Render each completed line
            lines.forEachIndexed { idx, (text, color) ->
                val style = if (idx == 0) TerminalLarge else TerminalBody
                TerminalText(
                    text = text,
                    style = style,
                    color = color,
                    flickerEnabled = false,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }

        // CRT flash overlay
        if (flashAnimatable.value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = flashAnimatable.value))
            )
        }
    }
}

/**
 * Appends a full line character by character with 40ms per character delay.
 * Each character appended triggers recompose of the line.
 */
private suspend fun typewriteLine(
    text: String,
    color: Color,
    lines: MutableList<Pair<String, Color>>
) {
    lines.add("" to color)
    val idx = lines.lastIndex
    for (char in text) {
        val current = lines[idx].first
        lines[idx] = (current + char) to color
        delay(40)
    }
}
