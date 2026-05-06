package com.wingman.launcher

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point. @HiltAndroidApp triggers Hilt's code generation and
 * sets up the component hierarchy for the entire app.
 */
@HiltAndroidApp
class WingmanApplication : Application()
