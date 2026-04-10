package com.zhoulesin.whyme.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 复习记录表
 * 记录用户每次复习单词的记录
 */
@Entity(
    tableName = "review_records",
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
        Index(value = ["reviewedAt"]),
        Index(value = ["level"])
    ]
)
data class ReviewRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val wordId: Long,
    val level: String,  // 复习时的词库级别
    val masteryLevel: Int = 0,  // 复习后的掌握级别
    val reviewedAt: Long = System.currentTimeMillis(),  // 复习时间
    val durationSeconds: Int = 0,  // 复习时长（秒）
    val isCorrect: Boolean = false,  // 是否正确
    val reviewResult: String? = null  // 复习结果（EASY/GOOD/HARD/AGAIN）
)