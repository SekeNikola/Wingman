package com.wingman.launcher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wingman.launcher.data.model.PlayerState
import com.wingman.launcher.data.model.TrackInfo
import com.wingman.launcher.data.repository.MusicRepository
import com.wingman.launcher.data.repository.PlaybackEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Music screen.
 *
 * Maintains a [PlayerState] by collecting [PlaybackEvent]s from [MusicRepository].
 * Also collects the full track list to keep totalTracks and queueIndex in sync.
 *
 * play/pause/next/previous delegate to the repository and are fully async.
 */
@HiltViewModel
class MusicViewModel @Inject constructor(
    private val repository: MusicRepository
) : ViewModel() {

    private val _playerState = MutableStateFlow(PlayerState.Idle)
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    /** Local snapshot of the track list for index calculations. */
    private var trackList: List<TrackInfo> = emptyList()

    init {
        // Collect the track catalogue and initialize the queue
        viewModelScope.launch {
            repository.tracks.collect { tracks ->
                trackList = tracks
                if (_playerState.value.currentTrack == null && tracks.isNotEmpty()) {
                    _playerState.update { state ->
                        state.copy(
                            currentTrack = tracks.first(),
                            totalTracks  = tracks.size,
                            queueIndex   = 0
                        )
                    }
                } else {
                    _playerState.update { it.copy(totalTracks = tracks.size) }
                }
            }
        }

        // Reflect playback events from the repository into PlayerState
        viewModelScope.launch {
            repository.playbackEvents.collect { event ->
                when (event) {
                    is PlaybackEvent.TrackStarted -> {
                        val index = trackList.indexOfFirst { it.id == event.track.id }
                        _playerState.update { state ->
                            state.copy(
                                isPlaying       = true,
                                currentTrack    = event.track,
                                queueIndex      = if (index >= 0) index else state.queueIndex,
                                progressSeconds = 0
                            )
                        }
                    }
                    is PlaybackEvent.Paused -> {
                        _playerState.update { it.copy(isPlaying = false) }
                    }
                    is PlaybackEvent.Resumed -> {
                        _playerState.update { it.copy(isPlaying = true) }
                    }
                    is PlaybackEvent.TrackEnded -> {
                        _playerState.update { it.copy(isPlaying = false, progressSeconds = 0) }
                    }
                    is PlaybackEvent.Error -> {
                        _playerState.update { it.copy(isPlaying = false) }
                    }
                }
            }
        }
    }

    /** Start or resume playback of the current track. */
    fun play() {
        viewModelScope.launch {
            val current = _playerState.value.currentTrack
            if (current != null && !_playerState.value.isPlaying) {
                repository.resume()
            } else if (current != null) {
                repository.play(current.id)
            } else if (trackList.isNotEmpty()) {
                repository.play(trackList.first().id)
            }
        }
    }

    /** Pause playback. */
    fun pause() {
        viewModelScope.launch {
            repository.pause()
        }
    }

    /** Skip to the next track in the queue. */
    fun next() {
        viewModelScope.launch {
            repository.next()
        }
    }

    /** Go back to the previous track in the queue. */
    fun previous() {
        viewModelScope.launch {
            repository.previous()
        }
    }
}
