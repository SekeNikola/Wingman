package com.wingman.launcher.data.source

import com.wingman.launcher.data.model.MusicTrack
import com.wingman.launcher.data.model.OrganizerTask
import com.wingman.launcher.data.model.Priority
import com.wingman.launcher.data.model.ScanFile
import com.wingman.launcher.data.model.ScanStatus
import com.wingman.launcher.data.model.TutorialEntry

/**
 * All stub data. In-memory only — no database, no network.
 * Filenames intentionally feel like terminal/filesystem output.
 */
object DummyData {

    // -------------------------------------------------------------------------
    // SCANS — 8 entries
    // -------------------------------------------------------------------------
    val scans: List<ScanFile> = listOf(
        ScanFile(
            id = "sc01",
            filename = "SYS_LOG_4471.DAT",
            sizeKb = 512,
            timestamp = "2089-04-17 03:22",
            status = ScanStatus.CLEAN
        ),
        ScanFile(
            id = "sc02",
            filename = "NET_PROBE_09.BIN",
            sizeKb = 88,
            timestamp = "2089-04-17 03:24",
            status = ScanStatus.FLAGGED
        ),
        ScanFile(
            id = "sc03",
            filename = "BOOT_TRACE_001.LOG",
            sizeKb = 14,
            timestamp = "2089-04-17 03:25",
            status = ScanStatus.CLEAN
        ),
        ScanFile(
            id = "sc04",
            filename = "KERNEL_PATCH_X7.HEX",
            sizeKb = 2048,
            timestamp = "2089-04-17 03:26",
            status = ScanStatus.QUARANTINED
        ),
        ScanFile(
            id = "sc05",
            filename = "COMMS_BUFFER_44.TMP",
            sizeKb = 33,
            timestamp = "2089-04-17 03:31",
            status = ScanStatus.CLEAN
        ),
        ScanFile(
            id = "sc06",
            filename = "SEC_AUDIT_R22.RPT",
            sizeKb = 107,
            timestamp = "2089-04-17 03:33",
            status = ScanStatus.FLAGGED
        ),
        ScanFile(
            id = "sc07",
            filename = "MEM_DUMP_0xF800.BIN",
            sizeKb = 4096,
            timestamp = "2089-04-17 03:35",
            status = ScanStatus.QUARANTINED
        ),
        ScanFile(
            id = "sc08",
            filename = "PROC_TABLE_77.DAT",
            sizeKb = 256,
            timestamp = "2089-04-17 03:38",
            status = ScanStatus.CLEAN
        )
    )

    // -------------------------------------------------------------------------
    // ORGANIZER — 6 tasks
    // -------------------------------------------------------------------------
    val tasks: List<OrganizerTask> = listOf(
        OrganizerTask(
            id = "tk01",
            title = "PATCH SECTOR 7 FIREWALL",
            body = "Apply delta-patch to perimeter nodes. Confirm checksum before deploy.",
            priority = Priority.HIGH,
            isDone = false
        ),
        OrganizerTask(
            id = "tk02",
            title = "REVIEW NET_PROBE LOG",
            body = "Flagged entries in scan batch 09. Cross-reference with intrusion matrix.",
            priority = Priority.HIGH,
            isDone = false
        ),
        OrganizerTask(
            id = "tk03",
            title = "CALIBRATE SENSOR ARRAY",
            body = "Run calibration cycle at 04:00 UTC. Expected deviation <0.3%.",
            priority = Priority.NORMAL,
            isDone = true
        ),
        OrganizerTask(
            id = "tk04",
            title = "UPDATE CONTACT LIST",
            body = "Remove decommissioned nodes. Add new relay endpoints from sector 12.",
            priority = Priority.LOW,
            isDone = false
        ),
        OrganizerTask(
            id = "tk05",
            title = "BACKUP KERNEL CONFIG",
            body = "Export current kernel params to offline storage. Rotate encryption keys.",
            priority = Priority.NORMAL,
            isDone = false
        ),
        OrganizerTask(
            id = "tk06",
            title = "QUARANTINE MEM_DUMP FILE",
            body = "Isolate MEM_DUMP_0xF800.BIN from active volume. Await forensic analysis.",
            priority = Priority.HIGH,
            isDone = false
        )
    )

