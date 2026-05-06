package com.wingman.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.wingman.launcher.ui.WingmanApp
import com.wingman.launcher.util.AudioPlayer
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Single activity entry point for the Wingman launcher.
 *
 * Registered with launchMode="singleTask" and taskAffinity="" in the manifest
 * so the launcher is always a fresh, solitary task and never lands in the
 * app task stack of whatever was previously open.
 *
 * The HOME + DEFAULT intent filter makes this activity visible in the Android
 * launcher chooser ("Which app should be the home screen?").
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var audioPlayer: AudioPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        setContent {
            WingmanApp()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.release()
    }
}
