package com.wingman.launcher.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wingman.launcher.data.db.dao.NoteDao
import com.wingman.launcher.data.db.dao.TaskDao
import com.wingman.launcher.data.db.entity.NoteEntity
import com.wingman.launcher.data.db.entity.TaskEntity

/**
 * Room database for Wingman launcher.
 *
 * Version history:
 *   1 (initial) -- notes + tasks tables
 *
 * exportSchema = true so the schema JSON is written to app/schemas/ for
 * migration tracking and diffing.
 */
@Database(
    entities = [NoteEntity::class, TaskEntity::class],
    version = 1,
    exportSchema = true
)
abstract class WingmanDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun taskDao(): TaskDao

    companion object {
        const val DATABASE_NAME = "wingman.db"
    }
}
