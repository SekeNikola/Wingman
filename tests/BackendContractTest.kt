/**
 * BackendContractTest.kt
 *
 * Contract tests verifying that every domain model and repository interface
 * matches the definitions in INTERFACES.md exactly.
 *
 * These are documentation-style tests: they reference real production types and
 * assert field names / types at compile time via property access. If any domain
 * model field is renamed or removed, this file will fail to compile, providing
 * an early signal of a contract break.
 *
 * To run:   ./gradlew :app:test
 * Requires: JUnit 4, kotlinx-coroutines-test
 */

package com.wingman.launcher

import com.wingman.launcher.data.model.MenuItem
import com.wingman.launcher.data.model.Note
import com.wingman.launcher.data.model.OrganizerUiState
import com.wingman.launcher.data.model.PixelIconType
import com.wingman.launcher.data.model.PlayerState
import com.wingman.launcher.data.model.ScanFile
import com.wingman.launcher.data.model.ScanStatus
import com.wingman.launcher.data.model.ScansUiState
import com.wingman.launcher.data.model.SettingsState
import com.wingman.launcher.data.model.SystemStatus
import com.wingman.launcher.data.model.Task
import com.wingman.launcher.data.model.ThemeVariant
import com.wingman.launcher.data.model.TrackInfo
import com.wingman.launcher.ui.navigation.AppDestination
import com.wingman.launcher.util.WingmanConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// ---------------------------------------------------------------------------
// 1. AppDestination — INTERFACES.md §1
// ---------------------------------------------------------------------------

class AppDestinationContractTest {

    /**
     * INTERFACES.md §1 requires exactly seven sealed subclasses:
     * Boot, Home, Scans, Organizer, Tutorials, Music, Settings.
     * This test constructs each one and asserts distinctness.
     */
    @Test
    fun `AppDestination sealed class has all required variants`() {
        val boot: AppDestination      = AppDestination.Boot
        val home: AppDestination      = AppDestination.Home
        val scans: AppDestination     = AppDestination.Scans
        val organizer: AppDestination = AppDestination.Organizer
        val tutorials: AppDestination = AppDestination.Tutorials
        val music: AppDestination     = AppDestination.Music
        val settings: AppDestination  = AppDestination.Settings

        val allDestinations = setOf(boot, home, scans, organizer, tutorials, music, settings)
        assertEquals("Expected 7 distinct AppDestination variants", 7, allDestinations.size)
    }

    /**
     * INTERFACES.md §1: Boot is a terminal state; when() must be exhaustive.
     * Verify a when expression over AppDestination compiles and covers all branches.
     */
    @Test
    fun `AppDestination when expression is exhaustive`() {
        fun describe(dest: AppDestination): String = when (dest) {
            AppDestination.Boot      -> "boot"
            AppDestination.Home      -> "home"
            AppDestination.Scans     -> "scans"
            AppDestination.Organizer -> "organizer"
            AppDestination.Tutorials -> "tutorials"
            AppDestination.Music     -> "music"
            AppDestination.Settings  -> "settings"
        }
        assertEquals("boot",      describe(AppDestination.Boot))
        assertEquals("home",      describe(AppDestination.Home))
        assertEquals("settings",  describe(AppDestination.Settings))
    }
}

// ---------------------------------------------------------------------------
// 2. MenuItem — INTERFACES.md §3.1
// ---------------------------------------------------------------------------

class MenuItemContractTest {

    @Test
    fun `MenuItem has required fields with correct types`() {
        val item = MenuItem(
            id          = "SCANS",
            label       = "SCANS",
            iconType    = PixelIconType.RADAR,
            destination = AppDestination.Scans
        )
        assertEquals("SCANS", item.id)
        assertEquals("SCANS", item.label)
        assertEquals(PixelIconType.RADAR, item.iconType)
        assertEquals(AppDestination.Scans, item.destination)
    }

    @Test
    fun `PixelIconType enum has all required variants`() {
        val expected = setOf("RADAR", "FOLDER", "BOOK", "MUSIC_NOTE", "GEAR")
        val actual   = PixelIconType.values().map { it.name }.toSet()
        assertEquals(
            "PixelIconType must have exactly RADAR, FOLDER, BOOK, MUSIC_NOTE, GEAR",
            expected,
            actual
        )
    }
}

// ---------------------------------------------------------------------------
// 3. SystemStatus — INTERFACES.md §3.2
// ---------------------------------------------------------------------------

class SystemStatusContractTest {

    @Test
    fun `SystemStatus has all required fields`() {
        val status = SystemStatus(
            username       = "WARREN",
            batteryPercent = 85,
            isCharging     = true,
            signalBars     = 3,
            timeLabel      = "14:22"
        )
        assertEquals("WARREN", status.username)
        assertEquals(85, status.batteryPercent)
        assertTrue(status.isCharging)
        assertEquals(3, status.signalBars)
        assertEquals("14:22", status.timeLabel)
    }

