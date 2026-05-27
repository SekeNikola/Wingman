package com.wingman.launcher.data.model

/**
 * A static tutorial/doc entry. Content may contain "\n" line breaks.
 * Displayed in a two-level navigator: list → detail (scrollable).
 */
data class TutorialEntry(
    val id: String,
    val title: String,
    val content: String,        // plain text, "\n" line breaks
    val tags: List<String>
)
