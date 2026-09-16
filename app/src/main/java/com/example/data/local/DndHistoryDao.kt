package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DndHistoryDao {
    @Query("SELECT * FROM dnd_history ORDER BY startTimeMillis DESC")
    fun getAllHistory(): Flow<List<DndHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entity: DndHistoryEntity): Long

    @Query("DELETE FROM dnd_history")
    suspend fun clearHistory()

    @Query("SELECT COUNT(*) FROM dnd_history WHERE status = 'COMPLETED'")
    fun getCompletedCount(): Flow<Int>

    @Query("SELECT SUM(durationMinutes) FROM dnd_history WHERE status = 'COMPLETED'")
    fun getTotalSilencedMinutes(): Flow<Int?>
}
