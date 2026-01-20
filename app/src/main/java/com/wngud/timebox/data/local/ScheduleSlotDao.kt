package com.wngud.timebox.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * ScheduleSlot 데이터 접근을 위한 DAO
 */
@Dao
interface ScheduleSlotDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleSlot(slot: ScheduleSlotEntity)
    
    @Query("SELECT * FROM schedule_slots WHERE dateMillis = :dateMillis ORDER BY startHour, startMinute")
    fun getScheduleSlotsForDate(dateMillis: Long): Flow<List<ScheduleSlotEntity>>
    
    @Query("SELECT * FROM schedule_slots WHERE id = :slotId")
    suspend fun getScheduleSlotById(slotId: String): ScheduleSlotEntity?
    
    @Delete
    suspend fun deleteScheduleSlot(slot: ScheduleSlotEntity)
    
    @Query("DELETE FROM schedule_slots WHERE id = :slotId")
    suspend fun deleteScheduleSlotById(slotId: String)
    
    @Query("DELETE FROM schedule_slots WHERE brainDumpItemId = :brainDumpItemId")
    suspend fun deleteScheduleSlotsByBrainDumpId(brainDumpItemId: Long)
    
    @Update
    suspend fun updateScheduleSlot(slot: ScheduleSlotEntity)
    
    @Query("SELECT * FROM schedule_slots WHERE dateMillis = :dateMillis AND " +
            "((startHour * 60 + startMinute) < (:endHour * 60 + :endMinute)) AND " +
            "((endHour * 60 + endMinute) > (:startHour * 60 + :startMinute))")
    suspend fun getOverlappingSlots(
        dateMillis: Long,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int
    ): List<ScheduleSlotEntity>
    
    @Query("SELECT * FROM schedule_slots WHERE dateMillis = :dateMillis AND startHour = :startHour AND startMinute = :startMinute LIMIT 1")
    suspend fun getScheduleSlotAtTime(
        dateMillis: Long,
        startHour: Int,
        startMinute: Int
    ): ScheduleSlotEntity?
}
