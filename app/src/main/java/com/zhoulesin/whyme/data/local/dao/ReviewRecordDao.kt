package com.zhoulesin.whyme.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.zhoulesin.whyme.data.local.entity.ReviewRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewRecordDao {
    @Insert
    suspend fun insertRecord(record: ReviewRecordEntity): Long

    @Query("SELECT * FROM review_records WHERE wordId = :wordId ORDER BY reviewedAt DESC")
    fun getRecordsByWordId(wordId: Long): Flow<List<ReviewRecordEntity>>

    @Query("SELECT * FROM review_records WHERE reviewedAt >= :startTime AND reviewedAt <= :endTime ORDER BY reviewedAt DESC")
    fun getRecordsByDateRange(startTime: Long, endTime: Long): Flow<List<ReviewRecordEntity>>

    @Query("SELECT * FROM review_records WHERE level = :level ORDER BY reviewedAt DESC")
    fun getRecordsByLevel(level: String): Flow<List<ReviewRecordEntity>>

    @Query("SELECT COUNT(*) FROM review_records WHERE wordId = :wordId")
    suspend fun getReviewCountByWordId(wordId: Long): Int

    @Query("SELECT COUNT(*) FROM review_records WHERE reviewedAt >= :startTime AND reviewedAt <= :endTime")
    suspend fun getReviewCountByDateRange(startTime: Long, endTime: Long): Int

    @Query("SELECT COUNT(*) FROM review_records WHERE wordId = :wordId AND isCorrect = 1")
    suspend fun getCorrectCountByWordId(wordId: Long): Int

    @Query("SELECT MAX(reviewedAt) FROM review_records WHERE wordId = :wordId")
    suspend fun getLastReviewTimeByWordId(wordId: Long): Long?

    @Query("DELETE FROM review_records")
    suspend fun clearAllRecords()
}