    @Test
    fun `SystemStatus Empty companion property matches INTERFACES spec`() {
        val empty = SystemStatus.Empty
        assertEquals("WARREN", empty.username)
        assertEquals(0, empty.batteryPercent)
        assertFalse(empty.isCharging)
        assertEquals(0, empty.signalBars)
        assertEquals("00:00", empty.timeLabel)
    }

    @Test
    fun `signalBars valid range is 0 to 4`() {
        // A real implementation should clamp; test that the model accepts 0 and 4.
        val low  = SystemStatus("WARREN", 0, false, 0, "00:00")
        val high = SystemStatus("WARREN", 100, true, 4, "23:59")
        assertEquals(0, low.signalBars)
        assertEquals(4, high.signalBars)
    }
}

// ---------------------------------------------------------------------------
// 4. ScanFile — INTERFACES.md §3.3
// ---------------------------------------------------------------------------

class ScanFileContractTest {

    @Test
    fun `ScanFile has required fields with correct types`() {
        val file = ScanFile(
            id        = "scan_001",
            filename  = "SCAN_7741.dat",
            sizeKb    = 1024,
            timestamp = "2024-11-03 14:22",
            status    = ScanStatus.COMPLETE
        )
        assertEquals("scan_001",           file.id)
        assertEquals("SCAN_7741.dat",      file.filename)
        assertEquals(1024,                 file.sizeKb)
        assertEquals("2024-11-03 14:22",   file.timestamp)
        assertEquals(ScanStatus.COMPLETE,  file.status)
    }

    @Test
    fun `ScanStatus enum has all required variants`() {
        val expected = setOf("COMPLETE", "CORRUPT", "PENDING")
        val actual   = ScanStatus.values().map { it.name }.toSet()
        assertEquals("ScanStatus must have exactly COMPLETE, CORRUPT, PENDING", expected, actual)
    }
}

// ---------------------------------------------------------------------------
// 5. Note — INTERFACES.md §3.4
// ---------------------------------------------------------------------------

class NoteContractTest {

    @Test
    fun `Note has required fields with correct types`() {
        val note = Note(
            id        = 1L,
            title     = "Test Note",
            body      = "Some body text",
            createdAt = 1_699_999_000_000L
        )
        assertEquals(1L, note.id)
        assertEquals("Test Note", note.title)
        assertEquals("Some body text", note.body)
        assertEquals(1_699_999_000_000L, note.createdAt)
    }

    @Test
    fun `Note default id is 0L (per INTERFACES spec)`() {
        val note = Note(title = "x", body = "y", createdAt = 0L)
        assertEquals(0L, note.id)
    }
}

// ---------------------------------------------------------------------------
// 6. Task — INTERFACES.md §3.5
// ---------------------------------------------------------------------------

class TaskContractTest {

    @Test
    fun `Task has required fields with correct types`() {
        val task = Task(
            id          = 2L,
            label       = "Buy groceries",
            isCompleted = false,
            createdAt   = 1_700_000_000_000L
        )
        assertEquals(2L, task.id)
        assertEquals("Buy groceries", task.label)
        assertFalse(task.isCompleted)
        assertEquals(1_700_000_000_000L, task.createdAt)
    }

    @Test
    fun `Task isCompleted field — not isDone — per INTERFACES mapping rule`() {
        // INTERFACES.md §9: TaskEntity.isDone -> Task.isCompleted (renamed for clarity).
        // This test asserts the DOMAIN field name is isCompleted (not isDone).
        val completed   = Task(1L, "label", isCompleted = true, createdAt = 0L)
        val incomplete  = Task(2L, "label", isCompleted = false, createdAt = 0L)
        assertTrue(completed.isCompleted)
        assertFalse(incomplete.isCompleted)
    }
}

// ---------------------------------------------------------------------------
// 7. TrackInfo — INTERFACES.md §3.6
// ---------------------------------------------------------------------------

class TrackInfoContractTest {

    @Test
    fun `TrackInfo has required fields`() {
        val track = TrackInfo(
            id              = "track_001",
            title           = "SIGNAL LOST",
            artist          = "UNIT 47",
            durationSeconds = 214
        )
        assertEquals("track_001",    track.id)
        assertEquals("SIGNAL LOST",  track.title)
        assertEquals("UNIT 47",      track.artist)
        assertEquals(214,            track.durationSeconds)
    }

    @Test
    fun `TrackInfo duration is in seconds not milliseconds (per contract)`() {
        // INTERFACES.md §3.6: durationSeconds: Int — seconds, NOT milliseconds.
        // A 3-minute 34-second track = 214 seconds; verify Int type sufficient.
        val track = TrackInfo("t", "title", "artist", durationSeconds = 3600)
        assertEquals(3600, track.durationSeconds)
    }
}

// ---------------------------------------------------------------------------
// 8. PlayerState — INTERFACES.md §3.7
// ---------------------------------------------------------------------------

class PlayerStateContractTest {

    @Test
    fun `PlayerState has required fields`() {
        val state = PlayerState(
            isPlaying       = true,
            currentTrack    = TrackInfo("t1", "Track", "Artist", 180),
            progressSeconds = 45,
            queueIndex      = 1,
            totalTracks     = 7
        )
        assertTrue(state.isPlaying)
        assertNotNull(state.currentTrack)
        assertEquals(45, state.progressSeconds)
        assertEquals(1, state.queueIndex)
        assertEquals(7, state.totalTracks)
    }

