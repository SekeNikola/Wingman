package com.wingman.launcher.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wingman.launcher.data.model.NavigationState
import com.wingman.launcher.ui.screens.BootScreen
import com.wingman.launcher.ui.screens.HomeScreen
import com.wingman.launcher.ui.screens.MusicScreen
import com.wingman.launcher.ui.screens.OrganizerScreen
import com.wingman.launcher.ui.screens.ScansScreen
import com.wingman.launcher.ui.screens.SettingsScreen
import com.wingman.launcher.ui.screens.TutorialsScreen
import com.wingman.launcher.ui.theme.WingmanTheme
import com.wingman.launcher.viewmodel.MainViewModel
import com.wingman.launcher.viewmodel.MusicViewModel
import com.wingman.launcher.viewmodel.OrganizerViewModel
import com.wingman.launcher.viewmodel.ScansViewModel
import com.wingman.launcher.viewmodel.SettingsViewModel
import com.wingman.launcher.viewmodel.TutorialsViewModel

/**
 * Root composable. Collects navState from MainViewModel and renders the
 * correct screen via a when expression. No Compose Navigation, no NavHost.
 *
 * WingmanTheme wraps all content. Section ViewModels are created once by
 * the Compose runtime and survive navigation (ViewModel lifecycle).
 *
 * sectionInputDispatcher: set on MainViewModel so hardware key events dispatched
 * to MainViewModel.onInputEvent can be forwarded to the active section ViewModel.
 */
@Composable
fun WingmanApp(
    mainViewModel: MainViewModel,
    settingsViewModel: SettingsViewModel,
    scansViewModel: ScansViewModel = viewModel(),
    organizerViewModel: OrganizerViewModel = viewModel(),
    tutorialsViewModel: TutorialsViewModel = viewModel(),
    musicViewModel: MusicViewModel = viewModel()
) {
    val navState by mainViewModel.navState.collectAsState()
    val settings by mainViewModel.settings.collectAsState()

    WingmanTheme {
        when (val nav = navState) {
            is NavigationState.Boot -> {
                // No section dispatcher during boot
                DisposableEffect(nav) {
                    mainViewModel.sectionInputDispatcher = null
                    onDispose { mainViewModel.sectionInputDispatcher = null }
                }
                BootScreen(
                    onBootComplete = { mainViewModel.onBootComplete() },
                    settings = settings
                )
            }

            is NavigationState.Home -> {
                DisposableEffect(nav) {
                    mainViewModel.sectionInputDispatcher = null
                    onDispose { mainViewModel.sectionInputDispatcher = null }
                }
                HomeScreen(
                    selectedIndex = nav.selectedIndex,
                    settings = settings,
                    onSectionSelected = { section -> mainViewModel.onSectionSelected(section) },
                    onSelectionChanged = { index -> mainViewModel.onSelectionChanged(index) }
                )
            }

            is NavigationState.Scans -> {
                DisposableEffect(nav) {
                    mainViewModel.sectionInputDispatcher = { event ->
                        scansViewModel.onInputEvent(event, mainViewModel.settings.value)
                        false // Scans never consumes Back — let MainViewModel handle it
                    }
                    onDispose { mainViewModel.sectionInputDispatcher = null }
                }
                ScansScreen(
                    viewModel = scansViewModel,
                    settings = settings,
                    onBack = { mainViewModel.navigateBack() }
                )
            }

            is NavigationState.Organizer -> {
                DisposableEffect(nav) {
                    mainViewModel.sectionInputDispatcher = { event ->
                        organizerViewModel.onInputEvent(event, mainViewModel.settings.value)
                        false // Organizer never consumes Back
                    }
                    onDispose { mainViewModel.sectionInputDispatcher = null }
                }
                OrganizerScreen(
                    viewModel = organizerViewModel,
                    settings = settings,
                    onBack = { mainViewModel.navigateBack() }
                )
            }

            is NavigationState.Tutorials -> {
                DisposableEffect(nav) {
                    mainViewModel.sectionInputDispatcher = { event ->
                        // TutorialsViewModel.onInputEvent returns true if consumed (Back in detail)
                        tutorialsViewModel.onInputEvent(event, mainViewModel.settings.value)
                    }
                    onDispose { mainViewModel.sectionInputDispatcher = null }
                }
                TutorialsScreen(
                    viewModel = tutorialsViewModel,
                    settings = settings,
                    onBack = { mainViewModel.navigateBack() }
                )
            }

            is NavigationState.Music -> {
                DisposableEffect(nav) {
                    mainViewModel.sectionInputDispatcher = { event ->
                        musicViewModel.onInputEvent(event, mainViewModel.settings.value)
                        false // Music never consumes Back
                    }
                    onDispose { mainViewModel.sectionInputDispatcher = null }
                }
                MusicScreen(
                    viewModel = musicViewModel,
                    settings = settings,
                    onBack = { mainViewModel.navigateBack() }
                )
            }

            is NavigationState.Settings -> {
                DisposableEffect(nav) {
                    mainViewModel.sectionInputDispatcher = { event ->
                        settingsViewModel.onInputEvent(event)
                        false // Settings never consumes Back
                    }
                    onDispose { mainViewModel.sectionInputDispatcher = null }
                }
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBack = { mainViewModel.navigateBack() }
                )
            }
        }
    }
}
