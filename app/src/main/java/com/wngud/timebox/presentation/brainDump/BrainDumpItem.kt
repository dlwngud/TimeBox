package com.wngud.timebox.presentation.brainDump

import com.wngud.timebox.data.local.BrainDumpEntity
import java.util.Date

data class BrainDumpItem(
    val id: Long,
    val content: String,
    val formattedTimestamp: String // UI에 표시할 포맷된 시간
)

fun BrainDumpItem.toBrainDumpEntity(): BrainDumpEntity {
    // timestamp는 UI 모델에서 직접적으로 관리하지 않으므로, 새 엔티티 생성 시에는 현재 시각을 사용하거나
    // 기존 엔티티의 timestamp를 유지해야 합니다. 여기서는 편의상 기본값인 현재 시각을 사용하도록 합니다.
    // 하지만 실제 수정 로직에서는 원본 엔티티의 timestamp를 가져와야 합니다.
    return BrainDumpEntity(
        id = this.id,
        content = this.content,
        timestamp = Date() // 실제 수정 시에는 원본 timestamp를 ViewModel에서 관리하여 전달해야 합니다.
    )
}