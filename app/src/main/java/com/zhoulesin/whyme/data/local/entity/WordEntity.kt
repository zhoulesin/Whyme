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
    val word: String,
    val phonetic: String,
    val definition: String,
    val example: String,
    val translation: String,
    val masteryLevel: Int = 0,
    val isFavorite: Boolean = false,
    val nextReviewDate: Long? = null, // Epoch day
    val reviewCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastReviewedAt: Long? = null
)
