package com.wngud.timebox.data.modal

data class DailyStats(
    val focusTime: String,
    val bigThreeCompleted: Int,
    val bigThreeTotal: Int,
    val bufferPercent: Int,
    val efficiencyPercent: Int
)