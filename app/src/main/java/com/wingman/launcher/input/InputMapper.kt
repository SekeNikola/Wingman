package com.wingman.launcher.input

import android.view.KeyEvent
import com.wingman.launcher.data.model.InputEvent

/**
 * Pure function mapper. No state, no side effects.
 * Maps raw Android key codes to abstracted InputEvent sealed class.
 *
 * Returns null for unmapped keys — callers must handle null gracefully.
 * No Compose or ViewModel references here.
 */
object InputMapper {

    fun map(keyCode: Int, event: KeyEvent): InputEvent? {
        // Only process ACTION_DOWN events (avoid double-fire on key repeat)
        // Hold-to-accelerate logic is in InputHandler, not here.
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> InputEvent.DpadUp
            KeyEvent.KEYCODE_DPAD_DOWN -> InputEvent.DpadDown
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_BUTTON_A -> InputEvent.Enter
            KeyEvent.KEYCODE_BACK -> InputEvent.Back
            KeyEvent.KEYCODE_VOLUME_UP -> InputEvent.ScrollUp
            KeyEvent.KEYCODE_VOLUME_DOWN -> InputEvent.ScrollDown
            else -> null
        }
    }
}
