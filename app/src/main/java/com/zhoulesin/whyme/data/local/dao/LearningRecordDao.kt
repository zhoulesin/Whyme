package com.zhoulesin.whyme.data.local.dao

import androidx.room.*
import com.zhoulesin.whyme.data.local.entity.LearningRecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * 学习记录数据访问对象
 */
@Dao
interface LearningRecordDao {
    @Query("SELECT * FROM learning_records WHERE date = :date")
    fun getRecordByDate(date: Long): Flow<LearningRecordEntity?>

    @Query("SELECT * FROM learning_records WHERE date = :date")
    suspend fun getRecordByDateSync(date: Long): LearningRecordEntity?

    @Query("SELECT * FROM learning_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getRecordsBetween(startDate: Long, endDate: Long): Flow<List<LearningRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: LearningRecordEntity)

    @Update
    suspend fun updateRecord(record: LearningRecordEntity)

    @Query("SELECT * FROM learning_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<LearningRecordEntity>>

    @Query("SELECT SUM(wordsLearned) FROM learning_records")
    suspend fun getTotalWordsLearned(): Int?

    @Query("SELECT SUM(wordsReviewed) FROM learning_records")
    suspend fun getTotalWordsReviewed(): Int?

    @Query("SELECT SUM(durationSeconds) FROM learning_records")
    suspend fun getTotalLearningSeconds(): Long?

    @Query("SELECT COUNT(*) FROM learning_records")
    suspend fun getRecordCount(): Int
}
