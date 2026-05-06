package com.wingman.launcher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wingman.launcher.data.model.ScansUiState
import com.wingman.launcher.data.repository.ScansRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Scans screen.
 *
 * Collects scan file data from [ScansRepository] and exposes it as
 * [ScansUiState] (Loading -> Success | Error) via a [StateFlow].
 *
 * [refresh] resets to Loading and re-collects the repository flow,
 * allowing the UI to trigger a manual reload.
 */
@HiltViewModel
class ScansViewModel @Inject constructor(
    private val repository: ScansRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScansUiState>(ScansUiState.Loading)
    val uiState: StateFlow<ScansUiState> = _uiState.asStateFlow()

    init {
        loadScans()
    }

    /**
     * Manual refresh trigger. Resets state to Loading then re-collects.
     */
    fun refresh() {
        _uiState.value = ScansUiState.Loading
        loadScans()
    }

    private fun loadScans() {
        viewModelScope.launch {
            repository.scanFiles
                .catch { e -> _uiState.value = ScansUiState.Error(e.message ?: "Unknown error") }
                .collect { files -> _uiState.value = ScansUiState.Success(files) }
        }
    }
}
