package com.zhoulesin.whyme.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 打卡记录表
 * 记录用户的打卡情况
 */
@Entity(
    tableName = "check_in_records"
)
data class CheckInRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val checkInDate: Long,  // 打卡日期（当天开始时间戳）
    val checkInTime: Long = System.currentTimeMillis(),  // 打卡时间
    val streak: Int = 0,  // 连续打卡天数
    val totalDays: Int = 0,  // 总打卡天数
    val learningMinutes: Int = 0,  // 当日学习时长（分钟）
    val wordsLearned: Int = 0,  // 当日学习单词数
    val wordsReviewed: Int = 0  // 当日复习单词数
)