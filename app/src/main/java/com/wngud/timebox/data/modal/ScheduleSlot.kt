package com.wngud.timebox.data.modal

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

/**
 * 타임라인에 배치된 일정을 나타내는 데이터 모델
 * BrainDump 아이템을 특정 시간대에 배치한 정보를 담습니다.
 */
data class ScheduleSlot(
    val id: String = UUID.randomUUID().toString(),
    val brainDumpItemId: Long,  // BrainDump 아이템과 연결
    val title: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val colorType: EventColorType = EventColorType.BLUE,
    val date: LocalDate = LocalDate.now()  // 날짜 정보
)
