package com.wingman.launcher.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for the audio subsystem.
 *
 * [AudioPlayer] is annotated @Inject and @Singleton so it can be
 * injected directly, but this module is here for explicit documentation
 * and to allow test overrides.
 *
 * If AudioPlayer were backed by an interface, the @Binds pattern
 * (as in AppBinds) would be used instead.
 */
@Module
@InstallIn(SingletonComponent::class)
object AudioModule
// AudioPlayer uses @Inject constructor + @Singleton — no @Provides needed.
