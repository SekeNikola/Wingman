package com.wingman.launcher.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wingman.launcher.data.model.SettingsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DataStore singleton scoped to application Context
private val Context.dataStore by preferencesDataStore(name = "wingman_settings")

/**
 * Persists SettingsState via DataStore Preferences.
 * Each field maps to a typed key. Defaults match SettingsState defaults.
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val EFFECTS_ENABLED = booleanPreferencesKey("effects_enabled")
        val GLOW_ENABLED = booleanPreferencesKey("glow_enabled")
        val FLICKER_ENABLED = booleanPreferencesKey("flicker_enabled")
        val THEME_INTENSITY = floatPreferencesKey("theme_intensity")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val USERNAME = stringPreferencesKey("username")
    }

    val settings: Flow<SettingsState> = context.dataStore.data.map { prefs ->
        SettingsState(
            effectsEnabled = prefs[Keys.EFFECTS_ENABLED] ?: true,
            glowEnabled = prefs[Keys.GLOW_ENABLED] ?: true,
            flickerEnabled = prefs[Keys.FLICKER_ENABLED] ?: false,
            themeIntensity = prefs[Keys.THEME_INTENSITY] ?: 0.7f,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            username = prefs[Keys.USERNAME] ?: "WARREN"
        )
    }

    suspend fun setEffectsEnabled(value: Boolean) {
        context.dataStore.edit { it[Keys.EFFECTS_ENABLED] = value }
    }

    suspend fun setGlowEnabled(value: Boolean) {
        context.dataStore.edit { it[Keys.GLOW_ENABLED] = value }
    }

    suspend fun setFlickerEnabled(value: Boolean) {
        context.dataStore.edit { it[Keys.FLICKER_ENABLED] = value }
    }

    suspend fun setThemeIntensity(value: Float) {
        context.dataStore.edit { it[Keys.THEME_INTENSITY] = value.coerceIn(0f, 1f) }
    }

    suspend fun setSoundEnabled(value: Boolean) {
        context.dataStore.edit { it[Keys.SOUND_ENABLED] = value }
    }

    suspend fun setUsername(value: String) {
        context.dataStore.edit { it[Keys.USERNAME] = value }
    }
}