    // -------------------------------------------------------------------------
    // TUTORIALS — 3 entries
    // -------------------------------------------------------------------------
    val tutorials: List<TutorialEntry> = listOf(
        TutorialEntry(
            id = "tu01",
            title = "GETTING STARTED",
            content = """
WINGMAN OS v1.0 — FIELD MANUAL

NAVIGATION
Use DPAD UP/DOWN to move through menus.
Press ENTER to open a section.
Press BACK to return to the main menu.

SECTIONS
SCANS     — View filesystem scan results.
ORGANIZER — Manage tasks and notes.
TUTORIALS — You are here.
MUSIC     — Access audio tracks.
SETTINGS  — Adjust system preferences.

TIP: Hold DPAD for accelerated scrolling.
Initial delay: 300ms. Repeat rate: 80ms.
After 1s hold, rate increases to 40ms.
            """.trimIndent(),
            tags = listOf("basics", "navigation", "dpad")
        ),
        TutorialEntry(
            id = "tu02",
            title = "SCAN STATUS CODES",
            content = """
SCAN STATUS REFERENCE

CLEAN
File passed all integrity checks.
No anomalies detected. Safe to use.

FLAGGED
File exhibits suspicious patterns.
Requires manual review before use.
Do not execute flagged binaries.

QUARANTINED
File has been isolated from the system.
Access restricted. Contact security admin.
Run full sector wipe if infection confirmed.

STATUS COLORS
GREEN  = CLEAN
AMBER  = FLAGGED
RED-AMBER = QUARANTINED
            """.trimIndent(),
            tags = listOf("scans", "status", "security")
        ),
        TutorialEntry(
            id = "tu03",
            title = "SETTINGS & EFFECTS",
            content = """
SYSTEM SETTINGS GUIDE

EFFECTS
Toggle scanlines and grain overlay.
Intensity slider: 0.0 to 1.0.
Higher = more visible CRT simulation.

GLOW
Amber glow halo on active menu item.
Disable for minimal visual mode.

FLICKER
Subtle screen flicker effect.
Disabled by default — enable for
maximum retro authenticity.

SOUND
Controls all SFX playback.
Sounds: CLICK, SCROLL, BOOT, BACK.
Volume fixed at 60%.

USERNAME
Displayed in the top status bar.
Change via SETTINGS > USERNAME.
            """.trimIndent(),
            tags = listOf("settings", "effects", "sound", "theme")
        )
    )

    // -------------------------------------------------------------------------
    // MUSIC — 6 tracks
    // -------------------------------------------------------------------------
    val tracks: List<MusicTrack> = listOf(
        MusicTrack(
            id = "tr01",
            title = "GHOST SIGNAL",
            artist = "CIRCUIT NULL",
            durationSeconds = 214
        ),
        MusicTrack(
            id = "tr02",
            title = "COLD BOOT",
            artist = "DEADLINK",
            durationSeconds = 183
        ),
        MusicTrack(
            id = "tr03",
            title = "SECTOR 7 DRIFT",
            artist = "CIRCUIT NULL",
            durationSeconds = 297
        ),
        MusicTrack(
            id = "tr04",
            title = "KERNEL PANIC",
            artist = "XFER PROTOCOL",
            durationSeconds = 156
        ),
        MusicTrack(
            id = "tr05",
            title = "AMBER STATIC",
            artist = "DEADLINK",
            durationSeconds = 241
        ),
        MusicTrack(
            id = "tr06",
            title = "TERMINAL BLOOM",
            artist = "XFER PROTOCOL",
            durationSeconds = 328
        )
    )
}
