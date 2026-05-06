package com.wingman.launcher.data.model

// ── Scans ─────────────────────────────────────────────────────────────────────
sealed class ScansUiState {
    object Loading : ScansUiState()
    data class Success(val files: List<ScanFile>) : ScansUiState()
    data class Error(val message: String) : ScansUiState()
}

// ── Organizer ─────────────────────────────────────────────────────────────────
data class OrganizerUiState(
    val notes: List<Note>,
    val tasks: List<Task>,
    val isLoading: Boolean
) {
    companion object {
        val Empty = OrganizerUiState(
            notes     = emptyList(),
            tasks     = emptyList(),
            isLoading = true
        )
    }
}

// ── Home ──────────────────────────────────────────────────────────────────────
data class HomeUiState(
    val menuItems: List<MenuItem>,
    val systemStatus: SystemStatus,
    val pinnedApps: List<AppShortcut> = emptyList(),
    val installedApps: List<AppShortcut> = emptyList(),
    val isPickerOpen: Boolean = false,
    val displaySettings: DisplaySettings = DisplaySettings(),
    val linkedApps: Map<String, String> = emptyMap(),
    val showPickerFor: String? = null,
    val isAppDrawerOpen: Boolean = false
)
