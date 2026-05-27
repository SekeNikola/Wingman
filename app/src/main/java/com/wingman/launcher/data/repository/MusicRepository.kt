package com.wingman.launcher.data.repository

import com.wingman.launcher.data.model.MusicTrack
import com.wingman.launcher.data.source.DummyData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Provides track list as a StateFlow.
 * Static content — no mutation. Player state is managed in MusicViewModel.
 */
class MusicRepository {

    private val _tracks = MutableStateFlow<List<MusicTrack>>(DummyData.tracks)
    val tracks: Flow<List<MusicTrack>> = _tracks.asStateFlow()
}
