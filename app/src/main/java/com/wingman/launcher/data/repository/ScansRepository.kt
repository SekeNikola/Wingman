package com.wingman.launcher.data.repository

import com.wingman.launcher.data.model.ScanFile
import com.wingman.launcher.data.source.DummyData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Provides scan file data as a StateFlow.
 * In-memory only — no database. Data is static dummy content.
 */
class ScansRepository {

    private val _scans = MutableStateFlow<List<ScanFile>>(DummyData.scans)
    val scans: Flow<List<ScanFile>> = _scans.asStateFlow()
}
