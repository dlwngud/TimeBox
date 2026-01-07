package com.wngud.timebox.presentation.brainDump

data class BrainDumpItem(
    val id: Long,
    val content: String,
    val formattedTimestamp: String // UI에 표시할 포맷된 시간
)