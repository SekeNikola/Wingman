package com.wingman.launcher

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wingman.launcher.data.repository.SettingsRepository
import com.wingman.launcher.input.InputHandler
import com.wingman.launcher.sound.SoundEngine
import com.wingman.launcher.ui.WingmanApp
import com.wingman.launcher.viewmodel.MainViewModel
import com.wingman.launcher.viewmodel.MainViewModelFactory
import com.wingman.launcher.viewmodel.SettingsViewModel

/**
 * Single Activity. No Fragments. No back stack library.
 *
 * Responsibilities:
 *  - setContent with WingmanApp (full UI tree)
 *  - Route KeyEvents through InputHandler → MainViewModel
 *  - Init/release SoundEngine on lifecycle boundaries
 *  - Manual DI: shared SettingsRepository injected into both ViewModels
 */
class MainActivity : ComponentActivity() {

    private val settingsRepository by lazy { SettingsRepository(applicationContext) }

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(applicationContext)
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SettingsViewModel(settingsRepository) as T
        }
    }

    private lateinit var inputHandler: InputHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SoundEngine.init(applicationContext)

        inputHandler = InputHandler { event ->
            mainViewModel.onInputEvent(event)
        }

        setContent {
            WingmanApp(
                mainViewModel = mainViewModel,
                settingsViewModel = settingsViewModel
            )
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        inputHandler.handleKeyDown(keyCode, event)
        return true // consume all key events — launcher owns hardware input
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        inputHandler.handleKeyUp(keyCode, event)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        inputHandler.release()
        SoundEngine.release()
    }
}
