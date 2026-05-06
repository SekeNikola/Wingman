package com.wingman.launcher.ui.navigation

/**
 * Navigation graph expressed as a sealed class.
 * WingmanApp holds a MutableStateFlow<AppDestination> initialized to Boot.
 * No Jetpack Navigation component is used — a when() expression drives rendering.
 *
 * Transition rules:
 *  - Boot can only transition to Home (one-way; no back stack from Boot).
 *  - All sub-screens (Scans, Organizer, etc.) navigate back to Home via BackHandler.
 *  - Home does not exit the app on Back — launcher apps must stay alive.
 */
sealed class AppDestination {
    object Boot       : AppDestination()
    object Home       : AppDestination()
    object Scans      : AppDestination()
    object Organizer  : AppDestination()
    object Tutorials  : AppDestination()
    object Music      : AppDestination()
    object Settings   : AppDestination()
    object AppDrawer  : AppDestination()
    object Gallery    : AppDestination()
}
