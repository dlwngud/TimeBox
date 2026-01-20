package com.wngud.timebox.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * LocalDate를 한글 날짜 형식으로 변환
 * 예: "2026.01.20 (월) 오늘의 결과"
 */
fun LocalDate.toKoreanDateString(): String {
    val dayOfWeek = when (this.dayOfWeek) {
        DayOfWeek.MONDAY -> "월"
        DayOfWeek.TUESDAY -> "화"
        DayOfWeek.WEDNESDAY -> "수"
        DayOfWeek.THURSDAY -> "목"
        DayOfWeek.FRIDAY -> "금"
        DayOfWeek.SATURDAY -> "토"
        DayOfWeek.SUNDAY -> "일"
    }
    
    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    return "${this.format(formatter)} ($dayOfWeek) 오늘의 결과"
}
