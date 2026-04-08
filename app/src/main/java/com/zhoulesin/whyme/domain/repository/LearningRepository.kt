package com.zhoulesin.whyme.domain.repository

import com.zhoulesin.whyme.domain.model.DailyGoal
import com.zhoulesin.whyme.domain.model.LearningRecord
import com.zhoulesin.whyme.domain.model.UserStats
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * 学习记录仓库接口
 */
interface LearningRepository {
    /**
     * 获取今日学习记录
     */
    fun getTodayRecord(): Flow<LearningRecord?>

    /**
     * 获取指定日期的学习记录
     */
    suspend fun getRecordByDate(date: LocalDate): LearningRecord?

    /**
     * 获取指定日期范围的学习记录
     */
    fun getRecordsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<LearningRecord>>

    /**
     * 保存学习记录
     */
    suspend fun saveRecord(record: LearningRecord)

    /**
     * 更新学习记录
     */
    suspend fun updateRecord(record: LearningRecord)

    /**
     * 获取用户统计数据
     */
    fun getUserStats(): Flow<UserStats>

    /**
     * 获取每日学习目标
     */
    fun getDailyGoal(): Flow<DailyGoal>

    /**
     * 更新每日学习目标
     */
    suspend fun updateDailyGoal(goal: DailyGoal)

    /**
     * 获取连续打卡天数
     */
    suspend fun getCurrentStreak(): Int

    /**
     * 获取最长连续打卡天数
     */
    suspend fun getLongestStreak(): Int

    /**
     * 记录一次学习会话
     */
    suspend fun recordLearningSession(wordsLearned: Int, wordsReviewed: Int, correctCount: Int, durationSeconds: Long)
}
