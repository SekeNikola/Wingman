package com.wingman.launcher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wingman.launcher.data.model.InputEvent
import com.wingman.launcher.data.model.OrganizerTask
import com.wingman.launcher.data.model.SettingsState
import com.wingman.launcher.data.repository.OrganizerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * Manages ORGANIZER screen state.
 * Enter on a task toggles isDone. DPAD navigates the list.
 */
class OrganizerViewModel(
    private val repository: OrganizerRepository = OrganizerRepository()
) : ViewModel() {

    val tasks: StateFlow<List<OrganizerTask>> = repository.tasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    private val _selectedIndex = MutableStateFlow(0)
    val selectedIndex: StateFlow<Int> = _selectedIndex.asStateFlow()

    fun onInputEvent(event: InputEvent, settings: SettingsState) {
        val count = tasks.value.size.coerceAtLeast(1)

        when (event) {
            is InputEvent.DpadUp, is InputEvent.ScrollUp -> {
                _selectedIndex.update { (it - 1 + count) % count }
            }

            is InputEvent.DpadDown, is InputEvent.ScrollDown -> {
                _selectedIndex.update { (it + 1) % count }
            }

            is InputEvent.Enter -> {
                val task = tasks.value.getOrNull(_selectedIndex.value)
                task?.let { toggleTask(it.id) }
            }

            else -> Unit
        }
    }

    fun toggleTask(id: String) {
        repository.toggleTask(id)
    }
}
