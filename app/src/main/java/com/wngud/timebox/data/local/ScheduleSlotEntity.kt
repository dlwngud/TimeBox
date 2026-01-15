package com.wngud.timebox.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wngud.timebox.data.modal.EventColorType
import com.wngud.timebox.data.modal.ScheduleSlot
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Room 데이터베이스용 ScheduleSlot Entity
 * LocalTime과 LocalDate를 Int와 Long으로 변환하여 저장합니다.
 */
@Entity(tableName = "schedule_slots")
data class ScheduleSlotEntity(
    @PrimaryKey val id: String,
    val brainDumpItemId: Long,
    val title: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val colorType: String,
    val dateMillis: Long  // LocalDate를 epoch day로 저장
)

/**
 * Entity를 Domain Model로 변환
 */
fun ScheduleSlotEntity.toScheduleSlot(): ScheduleSlot {
    return ScheduleSlot(
        id = this.id,
        brainDumpItemId = this.brainDumpItemId,
        title = this.title,
        startTime = LocalTime.of(this.startHour, this.startMinute),
        endTime = LocalTime.of(this.endHour, this.endMinute),
        colorType = EventColorType.valueOf(this.colorType),
        date = LocalDate.ofEpochDay(this.dateMillis)
    )
}

/**
 * Domain Model을 Entity로 변환
 */
fun ScheduleSlot.toScheduleSlotEntity(): ScheduleSlotEntity {
    return ScheduleSlotEntity(
        id = this.id,
        brainDumpItemId = this.brainDumpItemId,
        title = this.title,
        startHour = this.startTime.hour,
        startMinute = this.startTime.minute,
        endHour = this.endTime.hour,
        endMinute = this.endTime.minute,
        colorType = this.colorType.name,
        dateMillis = this.date.toEpochDay()
    )
}
