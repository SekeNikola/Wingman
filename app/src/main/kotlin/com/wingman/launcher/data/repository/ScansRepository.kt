package com.wingman.launcher.data.repository

import com.wingman.launcher.data.model.ScanFile
import com.wingman.launcher.data.source.FakeScansDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository interface for the Scans section.
 */
interface ScansRepository {
    /** Emits the list of scan files. With fake data this emits once; real impl would refresh. */
    val scanFiles: Flow<List<ScanFile>>
}

/**
 * Concrete implementation backed by [FakeScansDataSource].
 * In a real app this would delegate to a network or file-system source.
 */
@Singleton
class ScansRepositoryImpl @Inject constructor(
    private val dataSource: FakeScansDataSource
) : ScansRepository {

    override val scanFiles: Flow<List<ScanFile>> = dataSource.getScanFiles()
}
