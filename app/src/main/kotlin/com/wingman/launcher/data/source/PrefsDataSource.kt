package com.wingman.launcher.data.source

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wingman.launcher.data.model.DisplaySettings
import com.wingman.launcher.data.model.SettingsState
import com.wingman.launcher.data.model.ThemeVariant
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "wingman_settings")

/**
 * DataStore-backed preferences source for persisted settings.
 * Wraps DataStore<Preferences> and exposes a typed Flow<SettingsState>.
 *
 * Keys are stable string constants; changing them would invalidate stored prefs.
 */
@Singleton
class PrefsDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val KEY_EFFECT_INTENSITY = floatPreferencesKey("effect_intensity")
        private val KEY_THEME_VARIANT    = stringPreferencesKey("theme_variant")
        private val KEY_PINNED_APPS      = stringSetPreferencesKey("pinned_apps")
        // Display calibration
        private val KEY_TOP_H   = floatPreferencesKey("disp_top_h")
        private val KEY_TOP_PAD = floatPreferencesKey("disp_top_pad")
        private val KEY_TOP_OFF = floatPreferencesKey("disp_top_off")
        private val KEY_BOT_H   = floatPreferencesKey("disp_bot_h")
        private val KEY_BOT_PAD = floatPreferencesKey("disp_bot_pad")
        private val KEY_BOT_OFF = floatPreferencesKey("disp_bot_off")
    }

    /**
     * Emits the current [SettingsState] and subsequent updates whenever prefs change.
     * Falls back to [SettingsState.Default] values for missing keys.
     */
    val settingsFlow: Flow<SettingsState> = context.dataStore.data.map { prefs ->
        val intensity = prefs[KEY_EFFECT_INTENSITY] ?: SettingsState.Default.effectIntensity
        val variantName = prefs[KEY_THEME_VARIANT] ?: SettingsState.Default.themeVariant.name
        val variant = runCatching { ThemeVariant.valueOf(variantName) }
            .getOrDefault(ThemeVariant.STANDARD)
        SettingsState(
            effectIntensity = intensity.coerceIn(0f, 1f),
            themeVariant = variant
        )
    }

    /**
     * Persists a new effect intensity value. Value is clamped to 0f..1f before write.
     */
    suspend fun updateEffectIntensity(value: Float) {
        context.dataStore.edit { prefs ->
            prefs[KEY_EFFECT_INTENSITY] = value.coerceIn(0f, 1f)
        }
    }

    /**
     * Persists the selected theme variant by its enum name.
     */
    suspend fun updateThemeVariant(variant: ThemeVariant) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_VARIANT] = variant.name
        }
    }

    val pinnedAppsFlow: kotlinx.coroutines.flow.Flow<Set<String>> =
        context.dataStore.data.map { prefs -> prefs[KEY_PINNED_APPS] ?: emptySet() }

    suspend fun setPinnedApps(packages: Set<String>) {
        context.dataStore.edit { prefs -> prefs[KEY_PINNED_APPS] = packages }
    }

    private val _defaults = DisplaySettings()

    val displaySettingsFlow: Flow<DisplaySettings> = context.dataStore.data.map { p ->
        DisplaySettings(
            topHeightFrac = p[KEY_TOP_H]   ?: _defaults.topHeightFrac,
            topSidePadDp  = p[KEY_TOP_PAD] ?: _defaults.topSidePadDp,
            topOffsetDp   = p[KEY_TOP_OFF] ?: _defaults.topOffsetDp,
            botHeightFrac = p[KEY_BOT_H]   ?: _defaults.botHeightFrac,
            botSidePadDp  = p[KEY_BOT_PAD] ?: _defaults.botSidePadDp,
            botOffsetDp   = p[KEY_BOT_OFF] ?: _defaults.botOffsetDp
        )
    }

    private val KEY_ICON_PACK = stringPreferencesKey("icon_pack_package")

    val iconPackFlow: Flow<String?> =
        context.dataStore.data.map { it[KEY_ICON_PACK] }

    suspend fun setIconPack(packageName: String?) {
        context.dataStore.edit { prefs ->
            if (packageName != null) prefs[KEY_ICON_PACK] = packageName
            else prefs.remove(KEY_ICON_PACK)
        }
    }

    fun linkedAppFlow(itemId: String): Flow<String?> =
        context.dataStore.data.map { prefs ->
            prefs[stringPreferencesKey("linked_app_${itemId.lowercase()}")]
        }

    suspend fun setLinkedApp(itemId: String, packageName: String) {
        context.dataStore.edit { prefs ->
            prefs[stringPreferencesKey("linked_app_${itemId.lowercase()}")] = packageName
        }
    }

    suspend fun clearLinkedApp(itemId: String) {
        context.dataStore.edit { prefs ->
            prefs.remove(stringPreferencesKey("linked_app_${itemId.lowercase()}"))
        }
    }

    suspend fun updateDisplaySettings(ds: DisplaySettings) {
        context.dataStore.edit { p ->
            p[KEY_TOP_H]   = ds.topHeightFrac
            p[KEY_TOP_PAD] = ds.topSidePadDp
            p[KEY_TOP_OFF] = ds.topOffsetDp
            p[KEY_BOT_H]   = ds.botHeightFrac
            p[KEY_BOT_PAD] = ds.botSidePadDp
            p[KEY_BOT_OFF] = ds.botOffsetDp
        }
    }
}
