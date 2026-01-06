package com.wngud.timebox.di

import com.wngud.timebox.data.repository.timeBox.repositoryImp.BrainDumpRepositoryImpl
import com.wngud.timebox.domain.repository.BrainDumpRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBrainDumpRepository(brainDumpRepositoryImpl: BrainDumpRepositoryImpl): BrainDumpRepository
}