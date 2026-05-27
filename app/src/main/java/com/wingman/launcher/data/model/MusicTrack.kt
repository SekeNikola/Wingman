package com.wingman.launcher.data.model

/**
 * A single track in the mock music player.
 * No actual audio playback — progress is simulated via coroutine tick.
 */
data class MusicTrack(
    val id: String,
    val title: String,
    val artist: String,
    val durationSeconds: Int
)

/**
 * Full player state snapshot. Drives the MusicScreen composable entirely.
 */
data class MusicPlayerState(
    val currentTrack: MusicTrack?,
    val isPlaying: Boolean,
    val progressSeconds: Int,
    val tracks: List<MusicTrack>
)
