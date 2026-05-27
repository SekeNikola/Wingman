package com.wingman.launcher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wingman.launcher.data.model.InputEvent
import com.wingman.launcher.data.model.SettingsState
import com.wingman.launcher.data.repository.SettingsRepository
import com.wingman.launcher.sound.SoundEngine
import com.wingman.launcher.sound.SoundId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Settings screen ViewModel.
 * 5 navigable rows: Effects, Glow, Flicker, Sound, ThemeIntensity.
 * DPAD navigates rows; Enter toggles/adjusts.
 * All changes persist via SettingsRepository (DataStore).
 */
class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<SettingsState> = repository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SettingsState()
        )

    private val _selectedIndex = MutableStateFlow(0)
    val selectedIndex: StateFlow<Int> = _selectedIndex.asStateFlow()

    // 5 settings rows: 0=Effects, 1=Glow, 2=Flicker, 3=Sound, 4=ThemeIntensity
    private val rowCount = 5
    private val intensityStep = 0.1f

    fun onInputEvent(event: InputEvent) {
        val current = settings.value
        when (event) {
            is InputEvent.DpadUp, is InputEvent.ScrollUp -> {
                SoundEngine.play(SoundId.SCROLL, current)
                _selectedIndex.update { (it - 1 + rowCount) % rowCount }
            }

            is InputEvent.DpadDown, is InputEvent.ScrollDown -> {
                SoundEngine.play(SoundId.SCROLL, current)
                _selectedIndex.update { (it + 1) % rowCount }
            }

            is InputEvent.Enter -> {
                SoundEngine.play(SoundId.CLICK, current)
                when (_selectedIndex.value) {
                    0 -> toggleEffects()
                    1 -> toggleGlow()
                    2 -> toggleFlicker()
                    3 -> toggleSound()
                    4 -> setThemeIntensity(
                        (current.themeIntensity + intensityStep).let {
                            if (it > 1f) 0f else it   // cycle: 0.0 → 0.1 → ... → 1.0 → 0.0
                        }
                    )
                }
            }

            else -> Unit
        }
    }

    fun toggleEffects() {
        viewModelScope.launch { repository.setEffectsEnabled(!settings.value.effectsEnabled) }
    }

    fun toggleGlow() {
        viewModelScope.launch { repository.setGlowEnabled(!settings.value.glowEnabled) }
    }

    fun toggleFlicker() {
        viewModelScope.launch { repository.setFlickerEnabled(!settings.value.flickerEnabled) }
    }

    fun toggleSound() {
        viewModelScope.launch { repository.setSoundEnabled(!settings.value.soundEnabled) }
    }

    fun setThemeIntensity(value: Float) {
        viewModelScope.launch { repository.setThemeIntensity(value) }
    }
}
