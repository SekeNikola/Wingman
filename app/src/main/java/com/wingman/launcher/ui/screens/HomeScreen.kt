package com.wingman.launcher.ui.screens

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.wingman.launcher.data.model.MenuSection
import com.wingman.launcher.data.model.SettingsState
import com.wingman.launcher.ui.components.MenuRow
import com.wingman.launcher.ui.components.TopBar
import com.wingman.launcher.ui.effects.noiseGrain
import com.wingman.launcher.ui.effects.scanlines
import com.wingman.launcher.ui.theme.BG_PRIMARY

/**
 * Home screen — main menu.
 *
 * Layout: TopBar + 5 MenuRows stacked vertically.
 * Active item driven by [selectedIndex] from NavigationState.Home.
 * Touch swipe up/down changes selection; tap enters section.
 * Scanlines + grain overlays respect settings.
 *
 * Selection change uses animateIntAsState (80ms tween) for stepped snap feel.
 */
@Composable
fun HomeScreen(
    selectedIndex: Int,
    settings: SettingsState,
    onSectionSelected: (MenuSection) -> Unit,
    onSelectionChanged: (Int) -> Unit
) {
    val sections = MenuSection.entries
    val count = sections.size

    // Stepped snap animation on index change
    val animatedIndex by animateIntAsState(
        targetValue = selectedIndex,
        animationSpec = tween(durationMillis = 80),
        label = "menuSelection"
    )

    // Swipe gesture accumulator
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = 80f // dp-equivalent pixels to trigger a step

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
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = { dragAccumulator = 0f },
                    onDragCancel = { dragAccumulator = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        dragAccumulator += dragAmount
                        if (dragAccumulator < -swipeThreshold) {
                            // Swipe up → index decreases (move up)
                            val newIndex = (selectedIndex - 1 + count) % count
                            onSelectionChanged(newIndex)
                            dragAccumulator = 0f
                        } else if (dragAccumulator > swipeThreshold) {
                            // Swipe down → index increases
                            val newIndex = (selectedIndex + 1) % count
                            onSelectionChanged(newIndex)
                            dragAccumulator = 0f
                        }
                    }
                )
            }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top bar with username
            TopBar(username = settings.username)

            // 5 menu rows
            sections.forEach { section ->
                MenuRow(
                    section = section,
                    isActive = animatedIndex == section.index,
                    settings = settings,
                    onClick = { onSectionSelected(section) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
