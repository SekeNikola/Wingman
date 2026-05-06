package com.wingman.launcher.viewmodel

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wingman.launcher.data.model.AppShortcut
import com.wingman.launcher.data.model.HomeUiState
import com.wingman.launcher.data.model.MenuItem
import com.wingman.launcher.data.model.PixelIconType
import com.wingman.launcher.data.model.SystemStatus
import com.wingman.launcher.data.source.PrefsDataSource
import com.wingman.launcher.ui.navigation.AppDestination
import com.wingman.launcher.util.AudioPlayer
import com.wingman.launcher.util.IconPackLoader
import com.wingman.launcher.util.WingmanConstants
import com.wingman.launcher.util.toImageBitmap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioPlayer: AudioPlayer,
    private val prefs: PrefsDataSource
) : ViewModel() {

    companion object {
        private val MENU_ITEMS = listOf(
            MenuItem("SCANS",     "SCANS",     PixelIconType.RADAR,      AppDestination.Scans),
            MenuItem("ORGANIZER", "ORGANIZER", PixelIconType.FOLDER,     AppDestination.Organizer),
            MenuItem("TUTORIALS", "TUTORIALS", PixelIconType.BOOK,       AppDestination.Tutorials),
            MenuItem("MUSIC",     "MUSIC",     PixelIconType.MUSIC_NOTE, AppDestination.Music),
            MenuItem("APPS",      "APPS",      PixelIconType.GRID,       AppDestination.AppDrawer),
            MenuItem("FILES",     "FILES",     PixelIconType.CAMERA,     AppDestination.Gallery),
            MenuItem("SETTINGS",  "SETTINGS",  PixelIconType.GEAR,       AppDestination.Settings)
        )
        // Items that let the user pick and save a default app on first tap
        val LINKABLE = setOf("MUSIC", "APPS")
    }

    private val _uiState = MutableStateFlow(
        HomeUiState(menuItems = MENU_ITEMS, systemStatus = SystemStatus.Empty)
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null
    private var currentIconPack: String? = null

    init {
        viewModelScope.launch {
            currentIconPack = prefs.iconPackFlow.first()
            refreshSystemStatus()
            startPolling()
            collectPinnedApps()
            loadInstalledApps()
            collectDisplaySettings()
            collectLinkedApps()
            collectIconPack()
        }
    }

    // ── App drawer (ORGANIZER) ────────────────────────────────────────────────

    fun openAppDrawer()  = _uiState.update { it.copy(isAppDrawerOpen = true) }
    fun closeAppDrawer() = _uiState.update { it.copy(isAppDrawerOpen = false) }

    // ── Linked apps (default app per linkable menu item) ──────────────────────

    private fun collectLinkedApps() {
        LINKABLE.forEach { id ->
            viewModelScope.launch {
                prefs.linkedAppFlow(id).collect { pkg ->
                    _uiState.update { state ->
                        val updated = if (pkg != null) state.linkedApps + (id to pkg)
                                      else             state.linkedApps - id
                        state.copy(linkedApps = updated)
                    }
                }
            }
        }
    }

    fun openPickerFor(itemId: String) = _uiState.update { it.copy(showPickerFor = itemId) }
    fun closePickerFor()              = _uiState.update { it.copy(showPickerFor = null) }

    fun linkAndLaunch(itemId: String, packageName: String) {
        viewModelScope.launch { prefs.setLinkedApp(itemId, packageName) }
        launchApp(packageName)
        _uiState.update { it.copy(showPickerFor = null) }
    }

    // ── Icon pack ─────────────────────────────────────────────────────────────

    private fun collectIconPack() {
        viewModelScope.launch {
            prefs.iconPackFlow.collect { pkg ->
                currentIconPack = pkg
                loadInstalledApps()
                reloadPinnedAppIcons()
            }
        }
    }

    private fun reloadPinnedAppIcons() {
        viewModelScope.launch {
            val pm = context.packageManager
            val packages = prefs.pinnedAppsFlow.first()
            val shortcuts = packages.mapNotNull { pkg ->
                runCatching {
                    val info  = pm.getApplicationInfo(pkg, 0)
                    val label = pm.getApplicationLabel(info).toString()
                    AppShortcut(pkg, label, iconForApp(pkg))
                }.getOrNull()
            }
            _uiState.update { it.copy(pinnedApps = shortcuts) }
        }
    }

    private fun iconForApp(packageName: String): androidx.compose.ui.graphics.ImageBitmap? {
        val pack = currentIconPack
        return if (pack != null) {
            IconPackLoader.loadIconForApp(context, pack, packageName)
                ?: runCatching { context.packageManager.getApplicationIcon(packageName).toImageBitmap() }.getOrNull()
        } else {
            runCatching { context.packageManager.getApplicationIcon(packageName).toImageBitmap() }.getOrNull()
        }
    }

    // ── External app launchers ────────────────────────────────────────────────

    fun openCamera() {
        val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }.onFailure {
            val fallback = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching { context.startActivity(fallback) }
        }
    }

    fun openGallery() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            type = "image/*"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    // ── Display settings ──────────────────────────────────────────────────────

    private fun collectDisplaySettings() {
        viewModelScope.launch {
            prefs.displaySettingsFlow.collect { ds ->
                _uiState.update { it.copy(displaySettings = ds) }
            }
        }
    }

    // ── Pinned apps ───────────────────────────────────────────────────────────

    private fun collectPinnedApps() {
        viewModelScope.launch {
            prefs.pinnedAppsFlow.collect { packages ->
                val pm = context.packageManager
                val shortcuts = packages.mapNotNull { pkg ->
                    runCatching {
                        val info  = pm.getApplicationInfo(pkg, 0)
                        val label = pm.getApplicationLabel(info).toString()
                        AppShortcut(pkg, label, iconForApp(pkg))
                    }.getOrNull()
                }
                _uiState.update { it.copy(pinnedApps = shortcuts) }
            }
        }
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            val pm     = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val apps   = pm.queryIntentActivities(intent, 0)
                .filter { it.activityInfo.packageName != context.packageName }
                .map { ri ->
                    val pkg = ri.activityInfo.packageName
                    AppShortcut(
                        packageName = pkg,
                        label       = ri.loadLabel(pm).toString(),
                        icon        = currentIconPack?.let {
                            IconPackLoader.loadIconForApp(context, it, pkg)
                        } ?: runCatching { ri.loadIcon(pm).toImageBitmap() }.getOrNull()
                    )
                }
                .sortedBy { it.label.lowercase() }
            _uiState.update { it.copy(installedApps = apps) }
        }
    }

    fun openPicker()  = _uiState.update { it.copy(isPickerOpen = true) }
    fun closePicker() = _uiState.update { it.copy(isPickerOpen = false) }

    fun togglePin(packageName: String) {
        viewModelScope.launch {
            val current = prefs.pinnedAppsFlow.first()
            prefs.setPinnedApps(
                if (packageName in current) current - packageName
                else current + packageName
            )
        }
    }

    fun launchApp(packageName: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    // ── System status polling ─────────────────────────────────────────────────

    fun onResume() { refreshSystemStatus(); startPolling() }
    fun onPause()  { pollingJob?.cancel(); pollingJob = null }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(WingmanConstants.STATUS_POLL_INTERVAL_MS)
                refreshSystemStatus()
            }
        }
    }

    private fun refreshSystemStatus() {
        _uiState.update { it.copy(systemStatus = readSystemStatus()) }
    }

    private fun readSystemStatus(): SystemStatus {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val rawLevel      = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale         = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct    = if (rawLevel >= 0 && scale > 0) (rawLevel * 100 / scale) else 0
        val status        = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val charging      = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL
        val timeLabel     = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        return SystemStatus(
            username       = WingmanConstants.USERNAME,
            batteryPercent = batteryPct,
            isCharging     = charging,
            signalBars     = 0,
            timeLabel      = timeLabel
        )
    }

    fun onScrollEvent() = audioPlayer.playScroll()
    fun onSelectEvent() = audioPlayer.playClick()

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
