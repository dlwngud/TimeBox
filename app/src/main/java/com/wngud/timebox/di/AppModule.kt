package com.wngud.timebox.di

import com.wngud.timebox.manager.AppThemeManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppThemeManager(): AppThemeManager {
        return AppThemeManager()
    }
}
