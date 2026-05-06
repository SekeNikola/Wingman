package com.wingman.launcher.data.source

import com.wingman.launcher.data.model.ScanFile
import com.wingman.launcher.data.model.ScanStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory fake data source for the Scans section.
 * Returns a static list of dummy scan file entries; no network call required.
 *
 * Filenames follow the naming conventions shown in the brief (e.g. SCAN_XXXX.dat).
 * Timestamps are pre-formatted strings, ready for direct display.
 */
@Singleton
class FakeScansDataSource @Inject constructor() {

    private val fakeScanFiles: List<ScanFile> = listOf(
        ScanFile(
            id = "scan_001",
            filename = "SCAN_7741.dat",
            sizeKb = 1024,
            timestamp = "2024-11-03 14:22",
            status = ScanStatus.COMPLETE
        ),
        ScanFile(
            id = "scan_002",
            filename = "ECHO_PROBE_02.bin",
            sizeKb = 512,
            timestamp = "2024-11-03 09:47",
            status = ScanStatus.COMPLETE
        ),
        ScanFile(
            id = "scan_003",
            filename = "SECTOR_MAP_ALPHA.raw",
            sizeKb = 4096,
            timestamp = "2024-11-02 22:13",
            status = ScanStatus.CORRUPT
        ),
        ScanFile(
            id = "scan_004",
            filename = "RECON_DELTA_09.dat",
            sizeKb = 768,
            timestamp = "2024-11-02 18:05",
            status = ScanStatus.COMPLETE
        ),
        ScanFile(
            id = "scan_005",
            filename = "DEEP_PULSE_03.bin",
            sizeKb = 2048,
            timestamp = "2024-11-01 11:30",
            status = ScanStatus.PENDING
        ),
        ScanFile(
            id = "scan_006",
            filename = "FREQ_ANALYSIS_07.log",
            sizeKb = 256,
            timestamp = "2024-10-31 23:59",
            status = ScanStatus.COMPLETE
        ),
        ScanFile(
            id = "scan_007",
            filename = "GHOST_SIGNAL_X.raw",
            sizeKb = 3072,
            timestamp = "2024-10-30 16:44",
            status = ScanStatus.CORRUPT
        ),
        ScanFile(
            id = "scan_008",
            filename = "VECTOR_TRACE_14.dat",
            sizeKb = 1536,
            timestamp = "2024-10-29 08:12",
            status = ScanStatus.COMPLETE
        ),
        ScanFile(
            id = "scan_009",
            filename = "NOISE_FLOOR_BRV.bin",
            sizeKb = 128,
            timestamp = "2024-10-28 19:00",
            status = ScanStatus.PENDING
        ),
        ScanFile(
            id = "scan_010",
            filename = "THERMAL_GRID_02.dat",
            sizeKb = 896,
            timestamp = "2024-10-27 13:37",
            status = ScanStatus.COMPLETE
        )
    )

    /**
     * Returns a cold Flow that emits the static scan file list immediately.
     * ScansRepository wraps this in its own Flow pipeline.
     */
    fun getScanFiles(): Flow<List<ScanFile>> = flowOf(fakeScanFiles)
}
