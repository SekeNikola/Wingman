package com.wingman.launcher.data.model

/**
 * Single sealed class representing every possible navigation state.
 * MainViewModel holds a StateFlow<NavigationState> — no back stack library needed.
 *
 * Transitions:
 *   Boot → Home(0)         : auto on boot animation complete
 *   Home + Enter           : navigate to section by selectedIndex
 *   Section + Back         : Home(lastIndex)
 *   Home + Back            : no-op (launcher root, swallow)
 */
sealed class NavigationState {
    object Boot : NavigationState()
    data class Home(val selectedIndex: Int = 0) : NavigationState()
    object Scans : NavigationState()
    object Organizer : NavigationState()
    object Tutorials : NavigationState()
    object Music : NavigationState()
    object Settings : NavigationState()
}
