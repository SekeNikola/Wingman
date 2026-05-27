package com.wingman.launcher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wingman.launcher.data.model.InputEvent
import com.wingman.launcher.data.model.MusicPlayerState
import com.wingman.launcher.data.model.MusicTrack
import com.wingman.launcher.data.model.SettingsState
import com.wingman.launcher.data.repository.MusicRepository
import com.wingman.launcher.sound.SoundEngine
import com.wingman.launcher.sound.SoundId
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Mock music player. No actual audio playback — progress is simulated.
 * Progress tick: 1s interval while isPlaying = true.
 * Progress resets to 0 when a new track is selected.
 */
class MusicViewModel(
    private val repository: MusicRepository = MusicRepository()
) : ViewModel() {

    private val _playerState = MutableStateFlow(
        MusicPlayerState(
            currentTrack = null,
            isPlaying = false,
            progressSeconds = 0,
            tracks = repository.tracks.let { emptyList() }  // populated below
        )
    )
    val playerState: StateFlow<MusicPlayerState> = _playerState.asStateFlow()

    private val _selectedIndex = MutableStateFlow(0)
    val selectedIndex: StateFlow<Int> = _selectedIndex.asStateFlow()

    private var tickJob: Job? = null

    init {
        viewModelScope.launch {
            repository.tracks.collect { tracks ->
                _playerState.update { it.copy(tracks = tracks) }
            }
        }
    }

    fun onInputEvent(event: InputEvent, settings: SettingsState) {
        val tracks = _playerState.value.tracks
        val count = tracks.size.coerceAtLeast(1)

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
                val track = tracks.getOrNull(_selectedIndex.value)
                track?.let { playTrack(it) }
            }

            else -> Unit
        }
    }

    fun playTrack(track: MusicTrack) {
        _playerState.update {
            it.copy(
                currentTrack = track,
                isPlaying = true,
                progressSeconds = 0
            )
        }
        startProgressTick()
    }

    fun togglePlayPause() {
        val current = _playerState.value
        if (current.currentTrack == null) return

        _playerState.update { it.copy(isPlaying = !it.isPlaying) }

        if (_playerState.value.isPlaying) {
            startProgressTick()
        } else {
            tickJob?.cancel()
        }
    }

    private fun startProgressTick() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                val state = _playerState.value
                if (!state.isPlaying) break

                val duration = state.currentTrack?.durationSeconds ?: break
                val next = state.progressSeconds + 1
                if (next >= duration) {
                    // Track complete — stop
                    _playerState.update { it.copy(isPlaying = false, progressSeconds = duration) }
                    break
                } else {
                    _playerState.update { it.copy(progressSeconds = next) }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        tickJob?.cancel()
    }
}
