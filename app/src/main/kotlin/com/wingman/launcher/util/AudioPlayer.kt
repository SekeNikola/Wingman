package com.wingman.launcher.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.wingman.launcher.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps Android [SoundPool] for low-latency SFX playback.
 *
 * Two sound effects are used:
 *   - sfx_scroll: played whenever the selection cursor moves (Up/Down)
 *   - sfx_click:  played when an item is activated (Select)
 *
 * Audio files live in res/raw/ as .ogg files (small, low-latency format).
 * If the res/raw files are absent (e.g. during early development), playback
 * silently fails -- no crash.
 *
 * Usage:
 *   audioPlayer.playScroll()
 *   audioPlayer.playClick()
 */
@Singleton
class AudioPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private var scrollSoundId: Int = 0
    private var clickSoundId: Int = 0

    init {
        loadSounds()
    }

    private fun loadSounds() {
        try {
            scrollSoundId = soundPool.load(context, R.raw.sfx_scroll, 1)
            clickSoundId  = soundPool.load(context, R.raw.sfx_click, 1)
        } catch (e: Exception) {
            // Resources absent during development -- ignore
        }
    }

    /** Play the scroll/selection-move sound effect at full volume. */
    fun playScroll() {
        if (scrollSoundId != 0) {
            soundPool.play(scrollSoundId, 1f, 1f, 0, 0, 1f)
        }
    }

    /** Play the click/activate sound effect at full volume. */
    fun playClick() {
        if (clickSoundId != 0) {
            soundPool.play(clickSoundId, 1f, 1f, 0, 0, 1f)
        }
    }

    /** Release SoundPool resources. Call when the app is destroyed. */
    fun release() {
        soundPool.release()
    }
}
