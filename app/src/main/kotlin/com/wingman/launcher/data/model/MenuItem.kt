package com.wingman.launcher.data.model

import com.wingman.launcher.ui.navigation.AppDestination

/**
 * Represents one entry in the main Home menu.
 * The list of five items is fixed and provided by HomeViewModel.
 */
data class MenuItem(
    val id: String,               // e.g. "SCANS", "ORGANIZER"
    val label: String,            // display label (always uppercase)
    val iconType: PixelIconType,
    val destination: AppDestination
)

/**
 * Maps to a Canvas-drawn icon in the PixelIcon composable.
 * No bitmap or vector drawable is used -- each value is drawn procedurally.
 */
enum class PixelIconType {
    RADAR,
    FOLDER,
    BOOK,
    MUSIC_NOTE,
    GEAR,
    GRID,
    CAMERA
}
