package com.wingman.launcher.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wingman.launcher.data.repository.SettingsRepository

/**
 * Manual factory for MainViewModel — no Hilt, no DI framework.
 * Called in MainActivity.viewModels { MainViewModelFactory(applicationContext) }.
 */
class MainViewModelFactory(
    private val applicationContext: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(
                settingsRepository = SettingsRepository(applicationContext)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
