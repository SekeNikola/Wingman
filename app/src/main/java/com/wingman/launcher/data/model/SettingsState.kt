package com.wingman.launcher.data.model

/**
 * Persisted user preferences. Stored via DataStore Preferences.
 * Observed globally — changes update all active screens immediately.
 *
 * themeIntensity: 0.0 = no effects, 1.0 = full effects
 * username: displayed in TopBar
 */
data class SettingsState(
    val effectsEnabled: Boolean = true,
    val glowEnabled: Boolean = true,
    val flickerEnabled: Boolean = false,
    val themeIntensity: Float = 0.7f,       // 0.0–1.0
    val soundEnabled: Boolean = true,
    val username: String = "WARREN"
)
