package com.wngud.timebox.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wngud.timebox.data.local.BrainDumpEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BrainDumpDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrainDumpItem(item: BrainDumpEntity)

    @Query("SELECT * FROM brain_dump_items ORDER BY timestamp DESC")
    fun getBrainDumpItems(): Flow<List<BrainDumpEntity>>

    @Query("DELETE FROM brain_dump_items WHERE id = :itemId")
    suspend fun deleteBrainDumpItem(itemId: Long)

    @Query("DELETE FROM brain_dump_items")
    suspend fun deleteAllBrainDumpItems()
}