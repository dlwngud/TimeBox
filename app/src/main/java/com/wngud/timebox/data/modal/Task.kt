package com.wngud.timebox.data.modal

data class Task(
    val id: String,
    val title: String,
    val isCompleted: Boolean,
    val brainDumpId: Long? = null  // BrainDump 항목과 연결
)