package com.wingman.launcher.data.repository

import com.wingman.launcher.data.model.SettingsState
import com.wingman.launcher.data.model.ThemeVariant
import com.wingman.launcher.data.source.PrefsDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository interface for user settings.
 * ViewModels depend on this interface to allow mocking in tests.
 */
interface SettingsRepository {
    val settingsFlow: Flow<SettingsState>
    suspend fun updateEffectIntensity(value: Float)
    suspend fun updateThemeVariant(variant: ThemeVariant)
}

/**
 * Concrete implementation backed by [PrefsDataSource] (DataStore<Preferences>).
 *
 * Delegates read/write operations directly to the DataStore wrapper.
 * Value clamping is handled in [PrefsDataSource]; the repository passes through.
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val prefsDataSource: PrefsDataSource
) : SettingsRepository {

    override val settingsFlow: Flow<SettingsState> = prefsDataSource.settingsFlow

    override suspend fun updateEffectIntensity(value: Float) {
        prefsDataSource.updateEffectIntensity(value)
    }

    override suspend fun updateThemeVariant(variant: ThemeVariant) {
        prefsDataSource.updateThemeVariant(variant)
    }
}
