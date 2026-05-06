package com.wingman.launcher.data.model

/**
 * Possible visual theme variants.
 */
enum class ThemeVariant {
    STANDARD,
    HIGH_CONTRAST
}

/**
 * Domain model for persisted user settings.
 *
 *  effectIntensity -- 0.0f (all effects off) to 1.0f (maximum intensity).
 *    Controls scanlines, noise grain, and glow alpha simultaneously.
 *  themeVariant    -- which color palette to use.
 *
 * Use SettingsState.Companion.Default for the initial/default state.
 */
data class SettingsState(
    val effectIntensity: Float,
    val themeVariant: ThemeVariant
) {
    companion object {
        val Default = SettingsState(
            effectIntensity = 0.5f,
            themeVariant = ThemeVariant.STANDARD
        )
    }
}
