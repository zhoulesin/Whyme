package com.zhoulesin.whyme.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 学习记录数据库实体
 */
@Entity(tableName = "learning_records")
data class LearningRecordEntity(
    @PrimaryKey
    val date: Long, // Epoch day
    val wordsLearned: Int,
    val wordsReviewed: Int,
    val correctCount: Int,
    val durationSeconds: Long,
    val lastUpdated: Long = System.currentTimeMillis()
)
