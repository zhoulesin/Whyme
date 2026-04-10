package com.zhoulesin.whyme.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 每日学习记录表
 * 记录用户每天的学习统计数据
 */
@Entity(
    tableName = "daily_learning_records"
)
data class DailyLearningRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long,  // 日期（当天开始时间戳）
    val wordsLearned: Int = 0,  // 学习单词数
    val wordsReviewed: Int = 0,  // 复习单词数
    val correctCount: Int = 0,  // 正确次数
    val totalQuestions: Int = 0,  // 总题目数
    val durationMinutes: Int = 0,  // 学习时长（分钟）
    val accuracy: Float = 0f,  // 正确率
    val streak: Int = 0  // 连续学习天数
)