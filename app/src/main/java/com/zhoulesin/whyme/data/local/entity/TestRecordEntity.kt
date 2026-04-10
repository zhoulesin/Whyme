package com.zhoulesin.whyme.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 测试记录表
 * 记录用户每次测试的记录
 */
@Entity(
    tableName = "test_records"
)
data class TestRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val testType: String,  // 测试类型（WORD_TO_CHINESE/CHINESE_TO_WORD/SPELLING）
    val totalQuestions: Int = 0,  // 总题目数
    val correctCount: Int = 0,  // 正确题数
    val accuracy: Float = 0f,  // 正确率
    val testDate: Long = System.currentTimeMillis(),  // 测试时间
    val durationSeconds: Int = 0,  // 测试时长（秒）
    val questionCount: Int = 0,  // 测试题量
    val source: String? = null  // 测试来源（TODAY_LEARNED/ALL_LEARNED/FAVORITES/ALL）
)