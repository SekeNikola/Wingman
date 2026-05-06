package com.wingman.launcher.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wingman.launcher.data.model.AppShortcut
import com.wingman.launcher.ui.theme.WingmanTheme

/**
 * Full-screen overlay listing all installed apps.
 *
 * Two modes:
 *  - Pin mode  (onToggle != null): tap toggles pin/unpin. Shows [X]/[ ] markers.
 *  - Select mode (onSelect != null): tap calls onSelect(packageName) and closes. No markers.
 */
@Composable
fun AppPickerOverlay(
    apps: List<AppShortcut>,
    title: String = "// SELECT APPS",
    pinned: Set<String> = emptySet(),
    onToggle: ((String) -> Unit)? = null,
    onSelect: ((String) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    BackHandler { onDismiss() }

    val colors = WingmanTheme.colors
    val typo   = WingmanTheme.typography
    val isPinMode = onToggle != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(colors.background.copy(alpha = 0.6f))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            WingmanText(text = title, style = typo.heading, color = colors.primary)
            WingmanText(
                text  = "[X]",
                style = typo.label,
                color = colors.secondary,
                modifier = Modifier.clickable(
                    indication        = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() }
            )
        }

        Spacer(Modifier.fillMaxWidth().height(1.dp).background(colors.primary.copy(alpha = 0.4f)))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(apps, key = { it.packageName }) { app ->
                val isPinned = app.packageName in pinned

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(
                            if (isPinMode && isPinned) colors.highlightBar.copy(alpha = 0.22f)
                            else Color.Transparent
                        )
                        .clickable(
                            indication        = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            if (isPinMode) onToggle!!(app.packageName)
                            else onSelect!!(app.packageName)
                        }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (app.icon != null) {
                        Image(
                            bitmap             = app.icon,
                            contentDescription = null,
                            modifier           = Modifier.size(26.dp),
                            colorFilter        = ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
                                0.262f, 0.513f, 0.126f, 0f,  0f,
                                0.233f, 0.457f, 0.112f, 0f, 67.9f,
                                0.181f, 0.356f, 0.087f, 0f, 28.3f,
                                0f,     0f,     0f,     1f,  0f
                            )))
                        )
                    } else {
                        Spacer(Modifier.size(26.dp))
                    }

                    Spacer(Modifier.width(12.dp))

                    WingmanText(
                        text     = app.label,
                        style    = typo.body,
                        color    = if (isPinMode && isPinned) colors.primary else colors.secondary,
                        modifier = Modifier.weight(1f)
                    )

                    if (isPinMode) {
                        WingmanText(
                            text  = if (isPinned) "[X]" else "[ ]",
                            style = typo.caption,
                            color = if (isPinned) colors.primary else colors.dimText
                        )
                    }
                }

                Spacer(Modifier.fillMaxWidth().height(1.dp).background(colors.dimText.copy(alpha = 0.3f)))
            }
        }
    }
}
