package com.wingman.launcher.ui.input

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import com.wingman.launcher.util.WingmanConstants
import kotlinx.coroutines.delay

// ── Normalized input event ────────────────────────────────────────────────────
sealed class InputEvent {
    object Up     : InputEvent()
    object Down   : InputEvent()
    object Select : InputEvent()
    object Back   : InputEvent()
}

// ── Key state tracker for repeat acceleration ─────────────────────────────────
private enum class HeldKey { NONE, UP, DOWN }

/**
 * Return type of [rememberInputHandler].
 * Screen composables pass [onEvent] to [handleWingmanInput]'s first parameter
 * and [onDirectionKeyUp] to its second parameter so that releasing a direction
 * key cancels the repeat-acceleration coroutine (BUG-17).
 */
data class InputHandlerState(
    val onEvent: (InputEvent) -> Unit,
    val onDirectionKeyUp: () -> Unit
)

/**
 * Attaches Wingman unified input handling to any composable.
 *
 * D-pad / keyboard keys are translated to [InputEvent].
 * Scroll-wheel delta > 0.5 is translated to Up/Down.
 * Touch events fall through — handle them with .clickable at the item level.
 *
 * [onEvent] receives normalized [InputEvent] values from D-pad / keyboard.
 * [onDirectionKeyUp] is called when a direction key (Up/Down) is released;
 * used by [rememberInputHandler] to cancel key-repeat acceleration (BUG-17).
 *
 * Key-repeat acceleration: after [WingmanConstants.KEY_REPEAT_INITIAL_DELAY_MS] ms
 * of holding UP/DOWN, events fire every [WingmanConstants.KEY_REPEAT_FAST_INTERVAL_MS] ms.
 */
fun Modifier.handleWingmanInput(
    onEvent: (InputEvent) -> Unit,
    onDirectionKeyUp: () -> Unit = {}
): Modifier =
    this
        .onKeyEvent { keyEvent ->
            when (keyEvent.type) {
                KeyEventType.KeyDown -> {
                    val inputEvent = when {
                        keyEvent.key == Key.DirectionUp ||
                            keyEvent.key == Key.W -> InputEvent.Up
                        keyEvent.key == Key.DirectionDown ||
                            keyEvent.key == Key.S -> InputEvent.Down
                        keyEvent.key == Key.DirectionCenter ||
                            keyEvent.key == Key.Enter ||
                            keyEvent.key == Key.NumPadEnter -> InputEvent.Select
                        keyEvent.key == Key.Back ||
                            keyEvent.key == Key.Escape -> InputEvent.Back
                        else -> null
                    }
                    if (inputEvent != null) {
                        onEvent(inputEvent)
                        true
                    } else {
                        false
                    }
                }
                // BUG-17: Reset held state on KeyUp for direction keys so the
                // repeat-acceleration coroutine stops when the key is released.
                KeyEventType.KeyUp -> {
                    if (keyEvent.key == Key.DirectionUp || keyEvent.key == Key.W ||
                        keyEvent.key == Key.DirectionDown || keyEvent.key == Key.S) {
                        onDirectionKeyUp()
                    }
                    false // observe only — do not consume
                }
                else -> false
            }
        }
        .pointerInput(onEvent) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    if (event.type == PointerEventType.Scroll) {
                        val scrollDelta = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f
                        when {
                            scrollDelta > 0.5f  -> onEvent(InputEvent.Down)
                            scrollDelta < -0.5f -> onEvent(InputEvent.Up)
                        }
                    }
                }
            }
        }

/**
 * Convenience overload that accepts an [InputHandlerState] (the return value of
 * [rememberInputHandler]) directly.
 */
fun Modifier.handleWingmanInput(state: InputHandlerState): Modifier =
    handleWingmanInput(state.onEvent, state.onDirectionKeyUp)

/**
 * Composable hook that sets up key-repeat acceleration for held UP/DOWN keys.
 * Must be called inside a composable that also applies [handleWingmanInput].
 *
 * Returns an [InputHandlerState] containing:
 * - [InputHandlerState.onEvent]: pass to [handleWingmanInput]
 * - [InputHandlerState.onDirectionKeyUp]: pass to [handleWingmanInput] so KeyUp
 *   events can cancel the acceleration coroutine (BUG-17)
 *
 * Usage:
 * ```
 * val handler = rememberInputHandler { event ->
 *     when (event) {
 *         InputEvent.Up   -> selectedIndex = (selectedIndex - 1).coerceAtLeast(0)
 *         InputEvent.Down -> selectedIndex = (selectedIndex + 1).coerceAtMost(maxIndex)
 *         InputEvent.Select -> navigate(...)
 *         InputEvent.Back   -> onBack()
 *     }
 * }
 * Box(modifier = Modifier.handleWingmanInput(handler)) { ... }
 * ```
 */
@Composable
fun rememberInputHandler(onEvent: (InputEvent) -> Unit): InputHandlerState {
    var heldKey by remember { mutableStateOf(HeldKey.NONE) }

    // Key-repeat acceleration coroutine
    LaunchedEffect(heldKey) {
        if (heldKey == HeldKey.NONE) return@LaunchedEffect
        delay(WingmanConstants.KEY_REPEAT_INITIAL_DELAY_MS)
        while (true) {
            when (heldKey) {
                HeldKey.UP   -> onEvent(InputEvent.Up)
                HeldKey.DOWN -> onEvent(InputEvent.Down)
                HeldKey.NONE -> break
            }
            delay(WingmanConstants.KEY_REPEAT_FAST_INTERVAL_MS)
        }
    }

    val wrappedOnEvent = remember(onEvent) {
        { event: InputEvent ->
            onEvent(event)
            // track held state for acceleration
            when (event) {
                is InputEvent.Up   -> heldKey = HeldKey.UP
                is InputEvent.Down -> heldKey = HeldKey.DOWN
                else               -> heldKey = HeldKey.NONE
            }
        }
    }

    val onDirectionKeyUp = remember { { heldKey = HeldKey.NONE } }

    return InputHandlerState(wrappedOnEvent, onDirectionKeyUp)
}
