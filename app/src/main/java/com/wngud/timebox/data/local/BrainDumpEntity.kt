package com.wngud.timebox.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wngud.timebox.presentation.brainDump.BrainDumpItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "brain_dump_items")
data class BrainDumpEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val timestamp: Date = Date(),
    val isBigThree: Boolean = false // Big Three 필드 추가
)

fun BrainDumpEntity.toBrainDumpItem(): BrainDumpItem {
    val formatter = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
    return BrainDumpItem(
        id = this.id,
        content = this.content,
        formattedTimestamp = formatter.format(this.timestamp),
        isBigThree = this.isBigThree,
        timestamp = this.timestamp // 원본 Date 객체 매핑
    )
}
