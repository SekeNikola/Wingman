package com.wingman.launcher.data.model

/**
 * Represents a single entry in the SCANS section — a file the system has "scanned."
 * Timestamps use a retro-future format: "2089-04-17 03:22"
 */
enum class ScanStatus { CLEAN, FLAGGED, QUARANTINED }

data class ScanFile(
    val id: String,
    val filename: String,
    val sizeKb: Int,
    val timestamp: String,      // formatted: "2089-04-17 03:22"
    val status: ScanStatus
)
