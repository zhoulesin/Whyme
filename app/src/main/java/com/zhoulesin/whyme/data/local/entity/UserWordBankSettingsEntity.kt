package com.zhoulesin.whyme.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户词库设置实体
 */
@Entity(tableName = "user_word_bank_settings")
data class UserWordBankSettingsEntity(
    @PrimaryKey
    val id: Long = 1, // 只有一个用户，所以用固定 ID
    val currentLevel: String = "GAOZHONG",
    val enabledLevels: String = "[\"GAOZHONG\"]" // JSON 数组存储
)
