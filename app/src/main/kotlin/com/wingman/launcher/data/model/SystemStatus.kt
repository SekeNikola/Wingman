package com.wingman.launcher.data.model

/**
 * Snapshot of device system information displayed in the TopBar.
 *
 *  username     -- always "WARREN" in v1
 *  batteryPercent -- 0..100
 *  isCharging   -- true when plugged in
 *  signalBars   -- 0 (no signal) to 4 (full)
 *  timeLabel    -- pre-formatted "HH:MM" string
 *
 * HomeViewModel polls and emits updated SystemStatus every 30 seconds
 * while the app is foregrounded, and also on each onResume().
 *
 * Use SystemStatus.Companion.Empty for initial/loading state.
 */
data class SystemStatus(
    val username: String,
    val batteryPercent: Int,
    val isCharging: Boolean,
    val signalBars: Int,
    val timeLabel: String
) {
    companion object {
        val Empty = SystemStatus(
            username = "WARREN",
            batteryPercent = 0,
            isCharging = false,
            signalBars = 0,
            timeLabel = "00:00"
        )
    }
}
