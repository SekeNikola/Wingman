package com.wingman.launcher.data.model

/**
 * The 5 main menu sections. Index maps directly to NavigationState.Home.selectedIndex.
 */
enum class MenuSection(val label: String, val index: Int) {
    SCANS("SCANS", 0),
    ORGANIZER("ORGANIZER", 1),
    TUTORIALS("TUTORIALS", 2),
    MUSIC("MUSIC", 3),
    SETTINGS("SETTINGS", 4);

    companion object {
        fun fromIndex(index: Int): MenuSection =
            entries.first { it.index == index }
    }
}
