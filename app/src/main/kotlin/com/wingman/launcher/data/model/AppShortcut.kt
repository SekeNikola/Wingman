package com.wingman.launcher.data.model

import androidx.compose.ui.graphics.ImageBitmap

data class AppShortcut(
    val packageName: String,
    val label: String,
    val icon: ImageBitmap? = null
)
