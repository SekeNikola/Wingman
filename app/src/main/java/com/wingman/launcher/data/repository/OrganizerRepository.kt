package com.wingman.launcher.data.repository

import com.wingman.launcher.data.model.OrganizerTask
import com.wingman.launcher.data.source.DummyData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Provides task data. Supports toggling isDone in-memory.
 * No persistence — state resets on app restart.
 */
class OrganizerRepository {

    private val _tasks = MutableStateFlow<List<OrganizerTask>>(DummyData.tasks)
    val tasks: Flow<List<OrganizerTask>> = _tasks.asStateFlow()

    fun toggleTask(id: String) {
        _tasks.update { list ->
            list.map { task ->
                if (task.id == id) task.copy(isDone = !task.isDone) else task
            }
        }
    }
}
