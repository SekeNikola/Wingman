package com.wingman.launcher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wingman.launcher.data.model.InputEvent
import com.wingman.launcher.data.model.MenuSection
import com.wingman.launcher.data.model.NavigationState
import com.wingman.launcher.data.model.SettingsState
import com.wingman.launcher.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Root ViewModel. Owns navigation state and global settings state.
 *
 * Navigation rules (from ARCHITECTURE.md):
 *   Boot   → Home(0)          : onBootComplete()
 *   Home   + DPAD_UP          : selectedIndex - 1, wraps (0 → 4)
 *   Home   + DPAD_DOWN        : selectedIndex + 1, wraps (4 → 0)
 *   Home   + Enter            : navigate to section by selectedIndex
 *   Home   + Back             : no-op (launcher root)
 *   Section + Back            : Home(lastIndex)
 *   Section + DPAD/Enter      : delegated to section ViewModel
 *
 */
class MainViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // -------------------------------------------------------------------------
    // Navigation state
    // -------------------------------------------------------------------------

    private val _navState = MutableStateFlow<NavigationState>(NavigationState.Boot)
    val navState: StateFlow<NavigationState> = _navState.asStateFlow()

    // Tracks last Home index so Back from a section restores position
    private var lastHomeIndex: Int = 0

    // Total menu items — used for wrapping
    private val menuCount = MenuSection.entries.size   // 5

    // -------------------------------------------------------------------------
    // Settings — shared with all screens
    // -------------------------------------------------------------------------

    val settings: StateFlow<SettingsState> = settingsRepository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SettingsState()
        )

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    fun onBootComplete() {
        _navState.value = NavigationState.Home(0)
        lastHomeIndex = 0
    }

    fun onInputEvent(event: InputEvent) {
        val current = _navState.value

        when (current) {
            is NavigationState.Boot -> {
                // No input during boot — suppress everything
            }

            is NavigationState.Home -> handleHomeInput(current, event)

            is NavigationState.Scans,
            is NavigationState.Organizer,
            is NavigationState.Tutorials,
            is NavigationState.Music,
            is NavigationState.Settings -> handleSectionInput(event)
        }
    }

    fun navigateTo(state: NavigationState) {
        _navState.value = state
    }

    fun navigateBack() {
        val current = _navState.value
        if (current is NavigationState.Home) return  // launcher root — swallow

        _navState.value = NavigationState.Home(lastHomeIndex)
    }

    // -------------------------------------------------------------------------
    // Touch interaction helpers (called by HomeScreen gestures)
    // -------------------------------------------------------------------------

    /** Touch: change highlighted index without navigating (swipe gesture). */
    fun onSelectionChanged(index: Int) {
        val current = _navState.value
        if (current is NavigationState.Home) {
            lastHomeIndex = index
            _navState.update { NavigationState.Home(index) }
        }
    }

    /** Touch: tap on a row directly triggers section entry. */
    fun onSectionSelected(section: MenuSection) {
        lastHomeIndex = section.index
        _navState.value = sectionToNavState(section)
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private fun handleHomeInput(current: NavigationState.Home, event: InputEvent) {
        when (event) {
            is InputEvent.DpadUp, is InputEvent.ScrollUp -> {
                val newIndex = (current.selectedIndex - 1 + menuCount) % menuCount
                lastHomeIndex = newIndex
                _navState.update { NavigationState.Home(newIndex) }
            }

            is InputEvent.DpadDown, is InputEvent.ScrollDown -> {
                val newIndex = (current.selectedIndex + 1) % menuCount
                lastHomeIndex = newIndex
                _navState.update { NavigationState.Home(newIndex) }
            }

            is InputEvent.Enter -> {
                val section = MenuSection.fromIndex(current.selectedIndex)
                _navState.value = sectionToNavState(section)
            }

            is InputEvent.Back -> {
                // No-op — launcher root cannot go back
            }

            else -> Unit
        }
    }

    // Optional: called by WingmanApp to route events to the active section ViewModel.
    // Returns true if the event was consumed (e.g. Tutorials detail-view Back).
    // Set by WingmanApp via DisposableEffect when a section screen becomes active.
    var sectionInputDispatcher: ((InputEvent) -> Boolean)? = null

    private fun handleSectionInput(event: InputEvent) {
        // Let the section ViewModel try to consume the event first (e.g. Tutorials detail Back)
        val consumed = sectionInputDispatcher?.invoke(event) ?: false
        if (consumed) return

        // If Back was not consumed by section, return to Home
        if (event is InputEvent.Back) {
            navigateBack()
        }
    }

    private fun sectionToNavState(section: MenuSection): NavigationState = when (section) {
        MenuSection.SCANS -> NavigationState.Scans
        MenuSection.ORGANIZER -> NavigationState.Organizer
        MenuSection.TUTORIALS -> NavigationState.Tutorials
        MenuSection.MUSIC -> NavigationState.Music
        MenuSection.SETTINGS -> NavigationState.Settings
    }

}
