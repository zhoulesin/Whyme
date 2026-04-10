package com.zhoulesin.whyme.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.zhoulesin.whyme.data.local.entity.DailyLearningRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLearningRecordDao {
    @Insert
    suspend fun insertRecord(record: DailyLearningRecordEntity): Long

    @Query("SELECT * FROM daily_learning_records WHERE date = :date")
    suspend fun getRecordByDate(date: Long): DailyLearningRecordEntity?

    @Query("SELECT * FROM daily_learning_records ORDER BY date DESC LIMIT :limit")
    fun getRecentRecords(limit: Int): Flow<List<DailyLearningRecordEntity>>

    @Query("SELECT * FROM daily_learning_records WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getRecordsByDateRange(startDate: Long, endDate: Long): Flow<List<DailyLearningRecordEntity>>

    @Query("UPDATE daily_learning_records SET wordsLearned = wordsLearned + :wordsLearned, wordsReviewed = wordsReviewed + :wordsReviewed, correctCount = correctCount + :correctCount, totalQuestions = totalQuestions + :totalQuestions, durationMinutes = durationMinutes + :durationMinutes, accuracy = :accuracy WHERE date = :date")
    suspend fun updateRecord(date: Long, wordsLearned: Int, wordsReviewed: Int, correctCount: Int, totalQuestions: Int, durationMinutes: Int, accuracy: Float)

    @Query("SELECT SUM(wordsLearned) FROM daily_learning_records WHERE date >= :startDate AND date <= :endDate")
    suspend fun getTotalWordsLearnedInRange(startDate: Long, endDate: Long): Int

    @Query("SELECT SUM(durationMinutes) FROM daily_learning_records WHERE date >= :startDate AND date <= :endDate")
    suspend fun getTotalLearningMinutesInRange(startDate: Long, endDate: Long): Int

    @Query("DELETE FROM daily_learning_records")
    suspend fun clearAllRecords()
}