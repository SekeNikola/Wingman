package com.wingman.launcher.data.repository

import com.wingman.launcher.data.db.dao.NoteDao
import com.wingman.launcher.data.db.dao.TaskDao
import com.wingman.launcher.data.db.entity.NoteEntity
import com.wingman.launcher.data.db.entity.TaskEntity
import com.wingman.launcher.data.model.Note
import com.wingman.launcher.data.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [OrganizerRepository].
 *
 * This is the exclusive class that:
 *   1. Holds references to Room DAOs ([NoteDao], [TaskDao]).
 *   2. Maps Room entities (NoteEntity, TaskEntity) to domain models (Note, Task).
 *
 * Mapping rules (per INTERFACES.md):
 *   NoteEntity.id        -> Note.id
 *   NoteEntity.title     -> Note.title
 *   NoteEntity.body      -> Note.body
 *   NoteEntity.createdAt -> Note.createdAt
 *   TaskEntity.id        -> Task.id
 *   TaskEntity.label     -> Task.label
 *   TaskEntity.isDone    -> Task.isCompleted  (renamed)
 *   TaskEntity.createdAt -> Task.createdAt
 */
@Singleton
class OrganizerRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao,
    private val taskDao: TaskDao
) : OrganizerRepository {

    // ── Notes ─────────────────────────────────────────────────────────────────

    override val notes: Flow<List<Note>> =
        noteDao.getAllNotes().map { entities -> entities.map(::entityToNote) }

    override suspend fun addNote(title: String, body: String) {
        noteDao.insert(
            NoteEntity(
                title = title,
                body = body,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun deleteNote(note: Note) {
        noteDao.delete(noteToEntity(note))
    }

    // ── Tasks ─────────────────────────────────────────────────────────────────

    override val tasks: Flow<List<Task>> =
        taskDao.getAllTasks().map { entities -> entities.map(::entityToTask) }

    override suspend fun addTask(label: String) {
        taskDao.insert(
            TaskEntity(
                label = label,
                isDone = false,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun toggleTask(task: Task) {
        taskDao.updateDone(task.id, !task.isCompleted)
    }

    override suspend fun deleteTask(task: Task) {
        taskDao.delete(taskToEntity(task))
    }

    // ── Mapping helpers ───────────────────────────────────────────────────────

    private fun entityToNote(entity: NoteEntity): Note = Note(
        id = entity.id,
        title = entity.title,
        body = entity.body,
        createdAt = entity.createdAt
    )

    private fun noteToEntity(note: Note): NoteEntity = NoteEntity(
        id = note.id,
        title = note.title,
        body = note.body,
        createdAt = note.createdAt
    )

    private fun entityToTask(entity: TaskEntity): Task = Task(
        id = entity.id,
        label = entity.label,
        isCompleted = entity.isDone,
        createdAt = entity.createdAt
    )

    private fun taskToEntity(task: Task): TaskEntity = TaskEntity(
        id = task.id,
        label = task.label,
        isDone = task.isCompleted,
        createdAt = task.createdAt
    )
}
