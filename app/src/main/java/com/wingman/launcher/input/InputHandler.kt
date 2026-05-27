package com.wingman.launcher.input

import android.view.KeyEvent
import com.wingman.launcher.data.model.InputEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Stateful input handler. Bridges raw KeyEvents to the ViewModel callback.
 *
 * Hold-to-accelerate:
 *   - Initial delay before first repeat:   300ms
 *   - Normal repeat interval:               80ms
 *   - After 1s of continuous hold:          40ms (accelerated)
 *
 * Only directional events (DpadUp, DpadDown, ScrollUp, ScrollDown) repeat.
 * Enter, Back, RotaryScroll fire once per key-down.
 */
class InputHandler(
    private val onEvent: (InputEvent) -> Unit
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var holdJob: Job? = null

    fun handleKeyDown(keyCode: Int, event: KeyEvent) {
        // Android fires ACTION_DOWN with repeatCount > 0 for held keys.
        // We suppress the system repeat and drive our own timing.
        if (event.repeatCount > 0) return

        val inputEvent = InputMapper.map(keyCode, event) ?: return

        // Fire immediately
        onEvent(inputEvent)

        // Start hold-to-accelerate for directional events only
        if (inputEvent.isDirectional()) {
            startHoldRepeat(inputEvent)
        }
    }

    fun handleKeyUp(keyCode: Int, event: KeyEvent) {
        cancelHold()
    }

    fun release() {
        cancelHold()
        // Cancel the entire scope on Activity destroy
        scope.launch { holdJob?.cancelAndJoin() }
    }

    // -------------------------------------------------------------------------

    private fun startHoldRepeat(event: InputEvent) {
        holdJob?.cancel()
        holdJob = scope.launch {
            // Initial delay before repeat starts
            delay(INITIAL_DELAY_MS)

            var elapsed = 0L
            while (isActive) {
                onEvent(event)
                val interval = if (elapsed >= ACCELERATE_AFTER_MS) {
                    REPEAT_RATE_FAST_MS
                } else {
                    REPEAT_RATE_NORMAL_MS
                }
                delay(interval)
                elapsed += interval
            }
        }
    }

    private fun cancelHold() {
        holdJob?.cancel()
        holdJob = null
    }

    private fun InputEvent.isDirectional(): Boolean = when (this) {
        InputEvent.DpadUp,
        InputEvent.DpadDown,
        InputEvent.ScrollUp,
        InputEvent.ScrollDown -> true
        else -> false
    }

    companion object {
        private const val INITIAL_DELAY_MS = 300L
        private const val REPEAT_RATE_NORMAL_MS = 80L
        private const val REPEAT_RATE_FAST_MS = 40L
        private const val ACCELERATE_AFTER_MS = 1000L
    }
}
