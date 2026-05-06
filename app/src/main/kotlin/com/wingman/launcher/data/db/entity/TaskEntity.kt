package com.wingman.launcher.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for an organizer task (checklist item). Internal to the data layer.
 * isDone (DB name) maps to Task.isCompleted (domain name) at the repository boundary.
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "label")
    val label: String,

    @ColumnInfo(name = "is_done")
    val isDone: Boolean,

    @ColumnInfo(name = "created_at")
    val createdAt: Long   // Unix epoch ms
)
