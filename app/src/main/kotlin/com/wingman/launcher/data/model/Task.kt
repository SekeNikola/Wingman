package com.wingman.launcher.data.model

/**
 * Domain model for an organizer task (checklist item).
 * Pure Kotlin data class -- no Room or Android imports.
 * TaskEntity.isDone maps to Task.isCompleted (renamed for clarity at the
 * repository boundary).
 */
data class Task(
    val id: Long = 0L,
    val label: String,
    val isCompleted: Boolean,
    val createdAt: Long         // Unix epoch ms
)
