package com.wingman.launcher.data.model

/**
 * Abstracted input events — no Android KeyEvent reference here or in ViewModels.
 *
 * Mapping:
 *   DPAD_UP / DPAD_DOWN    → DpadUp / DpadDown
 *   ENTER                  → Enter
 *   BACK                   → Back
 *   VOLUME_UP / swipe-up   → ScrollUp  (fallback when no DPAD)
 *   VOLUME_DOWN / swipe-dn → ScrollDown
 *   Future rotary encoder  → RotaryScroll(delta)
 */
sealed class InputEvent {
    object DpadUp : InputEvent()
    object DpadDown : InputEvent()
    object Enter : InputEvent()
    object Back : InputEvent()
    object ScrollUp : InputEvent()      // volume-up fallback / swipe
    object ScrollDown : InputEvent()    // volume-down fallback / swipe
    data class RotaryScroll(val delta: Float) : InputEvent()  // future rotary encoder
}
