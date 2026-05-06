package com.wingman.launcher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wingman.launcher.data.model.Note
import com.wingman.launcher.data.model.OrganizerUiState
import com.wingman.launcher.data.model.Task
import com.wingman.launcher.data.repository.OrganizerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Organizer screen.
 *
 * Collects [Note] and [Task] flows from [OrganizerRepository] and combines
 * them into a single [OrganizerUiState] exposed via [uiState].
 *
 * All mutation methods (addNote, deleteNote, addTask, toggleTask, deleteTask)
 * delegate to the repository and are run in [viewModelScope].
 */
@HiltViewModel
class OrganizerViewModel @Inject constructor(
    private val repository: OrganizerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrganizerUiState.Empty)
    val uiState: StateFlow<OrganizerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repository.notes, repository.tasks) { notes, tasks ->
                OrganizerUiState(notes = notes, tasks = tasks, isLoading = false)
            }.collect { state ->
                _uiState.update { state }
            }
        }
    }

    fun addNote(title: String, body: String) {
        viewModelScope.launch {
            repository.addNote(title, body)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun addTask(label: String) {
        viewModelScope.launch {
            repository.addTask(label)
        }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch {
            repository.toggleTask(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }
}
