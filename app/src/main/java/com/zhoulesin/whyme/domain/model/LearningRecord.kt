package com.zhoulesin.whyme.domain.model

import java.time.LocalDate

/**
 * 学习记录实体
 */
data class LearningRecord(
    val id: Long = 0,
    val date: LocalDate,
    val wordsLearned: Int,
    val wordsReviewed: Int,
    val correctCount: Int,
    val durationSeconds: Long
) {
    val accuracy: Float
        get() = if (wordsReviewed + wordsLearned > 0) {
            correctCount.toFloat() / (wordsReviewed + wordsLearned)
        } else 0f
}

/**
 * 用户统计数据
 */
data class UserStats(
    val totalWordsLearned: Int,
    val totalWordsReviewed: Int,
    val currentStreak: Int, // 连续打卡天数
    val longestStreak: Int,
    val totalLearningMinutes: Long,
    val todayWordsLearned: Int,
    val todayWordsReviewed: Int,
    val todayTests: Int,
    val todayTestAccuracy: Float,
    val todayLearningMinutes: Int,
    val todayAccuracy: Float
)

/**
 * 每日学习目标
 */
data class DailyGoal(
    val wordsPerDay: Int = 10,
    val reviewPerDay: Int = 20,
    val testsPerDay: Int = 10,
    val minutesPerDay: Int = 15
)
