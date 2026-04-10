package com.zhoulesin.whyme.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
    val wordId: Long,
    val masteryLevel: Int = 0,
    val isLearned: Boolean = false,
    val isNew: Boolean = true,
    val nextReviewDate: Long? = null,
    val reviewCount: Int = 0,
    val correctCount: Int = 0,
    val lastReviewResult: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val learnedAt: Long? = null,
    val lastReviewedAt: Long? = null
)
