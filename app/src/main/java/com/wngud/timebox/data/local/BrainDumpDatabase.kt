package com.wngud.timebox.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

@Database(
    entities = [BrainDumpEntity::class], 
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class BrainDumpDatabase : RoomDatabase() {
    abstract fun brainDumpDao(): BrainDumpDao
}
