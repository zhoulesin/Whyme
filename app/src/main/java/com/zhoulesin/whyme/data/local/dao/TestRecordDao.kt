package com.zhoulesin.whyme.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.zhoulesin.whyme.data.local.entity.TestRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TestRecordDao {
    @Insert
    suspend fun insertRecord(record: TestRecordEntity): Long

    @Query("SELECT * FROM test_records ORDER BY testDate DESC")
    fun getAllRecords(): Flow<List<TestRecordEntity>>

    @Query("SELECT * FROM test_records WHERE testDate >= :startTime AND testDate <= :endTime ORDER BY testDate DESC")
    fun getRecordsByDateRange(startTime: Long, endTime: Long): Flow<List<TestRecordEntity>>

    @Query("SELECT * FROM test_records WHERE testType = :testType ORDER BY testDate DESC")
    fun getRecordsByType(testType: String): Flow<List<TestRecordEntity>>

    @Query("SELECT COUNT(*) FROM test_records WHERE testDate >= :startTime AND testDate <= :endTime")
    suspend fun getTestCountByDateRange(startTime: Long, endTime: Long): Int

    @Query("SELECT AVG(accuracy) FROM test_records WHERE testDate >= :startTime AND testDate <= :endTime")
    suspend fun getAverageAccuracyByDateRange(startTime: Long, endTime: Long): Float

    @Query("SELECT SUM(durationSeconds) FROM test_records WHERE testDate >= :startTime AND testDate <= :endTime")
    suspend fun getTotalTestDurationByDateRange(startTime: Long, endTime: Long): Int

    @Query("DELETE FROM test_records")
    suspend fun clearAllRecords()
}