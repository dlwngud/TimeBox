package com.wngud.timebox.domain.repository

import com.wngud.timebox.data.local.BrainDumpEntity
import kotlinx.coroutines.flow.Flow

interface BrainDumpRepository {
    suspend fun insertBrainDumpItem(item: BrainDumpEntity)
    fun getBrainDumpItems(): Flow<List<BrainDumpEntity>>
    suspend fun deleteBrainDumpItem(itemId: Long)
    suspend fun deleteAllBrainDumpItems()
    suspend fun updateBrainDumpItem(item: BrainDumpEntity)
    fun getBigThreeItems(): Flow<List<BrainDumpEntity>>
}