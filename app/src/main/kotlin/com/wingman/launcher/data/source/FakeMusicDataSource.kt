package com.wingman.launcher.data.source

import com.wingman.launcher.data.model.TrackInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory fake data source for the Music section.
 * Returns a static list of dummy track metadata; no MediaPlayer or network call.
 *
 * MusicRepository uses this list to build the playback queue.
 */
@Singleton
class FakeMusicDataSource @Inject constructor() {

    private val fakeTracks: List<TrackInfo> = listOf(
        TrackInfo(
            id = "track_001",
            title = "SIGNAL LOST",
            artist = "UNIT 47",
            durationSeconds = 214
        ),
        TrackInfo(
            id = "track_002",
            title = "DEEP CARRIER",
            artist = "ECHO DIVISION",
            durationSeconds = 187
        ),
        TrackInfo(
            id = "track_003",
            title = "COLD PROTOCOL",
            artist = "UNIT 47",
            durationSeconds = 253
        ),
        TrackInfo(
            id = "track_004",
            title = "PHASE DRIFT",
            artist = "NULL SECTOR",
            durationSeconds = 301
        ),
        TrackInfo(
            id = "track_005",
            title = "TERMINAL ECHO",
            artist = "ECHO DIVISION",
            durationSeconds = 178
        ),
        TrackInfo(
            id = "track_006",
            title = "VECTOR GHOST",
            artist = "NULL SECTOR",
            durationSeconds = 232
        ),
        TrackInfo(
            id = "track_007",
            title = "LOW BANDWIDTH",
            artist = "UNIT 47",
            durationSeconds = 196
        )
    )

    /**
     * Returns all available track metadata as a cold Flow.
     */
    fun getTracks(): Flow<List<TrackInfo>> = flowOf(fakeTracks)

    /**
     * Returns all tracks as a plain list for synchronous queue initialization.
     */
    fun getTracksSync(): List<TrackInfo> = fakeTracks
}
