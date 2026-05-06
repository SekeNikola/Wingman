package com.wingman.launcher.data.repository

import com.wingman.launcher.data.model.Note
import com.wingman.launcher.data.model.Task
import kotlinx.coroutines.flow.Flow

/**
 * Public interface for the Organizer data layer.
 * ViewModels depend on this interface (not the concrete impl) to allow mocking in tests.
 * Room entities never cross this boundary.
 */
interface OrganizerRepository {
    val notes: Flow<List<Note>>
    val tasks: Flow<List<Task>>

    suspend fun addNote(title: String, body: String)
    suspend fun deleteNote(note: Note)
    suspend fun addTask(label: String)
    suspend fun toggleTask(task: Task)
    suspend fun deleteTask(task: Task)
}
