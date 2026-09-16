package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dnd_history")
data class DndHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val prayerName: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val durationMinutes: Int,
    val dndMode: String, // "DND", "SILENT", "VIBRATE"
    val status: String // "COMPLETED", "CANCELLED_EARLY", "TEST_MODE"
)
