package com.wngud.timebox.di

import android.content.Context
import androidx.room.Room
import com.wngud.timebox.data.local.BrainDumpDao
import com.wngud.timebox.data.local.BrainDumpDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideBrainDumpDatabase(@ApplicationContext context: Context): BrainDumpDatabase {
        return Room.databaseBuilder(
            context,
            BrainDumpDatabase::class.java,
            "brain_dump_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideBrainDumpDao(database: BrainDumpDatabase): BrainDumpDao {
        return database.brainDumpDao()
    }

    @Provides
    @Singleton
    fun provideScheduleSlotDao(database: BrainDumpDatabase): com.wngud.timebox.data.local.ScheduleSlotDao {
        return database.scheduleSlotDao()
    }
}
