package com.zhoulesin.whyme.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.zhoulesin.whyme.data.local.entity.LearningRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningRecordDao {
    @Insert
    suspend fun insertRecord(record: LearningRecordEntity): Long

    @Query("SELECT * FROM learning_records WHERE wordId = :wordId ORDER BY learnedAt DESC")
    fun getRecordsByWordId(wordId: Long): Flow<List<LearningRecordEntity>>

    @Query("SELECT * FROM learning_records WHERE learnedAt >= :startTime AND learnedAt <= :endTime ORDER BY learnedAt DESC")
    fun getRecordsByDateRange(startTime: Long, endTime: Long): Flow<List<LearningRecordEntity>>

    @Query("SELECT * FROM learning_records WHERE level = :level ORDER BY learnedAt DESC")
    fun getRecordsByLevel(level: String): Flow<List<LearningRecordEntity>>

    @Query("SELECT COUNT(*) FROM learning_records WHERE wordId = :wordId")
    suspend fun getLearningCountByWordId(wordId: Long): Int

    @Query("SELECT COUNT(*) FROM learning_records WHERE learnedAt >= :startTime AND learnedAt <= :endTime")
    suspend fun getLearningCountByDateRange(startTime: Long, endTime: Long): Int

    @Query("SELECT MAX(learnedAt) FROM learning_records WHERE wordId = :wordId")
    suspend fun getLastLearnedTimeByWordId(wordId: Long): Long?

    @Query("DELETE FROM learning_records")
    suspend fun clearAllRecords()
}