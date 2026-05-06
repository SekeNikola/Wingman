package com.wingman.launcher.data.model

/**
 * Domain model for an organizer note.
 * Pure Kotlin data class -- no Room or Android imports.
 * Room entities (NoteEntity) are internal to the data layer and never cross
 * the OrganizerRepository boundary.
 */
data class Note(
    val id: Long = 0L,
    val title: String,
    val body: String,
    val createdAt: Long         // Unix epoch ms
)
