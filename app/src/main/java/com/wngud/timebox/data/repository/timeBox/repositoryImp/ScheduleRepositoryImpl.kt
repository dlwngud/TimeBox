package com.wngud.timebox.data.repository.timeBox.repositoryImp

import com.wngud.timebox.data.local.ScheduleSlotDao
import com.wngud.timebox.data.local.toScheduleSlot
import com.wngud.timebox.data.local.toScheduleSlotEntity
import com.wngud.timebox.data.modal.ScheduleSlot
import com.wngud.timebox.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/**
 * ScheduleRepository 구현체
 */
class ScheduleRepositoryImpl @Inject constructor(
    private val dao: ScheduleSlotDao
) : ScheduleRepository {
    
    override suspend fun insertScheduleSlot(slot: ScheduleSlot) {
        dao.insertScheduleSlot(slot.toScheduleSlotEntity())
    }
    
    override fun getScheduleSlotsForDate(date: LocalDate): Flow<List<ScheduleSlot>> {
        return dao.getScheduleSlotsForDate(date.toEpochDay())
            .map { entities -> entities.map { it.toScheduleSlot() } }
    }
    
    override suspend fun deleteScheduleSlot(slotId: String) {
        dao.deleteScheduleSlotById(slotId)
    }
    
    override suspend fun deleteScheduleSlotsByBrainDumpId(brainDumpItemId: Long) {
        dao.deleteScheduleSlotsByBrainDumpId(brainDumpItemId)
    }
    
    override suspend fun updateScheduleSlot(slot: ScheduleSlot) {
        dao.updateScheduleSlot(slot.toScheduleSlotEntity())
    }
    
    override suspend fun isTimeSlotAvailable(
        startTime: LocalTime,
        endTime: LocalTime,
        date: LocalDate
    ): Boolean {
        val overlappingSlots = dao.getOverlappingSlots(
            dateMillis = date.toEpochDay(),
            startHour = startTime.hour,
            startMinute = startTime.minute,
            endHour = endTime.hour,
            endMinute = endTime.minute
        )
        return overlappingSlots.isEmpty()
    }
    
    override suspend fun getScheduleSlotAtTime(
        startTime: LocalTime,
        date: LocalDate
    ): ScheduleSlot? {
        val entity = dao.getScheduleSlotAtTime(
            dateMillis = date.toEpochDay(),
            startHour = startTime.hour,
            startMinute = startTime.minute
        )
        return entity?.toScheduleSlot()
    }
}
