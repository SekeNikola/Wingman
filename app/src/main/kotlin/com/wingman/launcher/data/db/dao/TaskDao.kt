package com.wingman.launcher.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wingman.launcher.data.db.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for organizer tasks.
 * All queries return Flow for reactive UI updates.
 */
@Dao
interface TaskDao {

    /**
     * Observe all tasks ordered by creation time, newest first.
     * Emits a new list whenever the tasks table changes.
     */
    @Query("SELECT * FROM tasks ORDER BY created_at DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    /**
     * Insert a new task (or replace if primary key conflicts).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    /**
     * Update the done/completed flag for a task by its ID.
     */
    @Query("UPDATE tasks SET is_done = :done WHERE id = :id")
    suspend fun updateDone(id: Long, done: Boolean)

    /**
     * Delete a specific task by its full entity (matched on primary key).
     */
    @Delete
    suspend fun delete(task: TaskEntity)
}
