package com.wingman.launcher.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wingman.launcher.data.repository.SettingsRepository

/**
 * Manual factory for SettingsViewModel.
 * Used by UI developer when instantiating SettingsViewModel in WingmanApp or SettingsScreen.
 */
class SettingsViewModelFactory(
    private val applicationContext: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(
                repository = SettingsRepository(applicationContext)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
