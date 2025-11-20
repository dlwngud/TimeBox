package com.wngud.timebox.data.modal

import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class ScheduleEvent(
    val id: String,
    val title: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val colorType: EventColorType
) {
    val timeRange: String
        get() = "${startTime.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${endTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
}

enum class EventColorType {
    GREEN, BLUE, GRAY
}