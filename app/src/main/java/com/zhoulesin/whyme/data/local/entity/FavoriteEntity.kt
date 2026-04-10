package com.zhoulesin.whyme.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "favorites",
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId", "wordId"], unique = true),
        Index(value = ["userId", "createdAt"])
    ]
)
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val userId: String = "",
    val wordId: Long,

    val createdAt: Long = System.currentTimeMillis(),
    val notes: String? = null
)
