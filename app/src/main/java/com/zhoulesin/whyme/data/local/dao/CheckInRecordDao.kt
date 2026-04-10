package com.zhoulesin.whyme.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.zhoulesin.whyme.data.local.entity.CheckInRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInRecordDao {
    @Insert
    suspend fun insertRecord(record: CheckInRecordEntity): Long

    @Query("SELECT * FROM check_in_records ORDER BY checkInDate DESC")
    fun getAllRecords(): Flow<List<CheckInRecordEntity>>

    @Query("SELECT * FROM check_in_records WHERE checkInDate = :date")
    suspend fun getRecordByDate(date: Long): CheckInRecordEntity?

    @Query("SELECT * FROM check_in_records WHERE checkInDate >= :startDate AND checkInDate <= :endDate ORDER BY checkInDate ASC")
    fun getRecordsByDateRange(startDate: Long, endDate: Long): Flow<List<CheckInRecordEntity>>

    @Query("SELECT COUNT(*) FROM check_in_records")
    suspend fun getTotalCheckInDays(): Int

    @Query("SELECT MAX(streak) FROM check_in_records")
    suspend fun getLongestStreak(): Int

    @Query("SELECT streak FROM check_in_records ORDER BY checkInDate DESC LIMIT 1")
    suspend fun getCurrentStreak(): Int?

    @Query("SELECT SUM(learningMinutes) FROM check_in_records WHERE checkInDate >= :startDate AND checkInDate <= :endDate")
    suspend fun getTotalLearningMinutesByDateRange(startDate: Long, endDate: Long): Int

    @Query("DELETE FROM check_in_records")
    suspend fun clearAllRecords()
}