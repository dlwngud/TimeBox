package com.wngud.timebox.data.repository.timeBox.repositoryImp

import com.wngud.timebox.data.local.BrainDumpDao
import com.wngud.timebox.data.local.BrainDumpEntity
import com.wngud.timebox.domain.repository.BrainDumpRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BrainDumpRepositoryImpl @Inject constructor(
    private val dao: BrainDumpDao
) : BrainDumpRepository {
    override suspend fun insertBrainDumpItem(item: BrainDumpEntity) {
        dao.insertBrainDumpItem(item)
    }

    override fun getBrainDumpItems(): Flow<List<BrainDumpEntity>> {
        return dao.getBrainDumpItems()
    }

    override suspend fun deleteBrainDumpItem(itemId: Long) {
        dao.deleteBrainDumpItem(itemId)
    }

    override suspend fun deleteAllBrainDumpItems() {
        dao.deleteAllBrainDumpItems()
    }

    override suspend fun updateBrainDumpItem(item: BrainDumpEntity) {
        dao.updateBrainDumpItem(item)
    }

    override fun getBigThreeItems(): Flow<List<BrainDumpEntity>> {
        return dao.getBigThreeItems()
    }
}