package com.wingman.launcher.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.wingman.launcher.data.model.DisplaySettings
import com.wingman.launcher.ui.theme.WingmanTheme

@Composable
fun DisplayCalibrationOverlay(
    initial: DisplaySettings,
    onUpdate: (DisplaySettings) -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler { onDismiss() }

    var ds     by remember { mutableStateOf(initial) }
    var showTop by remember { mutableStateOf(true) }

    fun save(updated: DisplaySettings) {
        ds = updated
        onUpdate(updated)
    }

    val colors = WingmanTheme.colors
    val typo   = WingmanTheme.typography

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // Panel starts just below the middle of the screen
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = maxHeight * 0.52f)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.95f))
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // ── Header ───────────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                WingmanText("[ ADJUST SCREENS ]", style = typo.label, color = colors.primary)
                WingmanText(
                    text     = "[DONE]",
                    style    = typo.label,
                    color    = colors.secondary,
                    modifier = Modifier.clickable(
                        indication        = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onDismiss() }
                )
            }

            Spacer(Modifier.height(6.dp))

            // ── Tab switcher ─────────────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth()) {
                DisplayTab(
                    label      = "TOP DISPLAY",
                    isSelected = showTop,
                    modifier   = Modifier.weight(1f)
                ) { showTop = true }

                Spacer(Modifier.width(4.dp))

                DisplayTab(
                    label      = "BOTTOM DISPLAY",
                    isSelected = !showTop,
                    modifier   = Modifier.weight(1f)
                ) { showTop = false }
            }

            Spacer(Modifier.fillMaxWidth().height(1.dp).background(colors.primary.copy(0.30f)))
            Spacer(Modifier.height(8.dp))

            // ── Sliders for selected display ──────────────────────────────────
            if (showTop) {
                CalibSlider("HEIGHT",    ds.topHeightFrac, 0.20f, 0.75f, { "%.3f".format(it) })  { save(ds.copy(topHeightFrac = it)) }
                Spacer(Modifier.height(6.dp))
                CalibSlider("WIDTH PAD", ds.topSidePadDp,  0f,   120f,  { "%.1fdp".format(it) }) { save(ds.copy(topSidePadDp  = it)) }
                Spacer(Modifier.height(6.dp))
                CalibSlider("OFFSET Y",  ds.topOffsetDp,  -60f,  200f,  { "%.1fdp".format(it) }) { save(ds.copy(topOffsetDp   = it)) }
            } else {
                CalibSlider("HEIGHT",    ds.botHeightFrac, 0.05f, 0.40f, { "%.3f".format(it) })  { save(ds.copy(botHeightFrac = it)) }
                Spacer(Modifier.height(6.dp))
                CalibSlider("WIDTH PAD", ds.botSidePadDp,  0f,   120f,  { "%.1fdp".format(it) }) { save(ds.copy(botSidePadDp  = it)) }
                Spacer(Modifier.height(6.dp))
                CalibSlider("OFFSET Y",  ds.botOffsetDp,  -60f,  200f,  { "%.1fdp".format(it) }) { save(ds.copy(botOffsetDp   = it)) }
            }

            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun DisplayTab(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = WingmanTheme.colors
    val typo   = WingmanTheme.typography

    Box(
        modifier = modifier
            .background(
                if (isSelected) colors.primary.copy(alpha = 0.18f)
                else Color.Transparent
            )
            .clickable(
                indication        = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        WingmanText(
            text  = label,
            style = typo.caption,
            color = if (isSelected) colors.primary else colors.dimText
        )
    }
}

@Composable
private fun CalibSlider(
    label: String,
    value: Float,
    min: Float,
    max: Float,
    display: (Float) -> String,
    onChange: (Float) -> Unit
) {
    val colors       = WingmanTheme.colors
    val typo         = WingmanTheme.typography
    val currentValue by rememberUpdatedState(value)
    val fraction     = ((value - min) / (max - min)).coerceIn(0f, 1f)

    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WingmanText(label, style = typo.caption, color = colors.secondary, modifier = Modifier.width(62.dp))
        Spacer(Modifier.width(6.dp))

        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .weight(1f)
                .height(32.dp)
                .pointerInput(min, max) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        val oldFrac = (currentValue - min) / (max - min)
                        val newFrac = (oldFrac + dragAmount / size.width).coerceIn(0f, 1f)
                        onChange(min + newFrac * (max - min))
                    }
                }
        ) {
            val w = size.width
            val h = size.height
            drawRect(Color(0xFF111100))
            drawRect(colors.primary.copy(alpha = 0.38f), size = Size(w * fraction, h))
            val tx = (w * fraction).coerceIn(1.5f, w - 1.5f)
            drawLine(colors.primary, Offset(tx, 0f), Offset(tx, h), 3f)
        }

        Spacer(Modifier.width(6.dp))
        WingmanText(display(value), style = typo.caption, color = colors.primary, modifier = Modifier.width(54.dp))
    }
}
