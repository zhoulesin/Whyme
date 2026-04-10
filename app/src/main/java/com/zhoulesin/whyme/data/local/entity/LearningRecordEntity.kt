package com.zhoulesin.whyme.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "learning_records")
data class LearningRecordEntity(
    @PrimaryKey
    val date: Long,
    val userId: String = "",
    val wordsLearned: Int,
    val wordsReviewed: Int,
    val correctCount: Int,
    val durationSeconds: Long,
    val lastUpdated: Long = System.currentTimeMillis()
)
