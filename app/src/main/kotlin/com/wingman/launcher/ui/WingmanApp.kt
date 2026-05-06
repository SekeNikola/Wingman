package com.wingman.launcher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.wingman.launcher.R
import com.wingman.launcher.data.model.ThemeVariant
import com.wingman.launcher.ui.components.BootScreen
import com.wingman.launcher.ui.components.DisplayCalibrationOverlay
import com.wingman.launcher.ui.components.WingmanText
import com.wingman.launcher.ui.navigation.AppDestination
import com.wingman.launcher.ui.screen.HomeScreen
import com.wingman.launcher.ui.screen.MusicScreen
import com.wingman.launcher.ui.screen.OrganizerScreen
import com.wingman.launcher.ui.screen.ScansScreen
import com.wingman.launcher.ui.screen.SettingsScreen
import com.wingman.launcher.ui.screen.TutorialsScreen
import com.wingman.launcher.ui.theme.WingmanTheme
import com.wingman.launcher.util.WingmanConstants
import com.wingman.launcher.viewmodel.HomeViewModel
import com.wingman.launcher.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout

@Composable
fun WingmanApp(
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val settingsState   by settingsViewModel.settingsState.collectAsState()
    val displaySettings by settingsViewModel.displaySettings.collectAsState()
    val isHighContrast  = settingsState.themeVariant == ThemeVariant.HIGH_CONTRAST
    val effectIntensity = settingsState.effectIntensity

    val homeUiState  by homeViewModel.uiState.collectAsState()
    val systemStatus = homeUiState.systemStatus

    WingmanTheme(highContrast = isHighContrast) {
        val colors = WingmanTheme.colors

        var currentDestination by remember { mutableStateOf<AppDestination>(AppDestination.Boot) }
        var showModuleOverlay  by remember { mutableStateOf(false) }
        var showCalibration    by remember { mutableStateOf(false) }

        LaunchedEffect(showModuleOverlay) {
            if (showModuleOverlay) {
                delay(WingmanConstants.MODULE_TRANSITION_OVERLAY_MS.toLong())
                showModuleOverlay = false
            }
        }

        LaunchedEffect(Unit) {
            try {
                withTimeout(WingmanConstants.BOOT_DURATION_MS + 2000L) {
                    while (currentDestination == AppDestination.Boot) {
                        delay(100L)
                    }
                }
            } catch (_: kotlinx.coroutines.TimeoutCancellationException) {
                if (currentDestination == AppDestination.Boot) {
                    currentDestination = AppDestination.Home
                }
            }
        }

        BackHandler(
            enabled = currentDestination != AppDestination.Home &&
                      currentDestination != AppDestination.Boot
        ) {
            currentDestination = AppDestination.Home
        }

        val navigate: (AppDestination) -> Unit = { destination ->
            if (destination != AppDestination.Boot) {
                if (destination != AppDestination.Home) {
                    showModuleOverlay = true
                }
                currentDestination = destination
            }
        }

        // ── Root layer stack ─────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)   // fallback shown while image loads
        ) {
            // 1 — full-screen device reference image
            Image(
                painter          = painterResource(id = R.drawable.device_bg),
                contentDescription = null,
                contentScale     = ContentScale.FillBounds,
                modifier         = Modifier.fillMaxSize()
            )

            // 2 — uniform dark veil: keeps all text readable without killing image
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f))
            )

            // 3 — screen content (Home is transparent; sub-screens are opaque)
            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = {
                    // Skip animation when jumping to Home for calibration — Home should appear instantly
                    if (showCalibration) {
                        EnterTransition.None togetherWith ExitTransition.None
                    } else {
                        val enter = fadeIn(tween(WingmanConstants.SCREEN_TRANSITION_MS)) +
                            slideInVertically(tween(WingmanConstants.SCREEN_TRANSITION_MS)) { it / 8 }
                        val exit  = fadeOut(tween(WingmanConstants.SCREEN_TRANSITION_MS)) +
                            slideOutVertically(tween(WingmanConstants.SCREEN_TRANSITION_MS)) { -it / 8 }
                        enter togetherWith exit
                    }
                },
                label = "wingmanNav"
            ) { destination ->
                when (destination) {
                    AppDestination.Boot ->
                        BootScreen(
                            onBootComplete  = { currentDestination = AppDestination.Home },
                            effectIntensity = effectIntensity
                        )

                    AppDestination.Home ->
                        HomeScreen(
                            navigate        = navigate,
                            effectIntensity = effectIntensity
                        )

                    AppDestination.Scans ->
                        ScansScreen(
                            onBack          = { currentDestination = AppDestination.Home },
                            effectIntensity = effectIntensity,
                            systemStatus    = systemStatus
                        )

                    AppDestination.Organizer ->
                        OrganizerScreen(
                            onBack          = { currentDestination = AppDestination.Home },
                            effectIntensity = effectIntensity,
                            systemStatus    = systemStatus
                        )

                    AppDestination.Tutorials ->
                        TutorialsScreen(
                            onBack          = { currentDestination = AppDestination.Home },
                            effectIntensity = effectIntensity,
                            systemStatus    = systemStatus
                        )

                    AppDestination.Music ->
                        MusicScreen(
                            onBack          = { currentDestination = AppDestination.Home },
                            effectIntensity = effectIntensity,
                            systemStatus    = systemStatus
                        )

                    AppDestination.Settings ->
                        SettingsScreen(
                            onBack            = { currentDestination = AppDestination.Home },
                            effectIntensity   = effectIntensity,
                            systemStatus      = systemStatus,
                            settingsViewModel = settingsViewModel,
                            onOpenCalibration = {
                                currentDestination = AppDestination.Home
                                showCalibration    = true
                            }
                        )

                    // Handled internally by HomeScreen — currentDestination is never set to these
                    AppDestination.AppDrawer, AppDestination.Gallery -> Unit
                }
            }

            // 4 — calibration overlay (shown over Home so layout changes are visible live)
            if (showCalibration) {
                DisplayCalibrationOverlay(
                    initial   = displaySettings,
                    onUpdate  = { settingsViewModel.updateDisplaySettings(it) },
                    onDismiss = { showCalibration = false }
                )
            }

            // 5 — "ACCESSING MODULE..." transition flash
            AnimatedVisibility(
                visible = showModuleOverlay,
                enter   = fadeIn(tween(80)),
                exit    = fadeOut(tween(120))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.background.copy(alpha = 0.90f)),
                    contentAlignment = Alignment.Center
                ) {
                    WingmanText(
                        text  = "ACCESSING MODULE...",
                        style = WingmanTheme.typography.heading,
                        color = colors.primary
                    )
                }
            }
        }
    }
}
