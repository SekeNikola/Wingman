package com.wingman.launcher.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier

/**
 * WingmanTheme — root wrapper.
 * No Material. Custom color system only.
 * Sets background to BG_PRIMARY and provides the Press Start 2P font environment.
 */
@Composable
fun WingmanTheme(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BG_PRIMARY)
    ) {
        content()
    }
}
