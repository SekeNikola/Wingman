package com.wingman.launcher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wingman.launcher.data.model.InputEvent
import com.wingman.launcher.data.model.SettingsState
import com.wingman.launcher.data.model.TutorialEntry
import com.wingman.launcher.data.repository.TutorialsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * Two-level navigation: list → detail.
 * Back from detail returns to list; Back from list is handled by MainViewModel.
 */

sealed class TutorialsNavState {
    object List : TutorialsNavState()
    data class Detail(val entry: TutorialEntry, val scrollOffset: Int = 0) : TutorialsNavState()
}

class TutorialsViewModel(
    private val repository: TutorialsRepository = TutorialsRepository()
) : ViewModel() {

    val tutorials: StateFlow<List<TutorialEntry>> = repository.tutorials
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    private val _selectedIndex = MutableStateFlow(0)
    val selectedIndex: StateFlow<Int> = _selectedIndex.asStateFlow()

    private val _tutorialsNavState = MutableStateFlow<TutorialsNavState>(TutorialsNavState.List)
    val tutorialsNavState: StateFlow<TutorialsNavState> = _tutorialsNavState.asStateFlow()

    // Pixels to scroll per DPAD press in detail view
    private val scrollStep = 80

    fun onInputEvent(event: InputEvent, settings: SettingsState): Boolean {
        // Returns true if the event was consumed (Back in list = not consumed → MainViewModel handles)
        return when (val nav = _tutorialsNavState.value) {
            is TutorialsNavState.List -> handleListInput(event, settings)
            is TutorialsNavState.Detail -> handleDetailInput(nav, event, settings)
        }
    }

    private fun handleListInput(event: InputEvent, settings: SettingsState): Boolean {
        val count = tutorials.value.size.coerceAtLeast(1)
        return when (event) {
            is InputEvent.DpadUp, is InputEvent.ScrollUp -> {
                _selectedIndex.update { (it - 1 + count) % count }
                true
            }
            is InputEvent.DpadDown, is InputEvent.ScrollDown -> {
                _selectedIndex.update { (it + 1) % count }
                true
            }
            is InputEvent.Enter -> {
                val entry = tutorials.value.getOrNull(_selectedIndex.value)
                entry?.let { openEntry(it) }
                true
            }
            is InputEvent.Back -> false  // Let MainViewModel handle (navigate to Home)
            else -> false
        }
    }

    private fun handleDetailInput(
        nav: TutorialsNavState.Detail,
        event: InputEvent,
        settings: SettingsState
    ): Boolean {
        return when (event) {
            is InputEvent.DpadUp, is InputEvent.ScrollUp -> {
                val newOffset = (nav.scrollOffset - scrollStep).coerceAtLeast(0)
                _tutorialsNavState.value = nav.copy(scrollOffset = newOffset)
                true
            }
            is InputEvent.DpadDown, is InputEvent.ScrollDown -> {
                _tutorialsNavState.value = nav.copy(scrollOffset = nav.scrollOffset + scrollStep)
                true
            }
            is InputEvent.Back -> {
                closeEntry()
                true  // Consumed — back goes to list, not Home
            }
            else -> false
        }
    }

    fun openEntry(entry: TutorialEntry) {
        _tutorialsNavState.value = TutorialsNavState.Detail(entry, scrollOffset = 0)
    }

    fun closeEntry() {
        _tutorialsNavState.value = TutorialsNavState.List
    }
}
