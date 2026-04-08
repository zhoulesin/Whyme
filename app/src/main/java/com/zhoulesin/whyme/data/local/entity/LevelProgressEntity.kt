package com.zhoulesin.whyme.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 级别学习进度实体
 */
@Entity(tableName = "level_progress")
data class LevelProgressEntity(
    @PrimaryKey
    val level: String,                    // 级别名称
    val totalWords: Int = 0,             // 词库总词数
    val learnedWords: Int = 0,           // 已学习词数
    val masteredWords: Int = 0,          // 已掌握词数
    val lastStudyDate: Long? = null      // 最近学习日期 (Epoch day)
)
