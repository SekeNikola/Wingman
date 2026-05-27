package com.wingman.launcher.data.model

/**
 * A single task/note entry in the ORGANIZER section.
 * HIGH priority renders with "!!" prefix. Done tasks render with strikethrough.
 */
enum class Priority { HIGH, NORMAL, LOW }

data class OrganizerTask(
    val id: String,
    val title: String,
    val body: String,
    val priority: Priority,
    val isDone: Boolean
)
