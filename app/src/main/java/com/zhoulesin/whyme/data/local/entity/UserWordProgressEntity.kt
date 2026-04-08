package com.zhoulesin.whyme.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 用户单词学习进度表
 * 记录用户对每个单词的学习状态和进度
 */
@Entity(
    tableName = "user_word_progress",
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["wordId"], unique = true),
        Index(value = ["nextReviewDate"]),
        Index(value = ["masteryLevel"]),
        Index(value = ["isLearned"])
    ]
)
data class UserWordProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val wordId: Long,                    // 关联的单词ID

    // 学习状态
    val masteryLevel: Int = 0,           // 掌握等级 0-5
    val isLearned: Boolean = false,      // 是否已学习过
    val isNew: Boolean = true,           // 是否新词（未被学习过）

    // 记忆曲线
    val nextReviewDate: Long? = null,    // 下次复习日期 (Epoch day)
    val reviewCount: Int = 0,            // 复习次数
    val correctCount: Int = 0,           // 正确次数
    val lastReviewResult: String? = null, // 上次复习结果 (AGAIN/HARD/GOOD/EASY)

    // 时间戳
    val createdAt: Long = System.currentTimeMillis(),
    val learnedAt: Long? = null,          // 首次学习时间
    val lastReviewedAt: Long? = null     // 上次复习时间
)