    @Test
    fun `PlayerState Idle companion property matches INTERFACES spec`() {
        val idle = PlayerState.Idle
        assertFalse(idle.isPlaying)
        assertNull(idle.currentTrack)
        assertEquals(0, idle.progressSeconds)
        assertEquals(0, idle.queueIndex)
        assertEquals(0, idle.totalTracks)
    }

    @Test
    fun `PlayerState progress is in seconds not a Float fraction (INTERFACES 3_7)`() {
        // INTERFACES.md §3.7: "Progress is expressed in seconds (Int), not milliseconds."
        // TASK-03 note mentions 'progress: Float' but INTERFACES.md §3.7 and §5.4 both
        // state progressSeconds: Int. The implementation uses Int — this test verifies.
        val state = PlayerState(false, null, progressSeconds = 90, 0, 1)
        assertEquals(90, state.progressSeconds) // Int, not Float
    }
}

// ---------------------------------------------------------------------------
// 9. SettingsState — INTERFACES.md §3.8
// ---------------------------------------------------------------------------

class SettingsStateContractTest {

    @Test
    fun `SettingsState has effectIntensity Float and themeVariant ThemeVariant`() {
        val settings = SettingsState(
            effectIntensity = 0.5f,
            themeVariant    = ThemeVariant.STANDARD
        )
        assertEquals(0.5f, settings.effectIntensity, 0.001f)
        assertEquals(ThemeVariant.STANDARD, settings.themeVariant)
    }

    @Test
    fun `SettingsState Default companion property matches INTERFACES spec`() {
        val default = SettingsState.Default
        assertEquals(0.5f, default.effectIntensity, 0.001f)
        assertEquals(ThemeVariant.STANDARD, default.themeVariant)
    }

    @Test
    fun `ThemeVariant enum has STANDARD and HIGH_CONTRAST`() {
        val expected = setOf("STANDARD", "HIGH_CONTRAST")
        val actual   = ThemeVariant.values().map { it.name }.toSet()
        assertEquals(expected, actual)
    }
}

// ---------------------------------------------------------------------------
// 10. UiState sealed classes — INTERFACES.md §4
// ---------------------------------------------------------------------------

class UiStateContractTest {

    @Test
    fun `ScansUiState sealed class has Loading, Success, Error variants`() {
        val loading: ScansUiState         = ScansUiState.Loading
        val success: ScansUiState         = ScansUiState.Success(emptyList())
        val error: ScansUiState           = ScansUiState.Error("fail")

        assertTrue(loading is ScansUiState.Loading)
        assertTrue(success is ScansUiState.Success)
        assertTrue(error is ScansUiState.Error)
        assertEquals("fail", (error as ScansUiState.Error).message)
    }

    @Test
    fun `OrganizerUiState has notes tasks isLoading and Empty companion`() {
        val state = OrganizerUiState(
            notes     = emptyList(),
            tasks     = emptyList(),
            isLoading = false
        )
        assertTrue(state.notes.isEmpty())
        assertTrue(state.tasks.isEmpty())
        assertFalse(state.isLoading)

        val empty = OrganizerUiState.Empty
        assertTrue(empty.isLoading) // Empty starts with isLoading = true per spec
    }
}

// ---------------------------------------------------------------------------
// 11. WingmanConstants — INTERFACES.md §8
// ---------------------------------------------------------------------------

class WingmanConstantsContractTest {

    @Test
    fun `WingmanConstants has all values from INTERFACES spec`() {
        assertEquals("WARREN",    WingmanConstants.USERNAME)
        assertEquals(3_000L,      WingmanConstants.BOOT_DURATION_MS)
        assertEquals(80,          WingmanConstants.SELECTION_ANIMATION_MS)
        assertEquals(200,         WingmanConstants.SCREEN_TRANSITION_MS)
        assertEquals(400,         WingmanConstants.MODULE_TRANSITION_OVERLAY_MS)
        assertEquals(300L,        WingmanConstants.KEY_REPEAT_INITIAL_DELAY_MS)
        assertEquals(80L,         WingmanConstants.KEY_REPEAT_FAST_INTERVAL_MS)
        assertEquals(0.97f,       WingmanConstants.FLICKER_ALPHA_MIN, 0.001f)
        assertEquals(1.0f,        WingmanConstants.FLICKER_ALPHA_MAX, 0.001f)
        assertEquals(4_000,       WingmanConstants.FLICKER_PERIOD_MIN_MS)
        assertEquals(8_000,       WingmanConstants.FLICKER_PERIOD_MAX_MS)
        assertEquals(8,           WingmanConstants.GLOW_SPREAD_DP)
        assertEquals(4,           WingmanConstants.SCANLINE_SPACING_DP)
        assertEquals(64,          WingmanConstants.NOISE_BITMAP_SIZE_PX)
        assertEquals(30_000L,     WingmanConstants.STATUS_POLL_INTERVAL_MS)
    }
}
