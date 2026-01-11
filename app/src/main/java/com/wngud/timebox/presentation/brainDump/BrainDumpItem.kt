package com.wngud.timebox.presentation.brainDump

import com.wngud.timebox.data.local.BrainDumpEntity
import java.util.Date

data class BrainDumpItem(
    val id: Long,
    val content: String,
    val formattedTimestamp: String, // UI에 표시할 포맷된 시간
    val isBigThree: Boolean = false,
    val timestamp: Date // 원본 Date 객체를 포함하도록 변경
)

fun BrainDumpItem.toBrainDumpEntity(): BrainDumpEntity {
    return BrainDumpEntity(
        id = this.id,
        content = this.content,
        timestamp = this.timestamp, // BrainDumpItem의 timestamp를 사용
        isBigThree = this.isBigThree
    )
}
