package com.wngud.timebox.domain.repository

import com.wngud.timebox.data.modal.ScheduleSlot
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime

/**
 * 일정 관리를 위한 Repository 인터페이스
 */
interface ScheduleRepository {
    
    /**
     * 새로운 일정 슬롯을 추가합니다.
     */
    suspend fun insertScheduleSlot(slot: ScheduleSlot)
    
    /**
     * 특정 날짜의 모든 일정 슬롯을 가져옵니다.
     */
    fun getScheduleSlotsForDate(date: LocalDate): Flow<List<ScheduleSlot>>
    
    /**
     * ID로 일정 슬롯을 삭제합니다.
     */
    suspend fun deleteScheduleSlot(slotId: String)
    
    /**
     * BrainDump 아이템 ID로 연결된 모든 일정 슬롯을 삭제합니다.
     */
    suspend fun deleteScheduleSlotsByBrainDumpId(brainDumpItemId: Long)
    
    /**
     * 일정 슬롯을 업데이트합니다.
     */
    suspend fun updateScheduleSlot(slot: ScheduleSlot)
    
    /**
     * 특정 시간대에 일정을 배치할 수 있는지 확인합니다.
     * @return true if the time slot is available, false if there's an overlap
     */
    suspend fun isTimeSlotAvailable(
        startTime: LocalTime, 
        endTime: LocalTime, 
        date: LocalDate
    ): Boolean
    
    /**
     * 특정 시간에 시작하는 일정 슬롯을 가져옵니다.
     * @return 해당 시간에 시작하는 일정, 없으면 null
     */
    suspend fun getScheduleSlotAtTime(
        startTime: LocalTime,
        date: LocalDate
    ): ScheduleSlot?
    
    /**
     * BrainDump 아이템 ID로 연결된 모든 일정 슬롯의 색상을 업데이트합니다.
     * @param brainDumpItemId BrainDump 아이템 ID
     * @param isBigThree Big Three 여부 (true면 BLUE, false면 GREEN)
     */
    suspend fun updateScheduleColorByBrainDumpId(brainDumpItemId: Long, isBigThree: Boolean)
    
    /**
     * 모든 일정 슬롯을 삭제합니다.
     */
    suspend fun deleteAllScheduleSlots()
}
