package com.wingman.launcher.data.model

/**
 * Domain model for a single entry in the Scans section.
 * Data is static/fake (FakeScansDataSource) -- no network call.
 * timestamp is pre-formatted for display; the UI must not re-parse it.
 */
data class ScanFile(
    val id: String,
    val filename: String,           // e.g. "SCAN_7741.dat"
    val sizeKb: Int,
    val timestamp: String,          // pre-formatted: "2024-11-03 14:22"
    val status: ScanStatus
)

enum class ScanStatus {
    COMPLETE,
    CORRUPT,
    PENDING
}
