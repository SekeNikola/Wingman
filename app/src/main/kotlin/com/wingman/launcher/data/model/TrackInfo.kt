package com.wingman.launcher.data.model

/**
 * Domain model for a music track in the fake music library.
 * Pure Kotlin data class -- no Android or Room imports.
 */
data class TrackInfo(
    val id: String,
    val title: String,
    val artist: String,
    val durationSeconds: Int
)
