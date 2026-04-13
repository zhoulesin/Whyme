package com.zhoulesin.whyme.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 单词数据库实体
 * 只保存词汇基本信息，不包含用户相关的学习状态
 */
@Entity(
    tableName = "words",
    indices = [
        Index(value = ["word"], unique = true),
        Index(value = ["level"]),
        Index(value = ["wordBank"])
    ]
)
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 基本信息
    val word: String,                    // 单词（唯一索引）
    val phonetic: String,                // 音标
    val definition: String,              // 详细释义
    val example: String,                 // 例句（英文原句）
    val exampleTranslation: String,      // 例句中文译文
    val translation: String,             // 单词中文翻译

    // 美式/英式音标（可选）
    val usPhonetic: String? = null,
    val ukPhonetic: String? = null,

    // 来源词库
    val wordBank: String? = null,        // 来源词库名称
    val level: String = "GAOZHONG"      // 词库级别
)
