package com.wingman.launcher.data.model

/**
 * Represents the current playback state exposed by MusicViewModel.
 * Progress is in seconds (not milliseconds); UI formats for display.
 *
 * Use PlayerState.Companion.Idle for the initial/empty state.
 */
data class PlayerState(
    val isPlaying: Boolean,
    val currentTrack: TrackInfo?,
    val progressSeconds: Int,
    val queueIndex: Int,
    val totalTracks: Int
) {
    companion object {
        val Idle = PlayerState(
            isPlaying = false,
            currentTrack = null,
            progressSeconds = 0,
            queueIndex = 0,
            totalTracks = 0
        )
    }
}
