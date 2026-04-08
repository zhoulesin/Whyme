package com.zhoulesin.whyme.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 单词数据库实体
 */
@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 基本信息
    val word: String,
    val phonetic: String,
    val definition: String,
    val example: String,
    val translation: String,

    // 美式/英式音标（可选）
    val usPhonetic: String? = null,
    val ukPhonetic: String? = null,

    // 学习状态
    val masteryLevel: Int = 0,           // 掌握等级 0-5
    val isLearned: Boolean = false,      // 是否已学习过
    val isNew: Boolean = true,           // 是否新词（未被学习过）
    val isFavorite: Boolean = false,     // 是否收藏

    // 记忆曲线
    val nextReviewDate: Long? = null,    // 下次复习日期 (Epoch day)
    val reviewCount: Int = 0,            // 复习次数
    val correctCount: Int = 0,           // 正确次数
    val lastReviewResult: String? = null, // 上次复习结果 (AGAIN/HARD/GOOD/EASY)

    // 时间戳
    val createdAt: Long = System.currentTimeMillis(),
    val learnedAt: Long? = null,          // 首次学习时间
    val lastReviewedAt: Long? = null,   // 上次复习时间

    // 来源词库
    val wordBank: String? = null,        // 来源词库名称
    val level: String = "GAOZHONG"      // 词库级别
)
