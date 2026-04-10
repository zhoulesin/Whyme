package com.zhoulesin.whyme.data.local.dao

import androidx.room.*
import com.zhoulesin.whyme.data.local.entity.LearningRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningRecordDao {
    @Query("SELECT * FROM learning_records WHERE userId = :userId AND date = :date")
    fun getRecordByDate(userId: String, date: Long): Flow<LearningRecordEntity?>

    @Query("SELECT * FROM learning_records WHERE userId = :userId AND date = :date")
    suspend fun getRecordByDateSync(userId: String, date: Long): LearningRecordEntity?

    @Query("SELECT * FROM learning_records WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getRecordsBetween(userId: String, startDate: Long, endDate: Long): Flow<List<LearningRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: LearningRecordEntity)

    @Update
    suspend fun updateRecord(record: LearningRecordEntity)

    @Query("SELECT * FROM learning_records WHERE userId = :userId ORDER BY date DESC")
    fun getAllRecords(userId: String): Flow<List<LearningRecordEntity>>

    @Query("SELECT SUM(wordsLearned) FROM learning_records WHERE userId = :userId")
    suspend fun getTotalWordsLearned(userId: String): Int?

    @Query("SELECT SUM(wordsReviewed) FROM learning_records WHERE userId = :userId")
    suspend fun getTotalWordsReviewed(userId: String): Int?

    @Query("SELECT SUM(durationSeconds) FROM learning_records WHERE userId = :userId")
    suspend fun getTotalLearningSeconds(userId: String): Long?

    @Query("SELECT COUNT(*) FROM learning_records WHERE userId = :userId")
    suspend fun getRecordCount(userId: String): Int
}
