package com.wingman.launcher.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wingman.launcher.data.db.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for notes.
 * All queries return Flow for reactive UI updates via Room's built-in
 * coroutines integration (room-ktx).
 */
@Dao
interface NoteDao {

    /**
     * Observe all notes ordered by creation time, newest first.
     * Emits a new list whenever the notes table changes.
     */
    @Query("SELECT * FROM notes ORDER BY created_at DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    /**
     * Insert a new note. If the same primary key already exists the row is
     * replaced (safe for upsert scenarios).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    /**
     * Delete a specific note by its full entity (matched on primary key).
     */
    @Delete
    suspend fun delete(note: NoteEntity)
}
