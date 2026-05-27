package com.wingman.launcher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wingman.launcher.data.model.InputEvent
import com.wingman.launcher.data.model.ScanFile
import com.wingman.launcher.data.model.SettingsState
import com.wingman.launcher.data.repository.ScansRepository
import com.wingman.launcher.sound.SoundEngine
import com.wingman.launcher.sound.SoundId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * Manages SCANS screen state.
 * DPAD navigates the scan list; Back is bubbled up to MainViewModel.
 */
class ScansViewModel(
    private val repository: ScansRepository = ScansRepository()
) : ViewModel() {

    val scans: StateFlow<List<ScanFile>> = repository.scans
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    private val _selectedIndex = MutableStateFlow(0)
    val selectedIndex: StateFlow<Int> = _selectedIndex.asStateFlow()

    fun onInputEvent(event: InputEvent, settings: SettingsState) {
        val count = scans.value.size.coerceAtLeast(1)

        when (event) {
            is InputEvent.DpadUp, is InputEvent.ScrollUp -> {
                SoundEngine.play(SoundId.SCROLL, settings)
                _selectedIndex.update { (it - 1 + count) % count }
            }

            is InputEvent.DpadDown, is InputEvent.ScrollDown -> {
                SoundEngine.play(SoundId.SCROLL, settings)
                _selectedIndex.update { (it + 1) % count }
            }

            is InputEvent.Enter -> {
                SoundEngine.play(SoundId.CLICK, settings)
                // No detail view for scans — Enter is reserved for future use
            }

            else -> Unit
        }
    }
}
