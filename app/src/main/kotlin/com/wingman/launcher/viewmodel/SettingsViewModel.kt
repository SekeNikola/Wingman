package com.wingman.launcher.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wingman.launcher.data.model.AppShortcut
import com.wingman.launcher.data.model.DisplaySettings
import com.wingman.launcher.data.model.SettingsState
import com.wingman.launcher.data.model.ThemeVariant
import com.wingman.launcher.data.repository.SettingsRepository
import com.wingman.launcher.data.source.PrefsDataSource
import com.wingman.launcher.util.IconPackLoader
import com.wingman.launcher.util.toImageBitmap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private val LINKABLE_IDS = listOf("ORGANIZER", "MUSIC")

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val prefs: PrefsDataSource,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _settingsState   = MutableStateFlow(SettingsState.Default)
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

    private val _displaySettings = MutableStateFlow(DisplaySettings())
    val displaySettings: StateFlow<DisplaySettings> = _displaySettings.asStateFlow()

    private val _iconPacks        = MutableStateFlow<List<AppShortcut>>(emptyList())
    val iconPacks: StateFlow<List<AppShortcut>> = _iconPacks.asStateFlow()

    private val _selectedIconPack = MutableStateFlow<String?>(null)
    val selectedIconPack: StateFlow<String?> = _selectedIconPack.asStateFlow()

    // Map of itemId -> saved default package name
    private val _linkedApps = MutableStateFlow<Map<String, String?>>(emptyMap())
    val linkedApps: StateFlow<Map<String, String?>> = _linkedApps.asStateFlow()

    private val _installedApps = MutableStateFlow<List<AppShortcut>>(emptyList())
    val installedApps: StateFlow<List<AppShortcut>> = _installedApps.asStateFlow()

    init {
        viewModelScope.launch {
            repository.settingsFlow.collect { settings -> _settingsState.value = settings }
        }
        viewModelScope.launch {
            prefs.displaySettingsFlow.collect { ds -> _displaySettings.value = ds }
        }
        viewModelScope.launch {
            prefs.iconPackFlow.collect { pkg -> _selectedIconPack.value = pkg }
        }
        viewModelScope.launch {
            _iconPacks.value = IconPackLoader.findIconPacks(context.packageManager)
        }
        LINKABLE_IDS.forEach { id ->
            viewModelScope.launch {
                prefs.linkedAppFlow(id).collect { pkg ->
                    _linkedApps.value = _linkedApps.value + (id to pkg)
                }
            }
        }
        viewModelScope.launch { loadInstalledApps() }
    }

    private fun loadInstalledApps() {
        val pm     = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps   = pm.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != context.packageName }
            .map { ri ->
                AppShortcut(
                    packageName = ri.activityInfo.packageName,
                    label       = ri.loadLabel(pm).toString(),
                    icon        = runCatching { ri.loadIcon(pm).toImageBitmap() }.getOrNull()
                )
            }
            .sortedBy { it.label.lowercase() }
        _installedApps.value = apps
    }

    fun updateEffectIntensity(value: Float) {
        viewModelScope.launch { repository.updateEffectIntensity(value.coerceIn(0f, 1f)) }
    }

    fun updateThemeVariant(variant: ThemeVariant) {
        viewModelScope.launch { repository.updateThemeVariant(variant) }
    }

    fun updateDisplaySettings(ds: DisplaySettings) {
        viewModelScope.launch { prefs.updateDisplaySettings(ds) }
    }

    fun setIconPack(packageName: String?) {
        viewModelScope.launch { prefs.setIconPack(packageName) }
    }

    fun setDefaultApp(itemId: String, packageName: String?) {
        viewModelScope.launch {
            if (packageName != null) prefs.setLinkedApp(itemId, packageName)
            else prefs.clearLinkedApp(itemId)
        }
    }
}
