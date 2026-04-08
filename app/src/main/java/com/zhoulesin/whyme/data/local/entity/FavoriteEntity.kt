package com.zhoulesin.whyme.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 收藏表实体
 * 记录用户收藏的单词，支持多对多关系
 */
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
        Index(value = ["wordId"], unique = true),
        Index(value = ["createdAt"])
    ]
)
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val wordId: Long,                    // 关联的单词ID
    
    val createdAt: Long = System.currentTimeMillis(),  // 收藏时间
    
    val notes: String? = null            // 用户备注（可选）
)
