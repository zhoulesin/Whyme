package com.zhoulesin.whyme.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 学习记录表
 * 记录用户每次学习新单词的记录
 */
@Entity(
    tableName = "learning_records",
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["wordId"]),
        Index(value = ["learnedAt"]),
        Index(value = ["level"])
    ]
)
data class LearningRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val wordId: Long,
    val level: String,  // 学习时的词库级别
    val masteryLevel: Int = 0,  // 学习后的掌握级别
    val learnedAt: Long = System.currentTimeMillis(),  // 学习时间
    val durationSeconds: Int = 0,  // 学习时长（秒）
    val isMastered: Boolean = false  // 是否掌握
)