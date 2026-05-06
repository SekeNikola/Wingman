package com.wingman.launcher.data.repository

import com.wingman.launcher.data.model.TrackInfo
import com.wingman.launcher.data.source.FakeMusicDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sealed event hierarchy emitted by MusicRepository whenever playback state changes.
 */
sealed class PlaybackEvent {
    data class TrackStarted(val track: TrackInfo) : PlaybackEvent()
    object Paused  : PlaybackEvent()
    object Resumed : PlaybackEvent()
    object TrackEnded : PlaybackEvent()
    data class Error(val reason: String) : PlaybackEvent()
}

/**
 * Repository interface for music playback.
 * ViewModels depend on this interface to allow mocking in tests.
 */
interface MusicRepository {
    val tracks: Flow<List<TrackInfo>>
    val playbackEvents: Flow<PlaybackEvent>

    suspend fun play(trackId: String)
    suspend fun pause()
    suspend fun resume()
    suspend fun next()
    suspend fun previous()
}

/**
 * Concrete implementation backed by [FakeMusicDataSource].
 *
 * This implementation does NOT use a real MediaPlayer -- it simulates
 * playback state transitions and emits [PlaybackEvent] values.
 * MusicViewModel owns the current PlayerState derived from these events.
 */
@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val dataSource: FakeMusicDataSource
) : MusicRepository {

    override val tracks: Flow<List<TrackInfo>> = dataSource.getTracks()

    private val _playbackEvents = MutableSharedFlow<PlaybackEvent>(extraBufferCapacity = 8)
    override val playbackEvents: Flow<PlaybackEvent> = _playbackEvents.asSharedFlow()

    private val mutex = Mutex()
    private val queue: List<TrackInfo> = dataSource.getTracksSync()
    private var currentIndex: Int = 0
    private var isPlaying: Boolean = false

    override suspend fun play(trackId: String) {
        val index = queue.indexOfFirst { it.id == trackId }
        if (index == -1) {
            _playbackEvents.emit(PlaybackEvent.Error("Track $trackId not found in queue"))
            return
        }
        mutex.withLock {
            currentIndex = index
            isPlaying = true
        }
        _playbackEvents.emit(PlaybackEvent.TrackStarted(queue[currentIndex]))
    }

    override suspend fun pause() {
        val wasPlaying = mutex.withLock {
            if (isPlaying) { isPlaying = false; true } else false
        }
        if (wasPlaying) _playbackEvents.emit(PlaybackEvent.Paused)
    }

    override suspend fun resume() {
        val didResume = mutex.withLock {
            if (!isPlaying && queue.isNotEmpty()) { isPlaying = true; true } else false
        }
        if (didResume) _playbackEvents.emit(PlaybackEvent.Resumed)
    }

    override suspend fun next() {
        if (queue.isEmpty()) return
        val track = mutex.withLock {
            currentIndex = (currentIndex + 1) % queue.size
            isPlaying = true
            queue[currentIndex]
        }
        _playbackEvents.emit(PlaybackEvent.TrackStarted(track))
    }

    override suspend fun previous() {
        if (queue.isEmpty()) return
        val track = mutex.withLock {
            currentIndex = if (currentIndex == 0) queue.size - 1 else currentIndex - 1
            isPlaying = true
            queue[currentIndex]
        }
        _playbackEvents.emit(PlaybackEvent.TrackStarted(track))
    }
}
