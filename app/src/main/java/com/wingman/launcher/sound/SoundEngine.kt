package com.wingman.launcher.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.wingman.launcher.R
import com.wingman.launcher.data.model.SettingsState

/**
 * Sound IDs exposed to ViewModels. No Android references needed at call sites.
 */
enum class SoundId { CLICK, SCROLL, BOOT, BACK }

/**
 * Singleton SoundPool wrapper.
 * Low-latency OGG playback. Max 4 simultaneous streams.
 *
 * Usage:
 *   SoundEngine.init(context)      — call from MainActivity.onCreate()
 *   SoundEngine.play(SoundId.CLICK, settings)
 *   SoundEngine.release()          — call from MainActivity.onDestroy()
 *
 * play() is a no-op when settings.soundEnabled = false.
 * Volume is fixed at 0.6f (per ARCHITECTURE.md spec).
 */
object SoundEngine {

    private const val VOLUME = 0.6f
    private const val MAX_STREAMS = 4

    private var soundPool: SoundPool? = null

    // Sound IDs assigned by SoundPool after loading
    private var idClick: Int = 0
    private var idScroll: Int = 0
    private var idBoot: Int = 0
    private var idBack: Int = 0

    private var initialized = false

    fun init(context: Context) {
        if (initialized) return

        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(MAX_STREAMS)
            .setAudioAttributes(attrs)
            .build()

        soundPool?.let { pool ->
            idClick = pool.load(context, R.raw.sfx_click, 1)
            idScroll = pool.load(context, R.raw.sfx_scroll, 1)
            idBoot = pool.load(context, R.raw.sfx_boot, 1)
            idBack = pool.load(context, R.raw.sfx_back, 1)
        }

        initialized = true
    }

    fun play(id: SoundId, settingsState: SettingsState) {
        if (!settingsState.soundEnabled) return
        val pool = soundPool ?: return

        val soundId = when (id) {
            SoundId.CLICK -> idClick
            SoundId.SCROLL -> idScroll
            SoundId.BOOT -> idBoot
            SoundId.BACK -> idBack
        }

        if (soundId != 0) {
            pool.play(soundId, VOLUME, VOLUME, 1, 0, 1f)
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        initialized = false
    }
}
