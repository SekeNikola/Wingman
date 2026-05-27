package com.wingman.launcher.data.repository

import com.wingman.launcher.data.model.TutorialEntry
import com.wingman.launcher.data.source.DummyData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Provides tutorial entry data as a StateFlow.
 * Static content — no mutation needed.
 */
class TutorialsRepository {

    private val _tutorials = MutableStateFlow<List<TutorialEntry>>(DummyData.tutorials)
    val tutorials: Flow<List<TutorialEntry>> = _tutorials.asStateFlow()
}
