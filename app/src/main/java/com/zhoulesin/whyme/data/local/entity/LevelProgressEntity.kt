package com.zhoulesin.whyme.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_progress")
data class LevelProgressEntity(
    @PrimaryKey
    val level: String,
    val userId: String = "",
    val totalWords: Int = 0,
    val learnedWords: Int = 0,
    val masteredWords: Int = 0,
    val lastStudyDate: Long? = null
)
