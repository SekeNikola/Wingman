package com.wingman.launcher.di

import android.content.Context
import androidx.room.Room
import com.wingman.launcher.data.db.WingmanDatabase
import com.wingman.launcher.data.db.dao.NoteDao
import com.wingman.launcher.data.db.dao.TaskDao
import com.wingman.launcher.data.repository.MusicRepository
import com.wingman.launcher.data.repository.MusicRepositoryImpl
import com.wingman.launcher.data.repository.OrganizerRepository
import com.wingman.launcher.data.repository.OrganizerRepositoryImpl
import com.wingman.launcher.data.repository.ScansRepository
import com.wingman.launcher.data.repository.ScansRepositoryImpl
import com.wingman.launcher.data.repository.SettingsRepository
import com.wingman.launcher.data.repository.SettingsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides all database, DAO, and repository singletons.
 *
 * Split into two objects:
 *   [AppProvides]  -- @Provides functions for concrete Android objects (Room)
 *   [AppBinds]     -- @Binds functions binding interfaces to their implementations
 */
@Module
@InstallIn(SingletonComponent::class)
object AppProvides {

    @Provides
    @Singleton
    fun provideWingmanDatabase(
        @ApplicationContext context: Context
    ): WingmanDatabase =
        Room.databaseBuilder(
            context,
            WingmanDatabase::class.java,
            WingmanDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideNoteDao(db: WingmanDatabase): NoteDao = db.noteDao()

    @Provides
    @Singleton
    fun provideTaskDao(db: WingmanDatabase): TaskDao = db.taskDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBinds {

    @Binds
    @Singleton
    abstract fun bindOrganizerRepository(
        impl: OrganizerRepositoryImpl
    ): OrganizerRepository

    @Binds
    @Singleton
    abstract fun bindScansRepository(
        impl: ScansRepositoryImpl
    ): ScansRepository

    @Binds
    @Singleton
    abstract fun bindMusicRepository(
        impl: MusicRepositoryImpl
    ): MusicRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository
}
