package com.wingman.launcher.ui.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wingman.launcher.data.model.AppShortcut
import com.wingman.launcher.data.model.MenuItem
import com.wingman.launcher.ui.components.AppPickerOverlay
import com.wingman.launcher.ui.components.MenuRow
import com.wingman.launcher.ui.components.NoiseOverlay
import com.wingman.launcher.ui.components.ScanlineOverlay
import com.wingman.launcher.ui.components.TopBar
import com.wingman.launcher.ui.components.WingmanText
import com.wingman.launcher.ui.input.InputEvent
import com.wingman.launcher.ui.input.handleWingmanInput
import com.wingman.launcher.ui.input.rememberInputHandler
import com.wingman.launcher.ui.navigation.AppDestination
import com.wingman.launcher.ui.theme.WingmanTheme
import com.wingman.launcher.util.WingmanConstants
import com.wingman.launcher.viewmodel.HomeViewModel

private val DisplayBg = Color.Black.copy(alpha = 0.76f)

@Composable
fun HomeScreen(
    navigate: (AppDestination) -> Unit,
    effectIntensity: Float,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState    by viewModel.uiState.collectAsState()
    val menuItems  = uiState.menuItems
    val pinnedApps = uiState.pinnedApps
    val ds         = uiState.displaySettings

    var selectedIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) { viewModel.onResume() }
    DisposableEffect(Unit) { onDispose { viewModel.onPause() } }

    val infiniteTransition = rememberInfiniteTransition(label = "flicker")
    val flickerAlpha by infiniteTransition.animateFloat(
        initialValue  = WingmanConstants.FLICKER_ALPHA_MIN,
        targetValue   = WingmanConstants.FLICKER_ALPHA_MAX,
        animationSpec = infiniteRepeatable(
            animation  = tween(WingmanConstants.FLICKER_PERIOD_MIN_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flickerAlpha"
    )

    fun activateItem(item: MenuItem) {
        viewModel.onSelectEvent()
        when (item.id) {
            "SCANS"      -> viewModel.openCamera()
            "ORGANIZER"  -> viewModel.openAppDrawer()
            "FILES"      -> viewModel.openGallery()
            in HomeViewModel.LINKABLE -> {
                val linked = uiState.linkedApps[item.id]
                if (linked != null) viewModel.launchApp(linked)
                else viewModel.openPickerFor(item.id)
            }
            else -> navigate(item.destination)
        }
    }

    val handleInput = rememberInputHandler { event ->
        when (event) {
            is InputEvent.Up   -> { selectedIndex = (selectedIndex - 1).coerceAtLeast(0); viewModel.onScrollEvent() }
            is InputEvent.Down -> { selectedIndex = (selectedIndex + 1).coerceAtMost(menuItems.size - 1); viewModel.onScrollEvent() }
            is InputEvent.Select -> { if (menuItems.isNotEmpty()) activateItem(menuItems[selectedIndex]) }
            is InputEvent.Back   -> { /* launcher does not exit */ }
        }
    }

    val colors = WingmanTheme.colors
    val typo   = WingmanTheme.typography

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .alpha(flickerAlpha)
            .handleWingmanInput(handleInput)
    ) {
        val screenH = maxHeight

        Column(modifier = Modifier.fillMaxSize()) {

            // Vertical offset — pushes the top display down from the screen top
            if (ds.topOffsetDp > 0f) Spacer(modifier = Modifier.height(ds.topOffsetDp.dp))

            // ── TOP DISPLAY ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenH * ds.topHeightFrac)
                    .padding(horizontal = ds.topSidePadDp.dp)
                    .background(DisplayBg)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopBar(status = uiState.systemStatus)

                    Spacer(modifier = Modifier.height(6.dp))

                    WingmanText(
                        text     = "// MAIN MENU",
                        style    = typo.caption,
                        color    = colors.dimText,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(menuItems, key = { _, item -> item.id }) { index, item ->
                            MenuRow(
                                label           = item.label,
                                iconType        = item.iconType,
                                isSelected      = index == selectedIndex,
                                effectIntensity = effectIntensity,
                                onClick = {
                                    if (selectedIndex == index) activateItem(item)
                                    else selectedIndex = index
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // ── CHROME GAP ────────────────────────────────────────────────────
            Spacer(modifier = Modifier.weight(1f))

            // ── BOTTOM DISPLAY ────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenH * ds.botHeightFrac)
                    .padding(horizontal = ds.botSidePadDp.dp)
                    .background(DisplayBg)
                    .padding(vertical = 6.dp)
            ) {
                WingmanText(text = "// APPS", style = typo.caption, color = colors.dimText)

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier          = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        modifier              = Modifier.weight(1f).fillMaxHeight(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        items(pinnedApps, key = { it.packageName }) { app ->
                            AppDockIcon(app = app, onClick = { viewModel.launchApp(app.packageName) })
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(colors.background.copy(alpha = 0.5f))
                            .clickable(
                                indication        = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { viewModel.openPicker() },
                        contentAlignment = Alignment.Center
                    ) {
                        WingmanText(text = "+", style = typo.heading, color = colors.primary)
                    }
                }
            }

            // Bottom offset — distance between bottom display and screen edge
            if (ds.botOffsetDp > 0f) Spacer(modifier = Modifier.height(ds.botOffsetDp.dp))
        }

        ScanlineOverlay(intensity = effectIntensity)
        NoiseOverlay(intensity = effectIntensity)

        if (uiState.isPickerOpen) {
            AppPickerOverlay(
                apps      = uiState.installedApps,
                title     = "// PIN APPS",
                pinned    = uiState.pinnedApps.map { it.packageName }.toSet(),
                onToggle  = { pkg -> viewModel.togglePin(pkg) },
                onDismiss = { viewModel.closePicker() }
            )
        }

        if (uiState.isAppDrawerOpen) {
            AppPickerOverlay(
                apps      = uiState.installedApps,
                title     = "// ORGANIZER",
                onSelect  = { pkg -> viewModel.launchApp(pkg); viewModel.closeAppDrawer() },
                onDismiss = { viewModel.closeAppDrawer() }
            )
        }

        val pickFor = uiState.showPickerFor
        if (pickFor != null) {
            val pickerTitle = when (pickFor) {
                "ORGANIZER" -> "// SET DEFAULT: ORGANIZER"
                "MUSIC"     -> "// SET DEFAULT: MUSIC"
                "APPS"      -> "// SET DEFAULT: APPS"
                else        -> "// SELECT APP"
            }
            AppPickerOverlay(
                apps      = uiState.installedApps,
                title     = pickerTitle,
                onSelect  = { pkg -> viewModel.linkAndLaunch(pickFor, pkg) },
                onDismiss = { viewModel.closePickerFor() }
            )
        }
    }
}

// ── Bottom dock icon ──────────────────────────────────────────────────────────

@Composable
private fun AppDockIcon(app: AppShortcut, onClick: () -> Unit) {
    val colors = WingmanTheme.colors
    val typo   = WingmanTheme.typography

    Column(
        modifier = Modifier
            .width(88.dp)
            .clickable(
                indication        = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (app.icon != null) {
            Image(
                bitmap             = app.icon,
                contentDescription = app.label,
                modifier           = Modifier.size(56.dp),
                colorFilter        = ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
                    0.262f, 0.513f, 0.126f, 0f,  0f,
                    0.233f, 0.457f, 0.112f, 0f, 67.9f,
                    0.181f, 0.356f, 0.087f, 0f, 28.3f,
                    0f,     0f,     0f,     1f,  0f
                )))
            )
        } else {
            Box(
                modifier         = Modifier.size(56.dp).background(colors.dimText),
                contentAlignment = Alignment.Center
            ) {
                WingmanText(text = "?", style = typo.caption, color = colors.secondary)
            }
        }
        Spacer(Modifier.height(4.dp))
        WingmanText(text = app.label.take(9), style = typo.caption, color = colors.secondary)
    }
}
